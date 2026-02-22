package com.paypal.wallet.dto;

import java.time.LocalDateTime;

public class TransactionDTO {
    private Long id;
    private String senderName;
    private String senderEmail;
    private String receiverName;
    private String receiverEmail;
    private Double amount;
    private String description;
    private String status;
    private LocalDateTime transactionDate;
    
    // Constructor
    public TransactionDTO(Long id, String senderName, String senderEmail, 
                         String receiverName, String receiverEmail, 
                         Double amount, String description, 
                         String status, LocalDateTime transactionDate) {
        this.id = id;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.receiverName = receiverName;
        this.receiverEmail = receiverEmail;
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.transactionDate = transactionDate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    
    public String getReceiverEmail() { return receiverEmail; }
    public void setReceiverEmail(String receiverEmail) { this.receiverEmail = receiverEmail; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { 
        this.transactionDate = transactionDate; 
    }
}