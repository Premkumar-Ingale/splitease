package com.splitease.service;

import com.splitease.dto.CreateUserRequest;
import com.splitease.model.User;
import com.splitease.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Create new user successfully")
    void createUser_validRequest_createsUser() {
        CreateUserRequest req = new CreateUserRequest();
        req.setName("Test User");
        req.setEmail("testuser@example.com");
        req.setPassword("password123");

        User user = userService.createUser(req);

        assertNotNull(user.getId());
        assertEquals("Test User", user.getName());
        assertEquals("testuser@example.com", user.getEmail());
        assertNotNull(user.getPassword());
    }

    @Test
    @DisplayName("Creating user with existing email throws exception")
    void createUser_existingEmail_throwsException() {
        CreateUserRequest req = new CreateUserRequest();
        req.setName("Duplicate User");
        req.setEmail("aarav@splitease.com"); // Already exists in seed data
        req.setPassword("password123");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(req)
        );
        assertEquals("Email already exists: aarav@splitease.com", ex.getMessage());
    }

    @Test
    @DisplayName("Get user by ID returns correct user")
    void getUserById_existingId_returnsUser() {
        User user = userService.getUserById(1L);

        assertNotNull(user);
        assertEquals("Aarav", user.getName());
        assertEquals("aarav@splitease.com", user.getEmail());
    }
}
