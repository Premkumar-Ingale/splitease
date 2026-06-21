package com.splitease.service;

import com.splitease.dto.SettlementResponse;
import com.splitease.model.*;
import com.splitease.repository.ExpenseGroupRepository;
import com.splitease.repository.SettlementRepository;
import com.splitease.repository.UserRepository;
import com.splitease.repository.GroupMemberRepository;
import com.splitease.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Core algorithm — Minimum Settlement using Greedy Two-Max-Heap approach.
 *
 * Complexity: O(n log n) for n members.
 * Each member is pushed/popped a small constant number of times.
 *
 * The greedy approach pairs the biggest creditor with the biggest debtor
 * each iteration, clearing at least one person's balance to zero per step,
 * which keeps the transaction count low.
 *
 * Note: Finding the mathematically minimum number of transactions is NP-hard
 * (Optimal Account Balancing / LeetCode 465). This greedy heap approach
 * is a fast, near-optimal approximation perfect for real-world group sizes.
 */
@Service
public class SettlementService {

    private final BalanceService balanceService;
    private final SettlementRepository settlementRepository;
    private final ExpenseGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository memberRepository;
    private final ActivityService activityService;

    public SettlementService(BalanceService balanceService,
                             SettlementRepository settlementRepository,
                             ExpenseGroupRepository groupRepository,
                             UserRepository userRepository,
                             GroupMemberRepository memberRepository,
                             ActivityService activityService) {
        this.balanceService = balanceService;
        this.settlementRepository = settlementRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.activityService = activityService;
    }

    /**
     * Computes the minimum settlement plan for a group using two max-heaps.
     * This is computed on-the-fly, not stored until confirmed.
     */
    public List<SettlementResponse> computeSettlements(Long groupId) {
        verifyMembership(groupId);
        Map<Long, BigDecimal> balances = balanceService.getRawBalances(groupId);

        // Step 1: Separate creditors and debtors
        // Max-heap for creditors (highest balance first)
        PriorityQueue<long[]> creditorHeap = new PriorityQueue<>(
                (a, b) -> Long.compare(b[1], a[1])
        );
        // Max-heap for debtors (highest absolute balance first)
        PriorityQueue<long[]> debtorHeap = new PriorityQueue<>(
                (a, b) -> Long.compare(b[1], a[1])
        );

        // We use long representation (cents) to avoid floating point issues
        Map<Long, String> userNames = new HashMap<>();
        for (Map.Entry<Long, BigDecimal> entry : balances.entrySet()) {
            long cents = entry.getValue().multiply(BigDecimal.valueOf(100)).longValue();
            if (cents > 0) {
                creditorHeap.offer(new long[]{entry.getKey(), cents});
            } else if (cents < 0) {
                debtorHeap.offer(new long[]{entry.getKey(), -cents}); // store as positive
            }
            // Load user names
            userRepository.findById(entry.getKey())
                    .ifPresent(u -> userNames.put(u.getId(), u.getName()));
        }

        // Step 2: Greedy settlement — match biggest creditor with biggest debtor
        List<SettlementResponse> settlements = new ArrayList<>();
        long idCounter = 0;

        while (!creditorHeap.isEmpty() && !debtorHeap.isEmpty()) {
            long[] creditor = creditorHeap.poll();
            long[] debtor = debtorHeap.poll();

            long settleAmount = Math.min(creditor[1], debtor[1]);

            // Record transaction: debtor pays creditor
            idCounter++;
            settlements.add(new SettlementResponse(
                    idCounter,
                    debtor[0],  // fromUserId
                    userNames.getOrDefault(debtor[0], "Unknown"),
                    creditor[0], // toUserId
                    userNames.getOrDefault(creditor[0], "Unknown"),
                    BigDecimal.valueOf(settleAmount, 2), // convert cents back to decimal
                    "COMPUTED"
            ));

            // Subtract settled amount from both
            creditor[1] -= settleAmount;
            debtor[1] -= settleAmount;

            // Push back if residual balance remains
            if (creditor[1] > 0) {
                creditorHeap.offer(creditor);
            }
            if (debtor[1] > 0) {
                debtorHeap.offer(debtor);
            }
        }

        return settlements;
    }

    /**
     * Saves the computed settlement plan as PENDING settlements in the database.
     */
    @Transactional
    public List<Settlement> confirmSettlements(Long groupId) {
        verifyMembership(groupId);
        ExpenseGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));

        // Delete any existing pending settlements for this group
        settlementRepository.deleteByGroupIdAndStatus(groupId, SettlementStatus.PENDING);

        // Compute fresh settlements
        List<SettlementResponse> computed = computeSettlements(groupId);

        List<Settlement> savedSettlements = new ArrayList<>();
        for (SettlementResponse sr : computed) {
            User fromUser = userRepository.findById(sr.getFromUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            User toUser = userRepository.findById(sr.getToUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Settlement settlement = new Settlement(group, fromUser, toUser, sr.getAmount());
            Settlement savedSettlement = settlementRepository.save(settlement);
            savedSettlements.add(savedSettlement);
        }

        if (!savedSettlements.isEmpty()) {
            activityService.logActivity(groupId, "A new settlement plan was confirmed and saved.");
        }

        return savedSettlements;
    }

    /**
     * Marks a specific settlement as PAID.
     */
    @Transactional
    public Settlement markAsPaid(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found with id: " + settlementId));

        verifyMembership(settlement.getGroup().getId());

        if (settlement.getStatus() == SettlementStatus.PAID) {
            throw new IllegalArgumentException("Settlement is already marked as paid");
        }

        settlement.setStatus(SettlementStatus.PAID);
        Settlement saved = settlementRepository.save(settlement);

        activityService.logActivity(settlement.getGroup().getId(), 
            String.format("%s paid %s ₹%s to settle their debt.", 
                settlement.getFromUser().getName(), 
                settlement.getToUser().getName(), 
                settlement.getAmount()));
                
        return saved;
    }

    /**
     * Get all settlements for a group (stored/confirmed ones).
     */
    public List<SettlementResponse> getGroupSettlements(Long groupId) {
        verifyMembership(groupId);
        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);
        List<SettlementResponse> responses = new ArrayList<>();
        for (Settlement s : settlements) {
            responses.add(new SettlementResponse(
                    s.getId(),
                    s.getFromUser().getId(),
                    s.getFromUser().getName(),
                    s.getToUser().getId(),
                    s.getToUser().getName(),
                    s.getAmount(),
                    s.getStatus().name()
            ));
        }
        return responses;
    }

    private void verifyMembership(Long groupId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!memberRepository.existsByGroupIdAndUserId(groupId, currentUserId)) {
            throw new SecurityException("Access denied: You are not a member of this group");
        }
    }
}
