package com.paypal.wallet.dto;

public class UpdateProfileRequest {
    private String name;
    private String email;
    
    public UpdateProfileRequest() {}
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}