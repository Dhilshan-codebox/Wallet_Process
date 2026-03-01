package com.paypal.wallet.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Service
public class BlockchainService {

    /**
     * Generates a deterministic, unique SHA-256 hash that acts as a
     * simulated blockchain transaction ID.
     *
     * Input: senderId + receiverId + amount + transactionDateEpoch + nonce
     */
    public String generateTransactionHash(Long senderId, Long receiverId,
            Double amount, long epochSeconds) {
        try {
            String nonce = String.valueOf(Instant.now().toEpochMilli());
            String input = senderId + "|" + receiverId + "|" + amount + "|"
                    + epochSeconds + "|" + nonce;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString(); // 64-char hex string (simulates a blockchain TX hash)
        } catch (Exception e) {
            // Fallback: timestamp-based hex
            return Long.toHexString(System.currentTimeMillis())
                    + Long.toHexString(senderId + receiverId);
        }
    }

    /**
     * Generates a wallet address (simulated) for a new user.
     * Format: 0x<40 hex chars>
     */
    public String generateWalletAddress(String email, long userId) {
        try {
            String input = email + "|" + userId + "|" + System.currentTimeMillis();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder("0x");
            for (int i = 0; i < 20; i++) {
                hex.append(String.format("%02x", hash[i]));
            }
            return hex.toString(); // Ethereum-style address
        } catch (Exception e) {
            return "0x" + Long.toHexString(userId) + Long.toHexString(email.hashCode());
        }
    }
}
