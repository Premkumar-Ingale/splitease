package com.splitease.service;

import com.splitease.dto.AddMemberRequest;
import com.splitease.dto.CreateGroupRequest;
import com.splitease.model.ExpenseGroup;
import com.splitease.model.GroupMember;
import com.splitease.model.User;
import com.splitease.repository.ExpenseGroupRepository;
import com.splitease.repository.GroupMemberRepository;
import com.splitease.repository.UserRepository;
import com.splitease.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupService {

    private final ExpenseGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    public GroupService(ExpenseGroupRepository groupRepository,
                        GroupMemberRepository memberRepository,
                        UserRepository userRepository,
                        ActivityService activityService) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.activityService = activityService;
    }

    @Transactional
    public ExpenseGroup createGroup(CreateGroupRequest request) {
        ExpenseGroup group = new ExpenseGroup(request.getName());
        group = groupRepository.save(group);
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        
        // Automatically add creator as a member
        GroupMember creatorMember = new GroupMember(group, currentUser);
        memberRepository.save(creatorMember);
        
        activityService.logActivity(group.getId(), String.format("Group '%s' was created.", request.getName()));
        
        return group;
    }

    public ExpenseGroup getGroupById(Long id) {
        verifyMembership(id);
        return groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + id));
    }

    public List<ExpenseGroup> getAllGroups() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return groupRepository.findByUserId(currentUserId);
    }

    @Transactional
    public GroupMember addMember(Long groupId, AddMemberRequest request) {
        verifyMembership(groupId);
        ExpenseGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getUserId()));

        if (memberRepository.existsByGroupIdAndUserId(groupId, request.getUserId())) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        GroupMember member = new GroupMember(group, user);
        memberRepository.save(member);
        
        activityService.logActivity(groupId, String.format("%s was added to the group.", user.getName()));
        return member;
    }

    public List<GroupMember> getGroupMembers(Long groupId) {
        verifyMembership(groupId);
        return memberRepository.findByGroupId(groupId);
    }

    private void verifyMembership(Long groupId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!memberRepository.existsByGroupIdAndUserId(groupId, currentUserId)) {
            throw new SecurityException("Access denied: You are not a member of this group");
        }
    }
}
