package com.paypal.wallet.controller;

import com.paypal.wallet.dto.TransactionDTO;
import com.paypal.wallet.dto.TransactionRequest;
import com.paypal.wallet.service.TransferService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transfer")
public class TransferController {
    
    private final TransferService transferService;
    
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }
    
    // ============= TRANSFER MONEY =============
    @PostMapping
    public ResponseEntity<String> transfer(@RequestBody TransactionRequest request) {
        try {
            String result = transferService.transfer(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Transaction failed: " + e.getMessage());
        }
    }
    
    // ============= GET ALL TRANSACTIONS =============
    @GetMapping("/history")
    public ResponseEntity<List<TransactionDTO>> getTransactionHistory() {
        try {
            List<TransactionDTO> transactions = transferService.getUserTransactions();
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // ============= GET TRANSACTIONS WITH PAGINATION =============
    @GetMapping("/history/paginated")
    public ResponseEntity<Page<TransactionDTO>> getTransactionsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<TransactionDTO> transactions = transferService.getUserTransactionsPaginated(page, size);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // ============= FILTER BY DATE RANGE =============
    @GetMapping("/history/date-range")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<TransactionDTO> transactions = transferService.getTransactionsByDateRange(startDate, endDate);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // ============= FILTER BY STATUS =============
    @GetMapping("/history/status/{status}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByStatus(@PathVariable String status) {
        try {
            List<TransactionDTO> transactions = transferService.getTransactionsByStatus(status);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // ============= SORT BY AMOUNT =============
    @GetMapping("/history/sort-by-amount")
    public ResponseEntity<List<TransactionDTO>> getTransactionsSortedByAmount(
            @RequestParam(defaultValue = "desc") String order) {
        try {
            List<TransactionDTO> transactions = transferService.getTransactionsSortedByAmount(order);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // ============= GET TRANSACTION BY ID ============= ✅ ADD THIS
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        try {
            TransactionDTO transaction = transferService.getTransactionById(id);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // ============= CANCEL TRANSACTION ============= ✅ ADD THIS
    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancelTransaction(@PathVariable Long id) {
        try {
            String result = transferService.cancelTransaction(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}