package com.splitease.controller;

import com.splitease.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/groups/{groupId}/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getGroupAnalytics(@PathVariable Long groupId) {
        return ResponseEntity.ok(analyticsService.getGroupAnalytics(groupId));
    }
}
