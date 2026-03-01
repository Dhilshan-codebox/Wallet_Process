package com.paypal.wallet.dto;

/**
 * Per-recipient result returned after a bulk transfer.
 */
public class BulkTransferResultDTO {

    private String receiverEmail;
    private Double amount;
    private String status; // "SUCCESS" or "FAILED"
    private String message; // success text or error reason
    private String blockchainHash; // null if failed

    public BulkTransferResultDTO() {
    }

    public BulkTransferResultDTO(String receiverEmail, Double amount,
            String status, String message, String blockchainHash) {
        this.receiverEmail = receiverEmail;
        this.amount = amount;
        this.status = status;
        this.message = message;
        this.blockchainHash = blockchainHash;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBlockchainHash() {
        return blockchainHash;
    }

    public void setBlockchainHash(String blockchainHash) {
        this.blockchainHash = blockchainHash;
    }
}
