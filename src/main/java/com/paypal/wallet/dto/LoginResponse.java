package com.paypal.wallet.dto;

public class LoginResponse {
    private String token;
    private String email;
    private String name;
    private String role;
    private Double balance;
    
    public LoginResponse(String token, String email, String name, String role, Double balance) {
        this.token = token;
        this.email = email;
        this.name = name;
        this.role = role;
        this.balance = balance;
    }
    
    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
}