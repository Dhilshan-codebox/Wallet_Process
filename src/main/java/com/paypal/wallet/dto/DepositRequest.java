package com.paypal.wallet.dto;

public class DepositRequest {
    private Double amount;
    private String description;
    
    public DepositRequest() {}
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}