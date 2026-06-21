package com.splitease.dto;

import java.time.LocalDateTime;

public class ActivityResponse {
    private Long id;
    private String message;
    private LocalDateTime timestamp;

    public ActivityResponse(Long id, String message, LocalDateTime timestamp) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
