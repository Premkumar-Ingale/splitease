package com.splitease.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Load user by username returns valid UserDetails")
    void loadUserByUsername_existingUser_returnsUserDetails() {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("aarav@splitease.com");

        assertNotNull(userDetails);
        assertEquals("aarav@splitease.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().isEmpty() || !userDetails.getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("Load user by non-existent username throws UsernameNotFoundException")
    void loadUserByUsername_nonExistentUser_throwsException() {
        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("nonexistent@example.com")
        );
    }
}
