package com.paypal.wallet.dto;

public class AdminStatsDTO {
    private Long totalUsers;
    private Long totalTransactions;
    private Double totalTransactionAmount;
    private Long successfulTransactions;
    private Long failedTransactions;
    private Long pendingTransactions;
    
    public AdminStatsDTO() {}
    
    public AdminStatsDTO(Long totalUsers, Long totalTransactions, 
                        Double totalTransactionAmount, Long successfulTransactions,
                        Long failedTransactions, Long pendingTransactions) {
        this.totalUsers = totalUsers;
        this.totalTransactions = totalTransactions;
        this.totalTransactionAmount = totalTransactionAmount;
        this.successfulTransactions = successfulTransactions;
        this.failedTransactions = failedTransactions;
        this.pendingTransactions = pendingTransactions;
    }
    
    // Getters and Setters
    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
    
    public Long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Long totalTransactions) { 
        this.totalTransactions = totalTransactions; 
    }
    
    public Double getTotalTransactionAmount() { return totalTransactionAmount; }
    public void setTotalTransactionAmount(Double totalTransactionAmount) { 
        this.totalTransactionAmount = totalTransactionAmount; 
    }
    
    public Long getSuccessfulTransactions() { return successfulTransactions; }
    public void setSuccessfulTransactions(Long successfulTransactions) { 
        this.successfulTransactions = successfulTransactions; 
    }
    
    public Long getFailedTransactions() { return failedTransactions; }
    public void setFailedTransactions(Long failedTransactions) { 
        this.failedTransactions = failedTransactions; 
    }
    
    public Long getPendingTransactions() { return pendingTransactions; }
    public void setPendingTransactions(Long pendingTransactions) { 
        this.pendingTransactions = pendingTransactions; 
    }
}