package com.splitease.service;

import com.splitease.dto.CreateExpenseRequest;
import com.splitease.model.Expense;
import com.splitease.model.ExpenseShare;
import com.splitease.model.User;
import com.splitease.repository.ExpenseRepository;
import com.splitease.repository.ExpenseShareRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ExpenseServiceTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private com.splitease.repository.UserRepository userRepository;

    @org.junit.jupiter.api.BeforeEach
    void setUpSecurity() {
        com.splitease.model.User user = userRepository.findById(1L).orElseThrow();
        com.splitease.security.CustomUserDetails details = new com.splitease.security.CustomUserDetails(user);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(details, null, details.getAuthorities())
        );
    }

    @Test
    @DisplayName("Exact splits correctly allocate shares and fail on mismatch")
    void addExpense_exactSplits() {
        CreateExpenseRequest req = new CreateExpenseRequest();
        req.setPaidByUserId(1L);
        req.setAmount(new BigDecimal("100.00"));
        req.setDescription("Test Exact");
        req.setSplitType("EXACT");
        
        Map<Long, BigDecimal> splits = new HashMap<>();
        splits.put(1L, new BigDecimal("40.00"));
        splits.put(2L, new BigDecimal("60.00"));
        req.setSplits(splits);

        Expense e = expenseService.addExpense(1L, req);
        List<ExpenseShare> shares = e.getShares();
        
        assertEquals(2, shares.size());
        
        BigDecimal share1 = shares.stream().filter(s -> s.getUser().getId().equals(1L)).findFirst().get().getShareAmount();
        BigDecimal share2 = shares.stream().filter(s -> s.getUser().getId().equals(2L)).findFirst().get().getShareAmount();
        
        assertEquals(0, new BigDecimal("40.00").compareTo(share1));
        assertEquals(0, new BigDecimal("60.00").compareTo(share2));
        
        // Mismatch test
        req.getSplits().put(2L, new BigDecimal("70.00")); // Sum = 110 != 100
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> expenseService.addExpense(1L, req));
        assertEquals("Exact splits must sum to the total amount", ex.getMessage());
    }

    @Test
    @DisplayName("Percentage splits correctly allocate shares and handle remainders")
    void addExpense_percentageSplits() {
        CreateExpenseRequest req = new CreateExpenseRequest();
        req.setPaidByUserId(1L);
        req.setAmount(new BigDecimal("100.00"));
        req.setDescription("Test Percent");
        req.setSplitType("PERCENTAGE");
        
        Map<Long, BigDecimal> splits = new HashMap<>();
        splits.put(1L, new BigDecimal("33.33"));
        splits.put(2L, new BigDecimal("33.33"));
        splits.put(3L, new BigDecimal("33.34"));
        req.setSplits(splits);

        Expense e = expenseService.addExpense(1L, req);
        List<ExpenseShare> shares = e.getShares();
        
        BigDecimal totalShareAmt = shares.stream().map(ExpenseShare::getShareAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("100.00").compareTo(totalShareAmt));
        
        // Mismatch test
        req.getSplits().put(3L, new BigDecimal("30.00")); // Sum = 96.66 != 100
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> expenseService.addExpense(1L, req));
        assertEquals("Percentages must sum to exactly 100", ex.getMessage());
    }
}
