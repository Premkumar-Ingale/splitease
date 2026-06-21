package com.splitease.repository;

import com.splitease.model.ExpenseGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseGroupRepository extends JpaRepository<ExpenseGroup, Long> {
    
    @Query("SELECT gm.group FROM GroupMember gm WHERE gm.user.id = :userId")
    List<ExpenseGroup> findByUserId(@Param("userId") Long userId);
}
