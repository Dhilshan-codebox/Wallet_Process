package com.paypal.wallet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alerts")
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double transactionAmount;

    @Column(nullable = false)
    private String alertType; // LARGE_AMOUNT, UNUSUAL_TIME, VELOCITY, ANOMALY, LOCATION_CHANGE

    @Column(nullable = false)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(length = 1000)
    private String details;

    @Column(nullable = false)
    private LocalDateTime alertedAt = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean resolved = false;

    private LocalDateTime resolvedAt;

    // Constructors
    public FraudAlert() {
    }

    public FraudAlert(User user, Double amount, String alertType, String severity, String details) {
        this.user = user;
        this.transactionAmount = amount;
        this.alertType = alertType;
        this.severity = severity;
        this.details = details;
        this.alertedAt = LocalDateTime.now();
        this.resolved = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(Double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getAlertedAt() {
        return alertedAt;
    }

    public void setAlertedAt(LocalDateTime alertedAt) {
        this.alertedAt = alertedAt;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
