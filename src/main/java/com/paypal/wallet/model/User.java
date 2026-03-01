package com.paypal.wallet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Double balance = 0.0;

    @Column(nullable = false)
    private String role = "USER"; // USER or ADMIN

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    // === Phase 1: 2FA / OTP ===
    @Column(nullable = false)
    private Boolean twoFactorEnabled = false;

    private String otpCode;

    private LocalDateTime otpExpiry;

    // === Phase 1: Transaction Limits ===
    @Column(nullable = false)
    private Double dailyTransactionLimit = 10000.0;

    @Column(nullable = false)
    private Double singleTransactionLimit = 5000.0;

    @Column(nullable = false)
    private Double dailySpent = 0.0;

    private LocalDateTime lastTransactionDate;

    // === Phase 2: IP Geolocation ===
    private String lastKnownIp;

    private String lastKnownCountry;

    // === Phase 3: Multi-Currency + Blockchain wallet address ===
    @Column(nullable = false)
    private String currency = "USD";

    @Column(unique = true)
    private String walletAddress;

    // Constructors
    public User() {
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.balance = 0.0;
        this.role = "USER";
        this.createdAt = LocalDateTime.now();
        this.twoFactorEnabled = false;
        this.dailyTransactionLimit = 10000.0;
        this.singleTransactionLimit = 5000.0;
        this.dailySpent = 0.0;
        this.currency = "USD";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // 2FA
    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public LocalDateTime getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(LocalDateTime otpExpiry) {
        this.otpExpiry = otpExpiry;
    }

    // Transaction Limits
    public Double getDailyTransactionLimit() {
        return dailyTransactionLimit;
    }

    public void setDailyTransactionLimit(Double dailyTransactionLimit) {
        this.dailyTransactionLimit = dailyTransactionLimit;
    }

    public Double getSingleTransactionLimit() {
        return singleTransactionLimit;
    }

    public void setSingleTransactionLimit(Double singleTransactionLimit) {
        this.singleTransactionLimit = singleTransactionLimit;
    }

    public Double getDailySpent() {
        return dailySpent;
    }

    public void setDailySpent(Double dailySpent) {
        this.dailySpent = dailySpent;
    }

    public LocalDateTime getLastTransactionDate() {
        return lastTransactionDate;
    }

    public void setLastTransactionDate(LocalDateTime lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }

    // IP Geolocation
    public String getLastKnownIp() {
        return lastKnownIp;
    }

    public void setLastKnownIp(String lastKnownIp) {
        this.lastKnownIp = lastKnownIp;
    }

    public String getLastKnownCountry() {
        return lastKnownCountry;
    }

    public void setLastKnownCountry(String lastKnownCountry) {
        this.lastKnownCountry = lastKnownCountry;
    }

    // Multi-Currency
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