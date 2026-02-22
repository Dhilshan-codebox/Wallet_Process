package com.paypal.wallet.dto;

public class UserProfileDTO {
    private Long id;
    private String name;
    private String email;
    private Double balance;
    private String role;
    
    public UserProfileDTO(Long id, String name, String email, Double balance, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.balance = balance;
        this.role = role;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}