package com.paypal.wallet.dto;

/**
 * Represents a single recipient entry in a bulk transfer request.
 */
public class BulkTransferItem {

    private String receiverEmail;
    private Double amount;
    private String description;

    public BulkTransferItem() {
    }

    public BulkTransferItem(String receiverEmail, Double amount, String description) {
        this.receiverEmail = receiverEmail;
        this.amount = amount;
        this.description = description;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
