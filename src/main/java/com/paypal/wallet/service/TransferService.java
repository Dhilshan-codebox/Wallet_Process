package com.paypal.wallet.service;

import com.paypal.wallet.dto.TransactionDTO;
import com.paypal.wallet.dto.TransactionRequest;
import com.paypal.wallet.model.Transaction;
import com.paypal.wallet.model.User;
import com.paypal.wallet.repository.TransactionRepository;
import com.paypal.wallet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransferService {
    
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    public TransferService(TransactionRepository transactionRepository, 
                          UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }
    
    // Transfer money
    @Transactional
    public String transfer(TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            return "Invalid amount. Amount must be greater than 0.";
        }
        
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        if (sender.getBalance() < request.getAmount()) {
            return "Insufficient balance. Your balance: " + sender.getBalance();
        }
        
        if (sender.getId().equals(receiver.getId())) {
            return "Cannot transfer to yourself.";
        }
        
        sender.setBalance(sender.getBalance() - request.getAmount());
        receiver.setBalance(receiver.getBalance() + request.getAmount());
        
        userRepository.save(sender);
        userRepository.save(receiver);
        
        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setStatus("SUCCESS");
        transactionRepository.save(transaction);
        
        // Send email notifications
        try {
            emailService.sendTransactionReceipt(
                sender.getEmail(),
                receiver.getEmail(),
                request.getAmount(),
                request.getDescription()
            );
        } catch (Exception e) {
            System.out.println("Email notification failed: " + e.getMessage());
        }
        
        return "Transaction successful! Transferred $" + request.getAmount() 
               + " from " + sender.getName() + " to " + receiver.getName();
    }
    
    // Get all transactions for current user
    public List<TransactionDTO> getUserTransactions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Transaction> transactions = transactionRepository.findBySenderOrReceiver(user, user);
        return convertToDTO(transactions);
    }
    
    // Get transactions with pagination
    public Page<TransactionDTO> getUserTransactionsPaginated(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<Transaction> transactions = transactionRepository.findBySenderOrReceiver(user, user, pageable);
        
        return transactions.map(this::convertToDTO);
    }
    
    // Filter by date range
    public List<TransactionDTO> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Transaction> transactions = transactionRepository.findByUserAndDateRange(user, startDate, endDate);
        return convertToDTO(transactions);
    }
    
    // Filter by status
    public List<TransactionDTO> getTransactionsByStatus(String status) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Transaction> sent = transactionRepository.findBySenderAndStatus(user, status);
        List<Transaction> received = transactionRepository.findByReceiverAndStatus(user, status);
        
        sent.addAll(received);
        return convertToDTO(sent);
    }
    
    // Sort by amount
    public List<TransactionDTO> getTransactionsSortedByAmount(String order) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Transaction> transactions;
        if ("desc".equalsIgnoreCase(order)) {
            transactions = transactionRepository.findByUserOrderByAmountDesc(user);
        } else {
            transactions = transactionRepository.findByUserOrderByAmountAsc(user);
        }
        
        return convertToDTO(transactions);
    }
    
    // Get transaction by ID
    public TransactionDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        return convertToDTO(transaction);
    }
    
    // Cancel pending transaction
    @Transactional
    public String cancelTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        if (!"PENDING".equals(transaction.getStatus())) {
            return "Can only cancel pending transactions";
        }
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!transaction.getSender().getEmail().equals(email)) {
            return "You can only cancel your own transactions";
        }
        
        transaction.setStatus("CANCELLED");
        transactionRepository.save(transaction);
        
        return "Transaction cancelled successfully";
    }
    
    // Helper methods
    private List<TransactionDTO> convertToDTO(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private TransactionDTO convertToDTO(Transaction transaction) {
        return new TransactionDTO(
            transaction.getId(),
            transaction.getSender().getName(),
            transaction.getSender().getEmail(),
            transaction.getReceiver().getName(),
            transaction.getReceiver().getEmail(),
            transaction.getAmount(),
            transaction.getDescription(),
            transaction.getStatus(),
            transaction.getTransactionDate()
        );
    }
}