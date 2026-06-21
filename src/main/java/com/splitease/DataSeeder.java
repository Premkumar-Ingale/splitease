package com.splitease;

import com.splitease.dto.AddMemberRequest;
import com.splitease.dto.CreateExpenseRequest;
import com.splitease.dto.CreateGroupRequest;
import com.splitease.dto.CreateUserRequest;
import com.splitease.service.ExpenseService;
import com.splitease.service.GroupService;
import com.splitease.repository.UserRepository;
import com.splitease.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds the database with realistic demo data on startup.
 * 4 users, 1 group ("Room 404"), and 6 categorized expenses.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final GroupService groupService;
    private final ExpenseService expenseService;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, UserService userService, GroupService groupService, ExpenseService expenseService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.expenseService = expenseService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            System.out.println("✅ Seed data already present.");
            return;
        }

        String encodedPassword = passwordEncoder.encode("password123");

        // Create 4 users
        CreateUserRequest u1 = new CreateUserRequest();
        u1.setName("Aarav");
        u1.setEmail("aarav@splitease.com");
        u1.setPassword(encodedPassword);

        CreateUserRequest u2 = new CreateUserRequest();
        u2.setName("Meera");
        u2.setEmail("meera@splitease.com");
        u2.setPassword(encodedPassword);

        CreateUserRequest u3 = new CreateUserRequest();
        u3.setName("Rohan");
        u3.setEmail("rohan@splitease.com");
        u3.setPassword(encodedPassword);

        CreateUserRequest u4 = new CreateUserRequest();
        u4.setName("Priya");
        u4.setEmail("priya@example.com");
        u4.setPassword(encodedPassword);

        var aarav = userService.createUser(u1);
        var meera = userService.createUser(u2);
        var rohan = userService.createUser(u3);
        var priya = userService.createUser(u4);

        // Authenticate as Aarav to bypass authorization checks during seeding
        com.splitease.security.CustomUserDetails details = new com.splitease.security.CustomUserDetails(aarav);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(details, null, details.getAuthorities())
        );

        // Create a group
        CreateGroupRequest groupReq = new CreateGroupRequest();
        groupReq.setName("Room 404");
        var group = groupService.createGroup(groupReq);

        // Aarav is already added to group, add the rest
        for (var user : new Object[]{meera, rohan, priya}) {
            AddMemberRequest memberReq = new AddMemberRequest();
            memberReq.setUserId(((com.splitease.model.User) user).getId());
            groupService.addMember(group.getId(), memberReq);
        }

        // Add 6 realistic categorized expenses
        addExpense(group.getId(), aarav.getId(), "1200.00", "Monthly Groceries", "FOOD");
        addExpense(group.getId(), meera.getId(), "800.00", "Electricity Bill", "BILLS");
        addExpense(group.getId(), rohan.getId(), "2000.00", "Weekend Trip to Goa", "TRAVEL");
        addExpense(group.getId(), priya.getId(), "450.00", "Netflix & Hotstar", "ENTERTAINMENT");
        addExpense(group.getId(), aarav.getId(), "1500.00", "Room Supplies", "SHOPPING");
        addExpense(group.getId(), meera.getId(), "600.00", "Water & WiFi Bill", "BILLS");

        System.out.println("✅ Seed data loaded: 4 users, 1 group, 6 categorized expenses");
    }

    private void addExpense(Long groupId, Long paidBy, String amount, String desc, String category) {
        CreateExpenseRequest req = new CreateExpenseRequest();
        req.setPaidByUserId(paidBy);
        req.setAmount(new BigDecimal(amount));
        req.setDescription(desc);
        req.setCategory(category);
        expenseService.addExpense(groupId, req);
    }
}
