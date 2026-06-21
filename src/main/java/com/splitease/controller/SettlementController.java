package com.splitease.controller;

import com.splitease.dto.BalanceResponse;
import com.splitease.dto.SettlementResponse;
import com.splitease.model.Settlement;
import com.splitease.service.BalanceService;
import com.splitease.service.ExactSettlementService;
import com.splitease.service.SettlementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class SettlementController {

    private final BalanceService balanceService;
    private final SettlementService settlementService;
    private final ExactSettlementService exactSettlementService;

    public SettlementController(BalanceService balanceService,
                                SettlementService settlementService,
                                ExactSettlementService exactSettlementService) {
        this.balanceService = balanceService;
        this.settlementService = settlementService;
        this.exactSettlementService = exactSettlementService;
    }

    /**
     * GET /api/groups/{groupId}/balances
     * Returns net balance per member in the group.
     */
    @GetMapping("/groups/{groupId}/balances")
    public ResponseEntity<List<BalanceResponse>> getGroupBalances(@PathVariable Long groupId) {
        return ResponseEntity.ok(balanceService.getGroupBalances(groupId));
    }

    /**
     * GET /api/groups/{groupId}/settlements
     * Computes the minimum transaction settlement plan (not stored).
     */
    @GetMapping("/groups/{groupId}/settlements")
    public ResponseEntity<List<SettlementResponse>> computeSettlements(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementService.computeSettlements(groupId));
    }

    /**
     * POST /api/groups/{groupId}/settlements/confirm
     * Saves the computed settlement plan as PENDING settlements.
     */
    @PostMapping("/groups/{groupId}/settlements/confirm")
    public ResponseEntity<List<Map<String, Object>>> confirmSettlements(@PathVariable Long groupId) {
        List<Settlement> settlements = settlementService.confirmSettlements(groupId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Settlement s : settlements) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", s.getId());
            map.put("fromUserId", s.getFromUser().getId());
            map.put("fromUserName", s.getFromUser().getName());
            map.put("toUserId", s.getToUser().getId());
            map.put("toUserName", s.getToUser().getName());
            map.put("amount", s.getAmount());
            map.put("status", s.getStatus().name());
            result.add(map);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * POST /api/settlements/{id}/pay
     * Marks a specific settlement as PAID.
     */
    @PostMapping("/settlements/{id}/pay")
    public ResponseEntity<Map<String, Object>> markAsPaid(@PathVariable Long id) {
        Settlement settlement = settlementService.markAsPaid(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", settlement.getId());
        result.put("fromUserId", settlement.getFromUser().getId());
        result.put("fromUserName", settlement.getFromUser().getName());
        result.put("toUserId", settlement.getToUser().getId());
        result.put("toUserName", settlement.getToUser().getName());
        result.put("amount", settlement.getAmount());
        result.put("status", settlement.getStatus().name());
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/groups/{groupId}/settlements/confirmed
     * Returns the stored/confirmed settlements for a group.
     */
    @GetMapping("/groups/{groupId}/settlements/confirmed")
    public ResponseEntity<List<SettlementResponse>> getConfirmedSettlements(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementService.getGroupSettlements(groupId));
    }

    /**
     * GET /api/groups/{groupId}/settlements/exact
     * Computes the exact minimum settlement via DFS/backtracking.
     */
    @GetMapping("/groups/{groupId}/settlements/exact")
    public ResponseEntity<Map<String, Object>> computeExactSettlements(@PathVariable Long groupId) {
        return ResponseEntity.ok(exactSettlementService.computeExactSettlements(groupId));
    }

    /**
     * GET /api/groups/{groupId}/settlements/compare
     * Runs both greedy and exact algorithms and returns a side-by-side comparison.
     */
    @GetMapping("/groups/{groupId}/settlements/compare")
    public ResponseEntity<Map<String, Object>> compareAlgorithms(@PathVariable Long groupId) {
        List<SettlementResponse> greedyResults = settlementService.computeSettlements(groupId);
        Map<String, Object> comparison = exactSettlementService.compareAlgorithms(groupId, greedyResults);
        return ResponseEntity.ok(comparison);
    }
}
