package com.splitease.controller;

import com.splitease.dto.CreateExpenseRequest;
import com.splitease.model.Expense;
import com.splitease.model.ExpenseShare;
import com.splitease.repository.ExpenseRepository;
import com.splitease.service.ExpenseService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseRepository expenseRepository;

    public ExpenseController(ExpenseService expenseService, ExpenseRepository expenseRepository) {
        this.expenseService = expenseService;
        this.expenseRepository = expenseRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addExpense(@PathVariable Long groupId,
                                                               @Valid @RequestBody CreateExpenseRequest request) {
        Expense expense = expenseService.addExpense(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseToMap(expense));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getGroupExpenses(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Expense> expensePage = expenseService.getGroupExpensesPaginated(groupId, search, pageable);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Expense e : expensePage.getContent()) {
            result.add(expenseToMap(e));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", result);
        response.put("page", expensePage.getNumber());
        response.put("size", expensePage.getSize());
        response.put("totalElements", expensePage.getTotalElements());
        response.put("totalPages", expensePage.getTotalPages());
        response.put("last", expensePage.isLast());

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<String> exportExpensesToCSV(@PathVariable Long groupId) {
        List<Expense> expenses = expenseService.getGroupExpenses(groupId);
        
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Description,Category,Amount,Paid By,Date\n");
        for (Expense e : expenses) {
            String desc = escapeCsvField(e.getDescription());
            String name = escapeCsvField(e.getPaidBy().getName());
            
            sb.append(e.getId()).append(",")
              .append("\"").append(desc).append("\",")
              .append(e.getCategory()).append(",")
              .append(e.getAmount()).append(",")
              .append("\"").append(name).append("\",")
              .append(e.getCreatedAt()).append("\n");
        }
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"expenses.csv\"")
                .body(sb.toString());
    }
    
    private String escapeCsvField(String field) {
        if (field == null) return "";
        // Prevent CSV Formula Injection
        if (field.startsWith("=") || field.startsWith("+") || field.startsWith("-") || field.startsWith("@")) {
            field = "'" + field;
        }
        return field.replace("\"", "\"\"");
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long groupId,
                                               @PathVariable Long expenseId) {
        expenseService.deleteExpense(groupId, expenseId);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> expenseToMap(Expense expense) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", expense.getId());
        map.put("paidByUserId", expense.getPaidBy().getId());
        map.put("paidByUserName", expense.getPaidBy().getName());
        map.put("amount", expense.getAmount());
        map.put("description", expense.getDescription());
        map.put("category", expense.getCategory().name());
        map.put("categoryDisplayName", expense.getCategory().getDisplayName());
        map.put("createdAt", expense.getCreatedAt().toString());

        // Include shares
        List<Map<String, Object>> shares = new ArrayList<>();
        for (ExpenseShare share : expense.getShares()) {
            Map<String, Object> shareMap = new LinkedHashMap<>();
            shareMap.put("userId", share.getUser().getId());
            shareMap.put("userName", share.getUser().getName());
            shareMap.put("shareAmount", share.getShareAmount());
            shares.add(shareMap);
        }
        map.put("shares", shares);
        return map;
    }
}
