package com.paypal.wallet.dto;

public class LoginResponse {

    private String token;
    private String email;
    private String name;
    private String role;
    private Double balance;

    // 2FA fields
    private boolean requiresOtp;
    private boolean twoFactorEnabled;

    // Phase 3 fields
    private String currency;
    private String walletAddress;

    public LoginResponse() {
    }

    // Full constructor
    public LoginResponse(String token, String email, String name, String role, Double balance,
            boolean requiresOtp, boolean twoFactorEnabled,
            String currency, String walletAddress) {
        this.token = token;
        this.email = email;
        this.name = name;
        this.role = role;
        this.balance = balance;
        this.requiresOtp = requiresOtp;
        this.twoFactorEnabled = twoFactorEnabled;
        this.currency = currency;
        this.walletAddress = walletAddress;
    }

    // Legacy constructor (backwards compatible)
    public LoginResponse(String token, String email, String name, String role, Double balance) {
        this(token, email, name, role, balance, false, false, "USD", null);
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public boolean isRequiresOtp() {
        return requiresOtp;
    }

    public void setRequiresOtp(boolean requiresOtp) {
        this.requiresOtp = requiresOtp;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }
}