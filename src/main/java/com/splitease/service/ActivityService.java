package com.splitease.service;

import com.splitease.dto.ActivityResponse;
import com.splitease.model.ActivityLog;
import com.splitease.model.ExpenseGroup;
import com.splitease.repository.ActivityLogRepository;
import com.splitease.repository.ExpenseGroupRepository;
import com.splitease.repository.GroupMemberRepository;
import com.splitease.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;
    private final ExpenseGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;

    public ActivityService(ActivityLogRepository activityLogRepository,
                           ExpenseGroupRepository groupRepository,
                           GroupMemberRepository memberRepository) {
        this.activityLogRepository = activityLogRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void logActivity(Long groupId, String message) {
        ExpenseGroup group = groupRepository.findById(groupId).orElse(null);
        if (group != null) {
            ActivityLog log = new ActivityLog(group, message);
            activityLogRepository.save(log);
        }
    }

    public List<ActivityResponse> getGroupActivity(Long groupId) {
        verifyMembership(groupId);
        List<ActivityLog> logs = activityLogRepository.findByGroupIdOrderByTimestampDesc(groupId);
        return logs.stream()
                .map(log -> new ActivityResponse(log.getId(), log.getMessage(), log.getTimestamp()))
                .collect(Collectors.toList());
    }

    private void verifyMembership(Long groupId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!memberRepository.existsByGroupIdAndUserId(groupId, currentUserId)) {
            throw new SecurityException("Access denied: You are not a member of this group");
        }
    }
}
