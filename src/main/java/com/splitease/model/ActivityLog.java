package com.splitease.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ExpenseGroup group;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public ActivityLog() {}

    public ActivityLog(ExpenseGroup group, String message) {
        this.group = group;
        this.message = message;
    }

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public ExpenseGroup getGroup() { return group; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
