package com.splitease.dto;

import java.math.BigDecimal;

public class BalanceResponse {
    private Long userId;
    private String userName;
    private BigDecimal balance;

    public BalanceResponse() {}

    public BalanceResponse(Long userId, String userName, BigDecimal balance) {
        this.userId = userId;
        this.userName = userName;
        this.balance = balance;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
