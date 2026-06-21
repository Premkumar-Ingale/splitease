package com.splitease.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

public class CreateExpenseRequest {

    @NotNull(message = "Paid by user ID is required")
    private Long paidByUserId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    private String description;

    private String category; // optional, defaults to OTHER

    private String splitType; // EQUAL (default), EXACT, PERCENTAGE

    private Map<Long, BigDecimal> splits; // user ID -> exact amount or percentage

    public Long getPaidByUserId() { return paidByUserId; }
    public void setPaidByUserId(Long paidByUserId) { this.paidByUserId = paidByUserId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSplitType() { return splitType; }
    public void setSplitType(String splitType) { this.splitType = splitType; }
    public Map<Long, BigDecimal> getSplits() { return splits; }
    public void setSplits(Map<Long, BigDecimal> splits) { this.splits = splits; }
}
