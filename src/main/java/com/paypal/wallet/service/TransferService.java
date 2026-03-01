package com.paypal.wallet.service;

import com.paypal.wallet.dto.TransactionDTO;
import com.paypal.wallet.dto.TransactionRequest;
import com.paypal.wallet.model.Transaction;
import com.paypal.wallet.model.User;
import com.paypal.wallet.repository.TransactionRepository;
import com.paypal.wallet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private CurrencyConversionService currencyConversionService;

    @Autowired
    private BlockchainService blockchainService;

    @Value("${app.transaction.single-limit:5000}")
    private double singleTransactionLimit;

    @Value("${app.transaction.daily-limit:10000}")
    private double dailyTransactionLimit;

    @Value("${app.transaction.alert-threshold-percent:80}")
    private double alertThresholdPercent;

    public TransferService(TransactionRepository transactionRepository,
            UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String transfer(TransactionRequest request) {
        // Basic validation
        if (request.getAmount() == null || request.getAmount() <= 0) {
            return "Invalid amount. Amount must be greater than 0.";
        }

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            return "Cannot transfer to yourself.";
        }

        double amount = request.getAmount();

        // ---- Phase 1: Transaction Limit Checks ----
        double effectiveSingleLimit = (sender.getSingleTransactionLimit() != null
                && sender.getSingleTransactionLimit() > 0)
                        ? sender.getSingleTransactionLimit()
                        : singleTransactionLimit;
        if (amount > effectiveSingleLimit) {
            return "Transaction blocked: amount $" + amount + " exceeds your single-transaction limit of $"
                    + effectiveSingleLimit;
        }

        // Reset daily spent if it's a new day
        if (sender.getLastTransactionDate() == null ||
                sender.getLastTransactionDate().toLocalDate().isBefore(LocalDateTime.now().toLocalDate())) {
            sender.setDailySpent(0.0);
        }

        double effectiveDailyLimit = (sender.getDailyTransactionLimit() != null
                && sender.getDailyTransactionLimit() > 0)
                        ? sender.getDailyTransactionLimit()
                        : dailyTransactionLimit;
        double projectedDailySpent = (sender.getDailySpent() != null ? sender.getDailySpent() : 0.0) + amount;

        if (projectedDailySpent > effectiveDailyLimit) {
            return "Transaction blocked: daily spending limit of $" + effectiveDailyLimit
                    + " would be exceeded. Already spent: $" + sender.getDailySpent();
        }

        // Alert if approaching limit
        double usagePercent = (projectedDailySpent / effectiveDailyLimit) * 100;
        if (usagePercent >= alertThresholdPercent) {
            sendLimitApproachingAlert(sender, projectedDailySpent, effectiveDailyLimit);
        }

        // ---- Phase 1: Balance Check ----
        if (sender.getBalance() < amount) {
            return "Insufficient balance. Your balance: $" + sender.getBalance();
        }

        // ---- Phase 1 & 2: Fraud Detection ----
        FraudDetectionService.FraudResult fraudResult = fraudDetectionService.analyze(sender, amount);
        if (fraudResult == FraudDetectionService.FraudResult.BLOCK) {
            return "Transaction blocked by fraud detection. Please contact support if this is a mistake.";
        }

        // ---- Phase 3: Currency Conversion ----
        String senderCurrency = sender.getCurrency() != null ? sender.getCurrency() : "USD";
        String receiverCurrency = receiver.getCurrency() != null ? receiver.getCurrency() : "USD";
        double convertedAmount = amount;

        if (!senderCurrency.equalsIgnoreCase(receiverCurrency)) {
            convertedAmount = currencyConversionService.convert(amount, senderCurrency, receiverCurrency);
            convertedAmount = Math.round(convertedAmount * 100.0) / 100.0;
        }

        // ---- Execute Transfer ----
        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + convertedAmount);

        // Update daily tracking
        sender.setDailySpent(projectedDailySpent);
        sender.setLastTransactionDate(LocalDateTime.now());

        userRepository.save(sender);
        userRepository.save(receiver);

        // ---- Create Transaction Record ----
        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(amount);
        transaction.setDescription(request.getDescription());
        transaction.setSenderCurrency(senderCurrency);
        transaction.setReceiverCurrency(receiverCurrency);
        transaction.setConvertedAmount(convertedAmount);
        transaction.setStatus("SUCCESS");

        // ---- Phase 3: Blockchain Hash ----
        String blockchainHash = blockchainService.generateTransactionHash(
                sender.getId(), receiver.getId(), amount,
                System.currentTimeMillis() / 1000);
        transaction.setBlockchainHash(blockchainHash);

        transactionRepository.save(transaction);

        // ---- Send Email Notifications ----
        try {
            if (!senderCurrency.equalsIgnoreCase(receiverCurrency)) {
                // Multi-currency notification
                emailService.sendTransactionEmail(sender.getEmail(),
                        "Payment Sent - " + amount + " " + senderCurrency,
                        "You sent " + amount + " " + senderCurrency + " to " + receiver.getName()
                                + "\nRecipient received: " + convertedAmount + " " + receiverCurrency
                                + "\nTX Hash: " + blockchainHash);
                emailService.sendTransactionEmail(receiver.getEmail(),
                        "Payment Received - " + convertedAmount + " " + receiverCurrency,
                        "You received " + convertedAmount + " " + receiverCurrency
                                + " from " + sender.getName()
                                + "\nSent amount: " + amount + " " + senderCurrency
                                + "\nTX Hash: " + blockchainHash);
            } else {
                emailService.sendTransactionReceipt(
                        sender.getEmail(), receiver.getEmail(), amount, request.getDescription());
            }
        } catch (Exception e) {
            System.out.println("Email notification failed: " + e.getMessage());
        }

        String message = "Transaction successful! Transferred $" + amount + " (" + senderCurrency + ")"
                + " from " + sender.getName() + " to " + receiver.getName();
        if (!senderCurrency.equalsIgnoreCase(receiverCurrency)) {
            message += " (recipient received " + convertedAmount + " " + receiverCurrency + ")";
        }
        if (fraudResult == FraudDetectionService.FraudResult.FLAG) {
            message += " [Flagged for review]";
        }

        return message;
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

    // Helper
    private void sendLimitApproachingAlert(User user, double spent, double limit) {
        try {
            emailService.sendTransactionEmail(user.getEmail(),
                    "⚠️ Daily Spending Limit Alert",
                    "Hello " + user.getName() + ",\n\n"
                            + "You have used " + String.format("%.1f", (spent / limit) * 100)
                            + "% of your daily limit.\n"
                            + "Spent today: $" + spent + " / Limit: $" + limit + "\n\n"
                            + "WalletApp Security Team");
        } catch (Exception e) {
            System.out.println("Limit alert email failed: " + e.getMessage());
        }
    }

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
                transaction.getTransactionDate());
    }
}