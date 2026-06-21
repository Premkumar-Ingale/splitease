package com.splitease.repository;

import com.splitease.model.Settlement;
import com.splitease.model.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByGroupId(Long groupId);
    List<Settlement> findByGroupIdAndStatus(Long groupId, SettlementStatus status);
    void deleteByGroupIdAndStatus(Long groupId, SettlementStatus status);
}
