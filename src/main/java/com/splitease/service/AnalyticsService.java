package com.splitease.service;

import com.splitease.model.Expense;
import com.splitease.model.ExpenseCategory;
import com.splitease.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Analytics service providing spending insights for a group.
 * Computes breakdowns by category, by member, and over time.
 */
@Service
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;

    public AnalyticsService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    /**
     * Full analytics for a group: category breakdown, member contributions, timeline.
     */
    public Map<String, Object> getGroupAnalytics(Long groupId) {
        List<Expense> expenses = expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId);

        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("totalExpenses", expenses.size());
        analytics.put("totalAmount", expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Category breakdown
        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        for (ExpenseCategory cat : ExpenseCategory.values()) {
            categoryTotals.put(cat.name(), BigDecimal.ZERO);
            categoryCounts.put(cat.name(), 0);
        }
        for (Expense e : expenses) {
            String cat = e.getCategory().name();
            categoryTotals.merge(cat, e.getAmount(), BigDecimal::add);
            categoryCounts.merge(cat, 1, Integer::sum);
        }
        // Remove zero categories for cleaner output
        categoryTotals.entrySet().removeIf(entry -> entry.getValue().compareTo(BigDecimal.ZERO) == 0);
        categoryCounts.entrySet().removeIf(entry -> entry.getValue() == 0);

        List<Map<String, Object>> categoryBreakdown = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("category", entry.getKey());
            item.put("displayName", ExpenseCategory.valueOf(entry.getKey()).getDisplayName());
            item.put("total", entry.getValue());
            item.put("count", categoryCounts.getOrDefault(entry.getKey(), 0));
            categoryBreakdown.add(item);
        }
        // Sort by total descending
        categoryBreakdown.sort((a, b) -> ((BigDecimal) b.get("total")).compareTo((BigDecimal) a.get("total")));
        analytics.put("categoryBreakdown", categoryBreakdown);

        // Member contribution breakdown
        Map<Long, BigDecimal> memberTotals = new LinkedHashMap<>();
        Map<Long, String> memberNames = new LinkedHashMap<>();
        Map<Long, Integer> memberCounts = new LinkedHashMap<>();
        for (Expense e : expenses) {
            Long uid = e.getPaidBy().getId();
            memberTotals.merge(uid, e.getAmount(), BigDecimal::add);
            memberNames.putIfAbsent(uid, e.getPaidBy().getName());
            memberCounts.merge(uid, 1, Integer::sum);
        }

        List<Map<String, Object>> memberBreakdown = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : memberTotals.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", entry.getKey());
            item.put("userName", memberNames.get(entry.getKey()));
            item.put("totalPaid", entry.getValue());
            item.put("expenseCount", memberCounts.getOrDefault(entry.getKey(), 0));
            memberBreakdown.add(item);
        }
        memberBreakdown.sort((a, b) -> ((BigDecimal) b.get("totalPaid")).compareTo((BigDecimal) a.get("totalPaid")));
        analytics.put("memberBreakdown", memberBreakdown);

        // Available categories (for the frontend dropdown)
        List<Map<String, String>> categories = new ArrayList<>();
        for (ExpenseCategory cat : ExpenseCategory.values()) {
            Map<String, String> catMap = new LinkedHashMap<>();
            catMap.put("value", cat.name());
            catMap.put("label", cat.getDisplayName());
            categories.add(catMap);
        }
        analytics.put("availableCategories", categories);

        return analytics;
    }
}
