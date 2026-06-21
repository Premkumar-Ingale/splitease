package com.splitease.service;

import com.splitease.dto.BalanceResponse;
import com.splitease.model.Expense;
import com.splitease.model.ExpenseShare;
import com.splitease.model.GroupMember;
import com.splitease.model.User;
import com.splitease.repository.ExpenseRepository;
import com.splitease.repository.ExpenseShareRepository;
import com.splitease.repository.GroupMemberRepository;
import com.splitease.repository.SettlementRepository;
import com.splitease.model.Settlement;
import com.splitease.model.SettlementStatus;
import com.splitease.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class BalanceService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository shareRepository;
    private final GroupMemberRepository memberRepository;
    private final SettlementRepository settlementRepository;

    public BalanceService(ExpenseRepository expenseRepository,
                          ExpenseShareRepository shareRepository,
                          GroupMemberRepository memberRepository,
                          SettlementRepository settlementRepository) {
        this.expenseRepository = expenseRepository;
        this.shareRepository = shareRepository;
        this.memberRepository = memberRepository;
        this.settlementRepository = settlementRepository;
    }

    /**
     * Computes net balance per member in a group.
     * balance = total paid by user - total of user's shares across all expenses
     * Positive balance = creditor (group owes them money)
     * Negative balance = debtor (they owe the group money)
     */
    public List<BalanceResponse> getGroupBalances(Long groupId) {
        verifyMembership(groupId);
        List<GroupMember> members = memberRepository.findByGroupId(groupId);
        List<Expense> expenses = expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
        List<ExpenseShare> shares = shareRepository.findByExpenseGroupId(groupId);

        // HashMap keyed by userId — core DSA tie-in
        Map<Long, BigDecimal> balanceMap = new HashMap<>();
        Map<Long, String> userNameMap = new HashMap<>();

        // Initialize all members with zero balance
        for (GroupMember member : members) {
            User user = member.getUser();
            balanceMap.put(user.getId(), BigDecimal.ZERO);
            userNameMap.put(user.getId(), user.getName());
        }

        // Add amounts paid by each user
        for (Expense expense : expenses) {
            Long payerId = expense.getPaidBy().getId();
            balanceMap.merge(payerId, expense.getAmount(), BigDecimal::add);
        }

        // Subtract each user's share amounts
        for (ExpenseShare share : shares) {
            Long userId = share.getUser().getId();
            balanceMap.merge(userId, share.getShareAmount().negate(), BigDecimal::add);
        }

        // Adjust for actual payments made through settlements
        List<Settlement> paidSettlements = settlementRepository.findByGroupIdAndStatus(groupId, SettlementStatus.PAID);
        for (Settlement settlement : paidSettlements) {
            Long fromUserId = settlement.getFromUser().getId();
            Long toUserId = settlement.getToUser().getId();
            BigDecimal amount = settlement.getAmount();

            // The 'fromUser' paid money, so their balance goes up
            balanceMap.merge(fromUserId, amount, BigDecimal::add);
            // The 'toUser' received money, so their balance goes down
            balanceMap.merge(toUserId, amount.negate(), BigDecimal::add);
        }

        // Convert to response list
        List<BalanceResponse> balances = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : balanceMap.entrySet()) {
            balances.add(new BalanceResponse(
                    entry.getKey(),
                    userNameMap.get(entry.getKey()),
                    entry.getValue()
            ));
        }

        // Sort by balance descending (biggest creditors first)
        balances.sort((a, b) -> b.getBalance().compareTo(a.getBalance()));
        return balances;
    }

    /**
     * Returns the raw balance map for use by the settlement algorithm.
     */
    public Map<Long, BigDecimal> getRawBalances(Long groupId) {
        List<BalanceResponse> balances = getGroupBalances(groupId);
        Map<Long, BigDecimal> map = new HashMap<>();
        for (BalanceResponse b : balances) {
            map.put(b.getUserId(), b.getBalance());
        }
        return map;
    }

    private void verifyMembership(Long groupId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!memberRepository.existsByGroupIdAndUserId(groupId, currentUserId)) {
            throw new SecurityException("Access denied: You are not a member of this group");
        }
    }
}
