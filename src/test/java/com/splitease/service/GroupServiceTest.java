package com.splitease.service;

import com.splitease.dto.AddMemberRequest;
import com.splitease.dto.CreateGroupRequest;
import com.splitease.model.ExpenseGroup;
import com.splitease.model.GroupMember;
import com.splitease.repository.ExpenseGroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class GroupServiceTest {

    @Autowired
    private GroupService groupService;

    @Autowired
    private ExpenseGroupRepository expenseGroupRepository;

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
    @DisplayName("Create new group successfully")
    void createGroup_validRequest_createsGroup() {
        CreateGroupRequest req = new CreateGroupRequest();
        req.setName("Test Group 2");

        ExpenseGroup group = groupService.createGroup(req);

        assertNotNull(group.getId());
        assertEquals("Test Group 2", group.getName());
    }

    @Test
    @DisplayName("Get group by ID returns correct group")
    void getGroupById_existingId_returnsGroup() {
        ExpenseGroup group = groupService.getGroupById(1L);

        assertNotNull(group);
        assertEquals("Room 404", group.getName());
    }

    @Test
    @DisplayName("Add member to group successfully")
    void addMember_validRequest_addsMember() {
        CreateGroupRequest newGroupReq = new CreateGroupRequest();
        newGroupReq.setName("Test Add Member");
        ExpenseGroup newGroup = groupService.createGroup(newGroupReq);

        AddMemberRequest addMemberReq = new AddMemberRequest();
        addMemberReq.setUserId(2L); // Add Meera instead of Aarav

        groupService.addMember(newGroup.getId(), addMemberReq);

        List<GroupMember> members = groupService.getGroupMembers(newGroup.getId());
        assertEquals(2, members.size()); // Creator (1L) + New Member (2L)
        assertTrue(members.stream()
                .anyMatch(m -> m.getUser().getId().equals(2L)));
    }
}
