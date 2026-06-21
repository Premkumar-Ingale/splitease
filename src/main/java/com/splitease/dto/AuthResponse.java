package com.splitease.dto;

public class AuthResponse {
    private String token;
    private Long userId;
    private String name;

    public AuthResponse(String token, Long userId, String name) {
        this.token = token;
        this.userId = userId;
        this.name = name;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
