package com.splitease.service;

import com.splitease.dto.SettlementResponse;
import com.splitease.model.User;
import com.splitease.repository.UserRepository;
import com.splitease.repository.GroupMemberRepository;
import com.splitease.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Exact Minimum Settlement via DFS/Backtracking.
 *
 * This solves the "Optimal Account Balancing" problem (LeetCode 465) exactly
 * by exploring all possible ways to settle debts and finding the one with
 * the fewest transactions.
 *
 * Approach:
 * 1. Compute net balances and filter out zero-balance members.
 * 2. Use recursive DFS with backtracking: for each debtor, try settling
 *    with every creditor (or vice versa), then recurse on the remaining.
 * 3. Prune branches that can't beat the current best solution.
 *
 * Complexity: O(n!) in the worst case — exponential, so this is only
 * suitable for small groups (≤ 8 members with non-zero balances).
 * For larger groups, fall back to the greedy heap approach.
 *
 * Why this matters: Finding the true minimum number of transactions is
 * NP-hard (reducible to subset sum). Being able to explain BOTH the
 * exact and approximate solutions demonstrates real algorithmic depth.
 */
@Service
public class ExactSettlementService {

    private static final int MAX_EXACT_SIZE = 8;

    private final BalanceService balanceService;
    private final UserRepository userRepository;
    private final GroupMemberRepository memberRepository;

    public ExactSettlementService(BalanceService balanceService, UserRepository userRepository, GroupMemberRepository memberRepository) {
        this.balanceService = balanceService;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * Computes the exact minimum settlement using DFS/backtracking.
     * Falls back to greedy if the group is too large.
     */
    public Map<String, Object> computeExactSettlements(Long groupId) {
        verifyMembership(groupId);
        Map<Long, BigDecimal> rawBalances = balanceService.getRawBalances(groupId);

        // Build user name lookup
        Map<Long, String> userNames = new HashMap<>();
        for (Long userId : rawBalances.keySet()) {
            userRepository.findById(userId)
                    .ifPresent(u -> userNames.put(u.getId(), u.getName()));
        }

        // Convert to cents (long) and filter non-zero balances
        List<Long> userIds = new ArrayList<>();
        List<Long> balanceCents = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : rawBalances.entrySet()) {
            long cents = entry.getValue().multiply(BigDecimal.valueOf(100)).longValue();
            if (cents != 0) {
                userIds.add(entry.getKey());
                balanceCents.add(cents);
            }
        }

        int n = balanceCents.size();
        Map<String, Object> result = new LinkedHashMap<>();

        if (n == 0) {
            result.put("algorithm", "exact_dfs");
            result.put("memberCount", rawBalances.size());
            result.put("nonZeroCount", 0);
            result.put("transactionCount", 0);
            result.put("settlements", Collections.emptyList());
            result.put("feasible", true);
            return result;
        }

        boolean feasible = n <= MAX_EXACT_SIZE;
        result.put("algorithm", "exact_dfs");
        result.put("memberCount", rawBalances.size());
        result.put("nonZeroCount", n);
        result.put("feasible", feasible);
        result.put("maxExactSize", MAX_EXACT_SIZE);

        if (!feasible) {
            result.put("message", "Group too large for exact solution (" + n +
                    " non-zero balances, max " + MAX_EXACT_SIZE +
                    "). Use the greedy algorithm instead.");
            result.put("transactionCount", 0);
            result.put("settlements", Collections.emptyList());
            return result;
        }

        // Run DFS/Backtracking
        long[] bals = new long[n];
        for (int i = 0; i < n; i++) bals[i] = balanceCents.get(i);

        DfsState state = new DfsState();
        state.bestCount = n; // worst case: n-1 transactions
        state.bestTransactions = new ArrayList<>();
        List<long[]> currentTransactions = new ArrayList<>();

        long startTime = System.nanoTime();
        dfs(bals, 0, 0, currentTransactions, state);
        long elapsed = System.nanoTime() - startTime;

        // Convert bestTransactions to SettlementResponse list
        List<SettlementResponse> settlements = new ArrayList<>();
        long idCounter = 0;
        for (long[] tx : state.bestTransactions) {
            idCounter++;
            int fromIdx = (int) tx[0]; // debtor index
            int toIdx = (int) tx[1];   // creditor index
            long amount = tx[2]; // cents

            settlements.add(new SettlementResponse(
                    idCounter,
                    userIds.get(fromIdx),
                    userNames.getOrDefault(userIds.get(fromIdx), "Unknown"),
                    userIds.get(toIdx),
                    userNames.getOrDefault(userIds.get(toIdx), "Unknown"),
                    BigDecimal.valueOf(Math.abs(amount), 2),
                    "EXACT"
            ));
        }

        result.put("transactionCount", settlements.size());
        result.put("settlements", settlements);
        result.put("computeTimeMs", elapsed / 1_000_000.0);

        return result;
    }

