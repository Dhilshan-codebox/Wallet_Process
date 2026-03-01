package com.paypal.wallet.dto;

import java.util.List;

/**
 * Top-level request body for POST /api/transfer/bulk
 */
public class BulkTransferRequest {

    private Long senderId;
    private List<BulkTransferItem> items;

    public BulkTransferRequest() {
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public List<BulkTransferItem> getItems() {
        return items;
    }

    public void setItems(List<BulkTransferItem> items) {
        this.items = items;
    }
}
