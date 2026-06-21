package com.splitease.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AnalyticsServiceTest {

    @Autowired
    private AnalyticsService analyticsService;

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
    @DisplayName("Get group analytics returns valid data")
    void getGroupAnalytics_validGroupId_returnsValidData() {
        Map<String, Object> analytics = analyticsService.getGroupAnalytics(1L);

        assertNotNull(analytics.get("categoryBreakdown"));
        assertNotNull(analytics.get("totalExpenses"));
        assertNotNull(analytics.get("totalAmount"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categories = (List<Map<String, Object>>) analytics.get("categoryBreakdown");
        assertFalse(categories.isEmpty());

        BigDecimal total = (BigDecimal) analytics.get("totalAmount");
        assertTrue(total.compareTo(BigDecimal.ZERO) > 0);
    }
}