    private static class DfsState {
        int bestCount;
        List<long[]> bestTransactions;
    }

    /**
     * DFS with backtracking to find minimum transactions.
     *
     * Strategy: Find the first non-zero balance starting from index 'start'.
     * Then try settling it with every subsequent non-zero balance of opposite sign.
     * After settling, recurse. Backtrack and try the next pairing.
     *
     * Pruning: if current count >= bestCount, stop exploring this branch.
     */
    private void dfs(long[] bals, int start, int count, List<long[]> transactions, DfsState state) {
        // Pruning: can't do better than current best
        if (count >= state.bestCount) return;

        // Find the first non-zero balance from 'start'
        while (start < bals.length && bals[start] == 0) start++;

        // Base case: all balances are zero
        if (start == bals.length) {
            if (count < state.bestCount) {
                state.bestCount = count;
                state.bestTransactions = new ArrayList<>(transactions);
            }
            return;
        }

        // Try settling bals[start] with each subsequent person
        for (int i = start + 1; i < bals.length; i++) {
            // Only settle between opposite signs (debtor ↔ creditor)
            if (bals[start] * bals[i] < 0) {
                // Settle: transfer bals[start] amount to i
                long amount = bals[start];
                bals[i] += bals[start];
                bals[start] = 0;

                // Record transaction
                int fromIdx, toIdx;
                long txAmount;
                if (amount < 0) {
                    // start is debtor, i is creditor
                    fromIdx = start;
                    toIdx = i;
                    txAmount = -amount;
                } else {
                    // start is creditor, i is debtor
                    fromIdx = i;
                    toIdx = start;
                    txAmount = amount;
                }
                transactions.add(new long[]{fromIdx, toIdx, txAmount});

                dfs(bals, start + 1, count + 1, transactions, state);

                // Backtrack
                transactions.remove(transactions.size() - 1);
                bals[start] = amount;
                bals[i] -= amount;

                // Optimization: if this settle exactly zeroed out bals[i],
                // this is an optimal match for this step — no need to try others
                if (bals[i] + amount == 0) break;
            }
        }
    }

    /**
     * Runs both algorithms and returns a comparison.
     */
    public Map<String, Object> compareAlgorithms(Long groupId, List<SettlementResponse> greedyResults) {
        verifyMembership(groupId);
        Map<String, Object> comparison = new LinkedHashMap<>();

        // Greedy results (passed in)
        long greedyStart = System.nanoTime();
        // Already computed — just measure serialization overhead
        long greedyTime = System.nanoTime() - greedyStart;

        Map<String, Object> greedyData = new LinkedHashMap<>();
        greedyData.put("algorithm", "greedy_heap");
        greedyData.put("complexity", "O(n log n)");
        greedyData.put("transactionCount", greedyResults.size());
        greedyData.put("settlements", greedyResults);
        greedyData.put("description", "Fast greedy approximation using two max-heaps. " +
                "Pairs the largest creditor with the largest debtor each iteration.");

        // Exact results
        Map<String, Object> exactData = computeExactSettlements(groupId);
        exactData.put("complexity", "O(n!) worst case");
        exactData.put("description", "Exact minimum via DFS/backtracking. " +
                "Explores all possible settlement orderings to find the provably optimal solution.");

        comparison.put("greedy", greedyData);
        comparison.put("exact", exactData);

        // Comparison summary
        int greedyCount = greedyResults.size();
        int exactCount = (int) exactData.get("transactionCount");
        boolean feasible = (boolean) exactData.get("feasible");

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("greedyTransactions", greedyCount);
        summary.put("exactTransactions", feasible ? exactCount : "N/A (group too large)");
        summary.put("isGreedyOptimal", feasible && greedyCount == exactCount);
        summary.put("savings", feasible ? (greedyCount - exactCount) : 0);

        if (feasible) {
            if (greedyCount == exactCount) {
                summary.put("verdict", "The greedy algorithm found the optimal solution! " +
                        "Both algorithms produce " + greedyCount + " transaction(s).");
            } else {
                summary.put("verdict", "The exact DFS found a better solution with " +
                        exactCount + " transaction(s) vs greedy's " + greedyCount +
                        ". This saves " + (greedyCount - exactCount) + " transaction(s).");
            }
        } else {
            summary.put("verdict", "Group is too large for exact computation. " +
                    "The greedy algorithm provides a near-optimal solution.");
        }

        comparison.put("summary", summary);
        return comparison;
    }

    private void verifyMembership(Long groupId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!memberRepository.existsByGroupIdAndUserId(groupId, currentUserId)) {
            throw new SecurityException("Access denied: You are not a member of this group");
        }
    }
}
