package com.paypal.wallet.dto;

public class TransactionRequest {
    
    private Long senderId;
    private Long receiverId;
    private Double amount;
    private String description;
    
    // Constructors
    public TransactionRequest() {}
    
    // Getters and Setters
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}