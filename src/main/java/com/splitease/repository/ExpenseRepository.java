package com.splitease.repository;

import com.splitease.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    Page<Expense> findByGroupId(Long groupId, Pageable pageable);

    Page<Expense> findByGroupIdAndDescriptionContainingIgnoreCase(Long groupId, String search, Pageable pageable);
}
