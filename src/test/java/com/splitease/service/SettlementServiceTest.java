package com.splitease.service;

import com.splitease.dto.BalanceResponse;
import com.splitease.dto.SettlementResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the settlement algorithms.
 * Uses the seeded data (4 users, 1 group, 6 expenses) for testing.
 */
@SpringBootTest
class SettlementServiceTest {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private ExactSettlementService exactSettlementService;

    @Autowired
    private BalanceService balanceService;

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
    @DisplayName("Greedy algorithm produces valid settlements for seeded data")
    void greedySettlements_seededData_producesValidResults() {
        List<SettlementResponse> settlements = settlementService.computeSettlements(1L);

        assertNotNull(settlements);
        assertFalse(settlements.isEmpty(), "Settlements should not be empty");

        // All amounts should be positive
        for (SettlementResponse s : settlements) {
            assertTrue(s.getAmount().compareTo(BigDecimal.ZERO) > 0,
                    "Settlement amount should be positive");
            assertNotEquals(s.getFromUserId(), s.getToUserId(),
                    "Sender and receiver should be different");
        }
    }

    @Test
    @DisplayName("Exact DFS algorithm produces valid settlements for seeded data")
    void exactSettlements_seededData_producesValidResults() {
        Map<String, Object> result = exactSettlementService.computeExactSettlements(1L);

        assertTrue((boolean) result.get("feasible"), "Should be feasible for 4 members");
        int txCount = (int) result.get("transactionCount");
        assertTrue(txCount > 0, "Should have at least one transaction");
    }

    @Test
    @DisplayName("Exact DFS produces equal or fewer transactions than greedy")
    void exactVsGreedy_exactShouldBeEqualOrBetter() {
        List<SettlementResponse> greedyResults = settlementService.computeSettlements(1L);
        Map<String, Object> exactResult = exactSettlementService.computeExactSettlements(1L);

        int greedyCount = greedyResults.size();
        int exactCount = (int) exactResult.get("transactionCount");

        assertTrue(exactCount <= greedyCount,
                "Exact should produce <= transactions than greedy. Exact=" + exactCount + " Greedy=" + greedyCount);
    }

    @Test
    @DisplayName("Balances sum to zero across all members")
    void balances_sumToZero() {
        List<BalanceResponse> balances = balanceService.getGroupBalances(1L);

        BigDecimal sum = balances.stream()
                .map(BalanceResponse::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, sum.compareTo(BigDecimal.ZERO),
                "Sum of all balances should be zero, but was: " + sum);
    }

    @Test
    @DisplayName("Settlement amounts match total debt")
    void settlementAmounts_matchTotalDebt() {
        List<BalanceResponse> balances = balanceService.getGroupBalances(1L);
        List<SettlementResponse> settlements = settlementService.computeSettlements(1L);

        // Total debt = sum of all negative balances
        BigDecimal totalDebt = balances.stream()
                .map(BalanceResponse::getBalance)
                .filter(b -> b.compareTo(BigDecimal.ZERO) < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs();

        // Total settlement amount
        BigDecimal totalSettled = settlements.stream()
                .map(SettlementResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, totalDebt.compareTo(totalSettled),
                "Total settlements should equal total debt. Debt=" + totalDebt + " Settled=" + totalSettled);
    }

    @Test
    @DisplayName("Compare endpoint returns valid comparison data")
    void compareAlgorithms_returnsValidComparison() {
        List<SettlementResponse> greedyResults = settlementService.computeSettlements(1L);
        Map<String, Object> comparison = exactSettlementService.compareAlgorithms(1L, greedyResults);

        assertNotNull(comparison.get("greedy"));
        assertNotNull(comparison.get("exact"));
        assertNotNull(comparison.get("summary"));

        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) comparison.get("summary");
        assertNotNull(summary.get("verdict"));
        assertNotNull(summary.get("isGreedyOptimal"));
    }
}
