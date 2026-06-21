package com.splitease.service;

import com.splitease.dto.CreateExpenseRequest;
import com.splitease.model.*;
import com.splitease.repository.*;
import com.splitease.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ExpenseShareRepository shareRepository;
    private final ActivityService activityService;

    public ExpenseService(ExpenseRepository expenseRepository,
                          ExpenseGroupRepository groupRepository,
                          GroupMemberRepository memberRepository,
                          UserRepository userRepository,
                          ExpenseShareRepository shareRepository,
                          ActivityService activityService) {
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.shareRepository = shareRepository;
        this.activityService = activityService;
    }

    @Transactional
    public Expense addExpense(Long groupId, CreateExpenseRequest request) {
        verifyMembership(groupId);
        ExpenseGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));

        User paidBy = userRepository.findById(request.getPaidByUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getPaidByUserId()));

        // Verify the payer is a member of the group
        if (!memberRepository.existsByGroupIdAndUserId(groupId, request.getPaidByUserId())) {
            throw new IllegalArgumentException("User is not a member of this group");
        }

        List<GroupMember> members = memberRepository.findByGroupId(groupId);
        if (members.isEmpty()) {
            throw new IllegalArgumentException("Group has no members to split the expense");
        }

        // Parse category
        ExpenseCategory category = ExpenseCategory.OTHER;
        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            try {
                category = ExpenseCategory.valueOf(request.getCategory().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid category, default to OTHER
            }
        }

        // Create expense
        Expense expense = new Expense(group, paidBy, request.getAmount(), request.getDescription(), category);
        expense = expenseRepository.save(expense);

        String splitType = request.getSplitType() != null ? request.getSplitType().toUpperCase() : "EQUAL";
        Map<Long, BigDecimal> splits = request.getSplits();

        if ("EXACT".equals(splitType)) {
            if (splits == null || splits.isEmpty()) throw new IllegalArgumentException("Splits map required for EXACT split type");
            BigDecimal totalSplits = splits.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalSplits.compareTo(request.getAmount()) != 0) {
                throw new IllegalArgumentException("Exact splits must sum to the total amount");
            }
            for (GroupMember member : members) {
                BigDecimal shareAmt = splits.getOrDefault(member.getUser().getId(), BigDecimal.ZERO);
                if (shareAmt.compareTo(BigDecimal.ZERO) > 0) {
                    ExpenseShare share = shareRepository.save(new ExpenseShare(expense, member.getUser(), shareAmt));
                    expense.getShares().add(share);
                }
            }
        } else if ("PERCENTAGE".equals(splitType)) {
            if (splits == null || splits.isEmpty()) throw new IllegalArgumentException("Splits map required for PERCENTAGE split type");
            BigDecimal totalPercent = splits.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalPercent.compareTo(new BigDecimal("100")) != 0) {
                throw new IllegalArgumentException("Percentages must sum to exactly 100");
            }
            
            BigDecimal totalAllocated = BigDecimal.ZERO;
            List<ExpenseShare> pendingShares = new java.util.ArrayList<>();
            
            for (GroupMember member : members) {
                BigDecimal pct = splits.getOrDefault(member.getUser().getId(), BigDecimal.ZERO);
                if (pct.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal shareAmt = request.getAmount().multiply(pct)
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    totalAllocated = totalAllocated.add(shareAmt);
                    pendingShares.add(new ExpenseShare(expense, member.getUser(), shareAmt));
                }
            }
            
            // Handle rounding remainder
            BigDecimal remainder = request.getAmount().subtract(totalAllocated);
            if (remainder.compareTo(BigDecimal.ZERO) != 0 && !pendingShares.isEmpty()) {
                ExpenseShare firstShare = pendingShares.get(0);
                firstShare.setShareAmount(firstShare.getShareAmount().add(remainder));
            }
            
            List<ExpenseShare> savedShares = shareRepository.saveAll(pendingShares);
            expense.getShares().addAll(savedShares);
            
        } else { // EQUAL
            BigDecimal shareAmount = request.getAmount()
                    .divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);

            BigDecimal totalShares = shareAmount.multiply(BigDecimal.valueOf(members.size()));
            BigDecimal remainder = request.getAmount().subtract(totalShares);

            for (int i = 0; i < members.size(); i++) {
                BigDecimal thisShare = shareAmount;
                if (i == 0 && remainder.compareTo(BigDecimal.ZERO) != 0) {
                    thisShare = thisShare.add(remainder);
                }
                ExpenseShare share = shareRepository.save(new ExpenseShare(expense, members.get(i).getUser(), thisShare));
                expense.getShares().add(share);
            }
        }

        activityService.logActivity(groupId, String.format("%s added an expense: '%s' (₹%s)", paidBy.getName(), expense.getDescription(), expense.getAmount()));

        return expense;
    }

    public List<Expense> getGroupExpenses(Long groupId) {
        verifyMembership(groupId);
        groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        return expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
    }

    public org.springframework.data.domain.Page<Expense> getGroupExpensesPaginated(Long groupId, String search, org.springframework.data.domain.Pageable pageable) {
        verifyMembership(groupId);
        if (search != null && !search.trim().isEmpty()) {
            return expenseRepository.findByGroupIdAndDescriptionContainingIgnoreCase(groupId, search.trim(), pageable);
        } else {
            return expenseRepository.findByGroupId(groupId, pageable);
        }
    }

    public void deleteExpense(Long groupId, Long expenseId) {
        verifyMembership(groupId);
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found with id: " + expenseId));
        if (!expense.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Expense does not belong to the specified group");
        }
        
        String desc = expense.getDescription();
        String amt = expense.getAmount().toString();
        
        expenseRepository.delete(expense);
        
        // Use SecurityUtils or user info if we had current user, but simple message is fine
        activityService.logActivity(groupId, String.format("An expense was deleted: '%s' (₹%s)", desc, amt));
    }

    private void verifyMembership(Long groupId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!memberRepository.existsByGroupIdAndUserId(groupId, currentUserId)) {
            throw new SecurityException("Access denied: You are not a member of this group");
        }
    }
}
