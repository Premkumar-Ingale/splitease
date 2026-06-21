package com.splitease.controller;

import com.splitease.dto.AddMemberRequest;
import com.splitease.dto.CreateGroupRequest;
import com.splitease.model.ExpenseGroup;
import com.splitease.model.GroupMember;
import com.splitease.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        ExpenseGroup group = groupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(groupToMap(group));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllGroups() {
        List<ExpenseGroup> groups = groupService.getAllGroups();
        List<Map<String, Object>> result = new ArrayList<>();
        for (ExpenseGroup g : groups) {
            result.add(groupToMap(g));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getGroupById(@PathVariable Long id) {
        ExpenseGroup group = groupService.getGroupById(id);
        Map<String, Object> result = groupToMap(group);
        // Include members
        List<GroupMember> members = groupService.getGroupMembers(id);
        List<Map<String, Object>> memberList = new ArrayList<>();
        for (GroupMember m : members) {
            Map<String, Object> memberMap = new LinkedHashMap<>();
            memberMap.put("id", m.getId());
            memberMap.put("userId", m.getUser().getId());
            memberMap.put("userName", m.getUser().getName());
            memberMap.put("userEmail", m.getUser().getEmail());
            memberList.add(memberMap);
        }
        result.put("members", memberList);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Map<String, Object>> addMember(@PathVariable Long id,
                                                          @Valid @RequestBody AddMemberRequest request) {
        GroupMember member = groupService.addMember(id, request);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", member.getId());
        result.put("groupId", id);
        result.put("userId", member.getUser().getId());
        result.put("userName", member.getUser().getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<Map<String, Object>>> getMembers(@PathVariable Long id) {
        List<GroupMember> members = groupService.getGroupMembers(id);
        List<Map<String, Object>> result = new ArrayList<>();
        for (GroupMember m : members) {
            Map<String, Object> memberMap = new LinkedHashMap<>();
            memberMap.put("id", m.getId());
            memberMap.put("userId", m.getUser().getId());
            memberMap.put("userName", m.getUser().getName());
            memberMap.put("userEmail", m.getUser().getEmail());
            result.add(memberMap);
        }
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> groupToMap(ExpenseGroup group) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", group.getId());
        map.put("name", group.getName());
        return map;
    }
}
