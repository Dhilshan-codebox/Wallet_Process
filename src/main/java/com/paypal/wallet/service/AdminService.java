package com.paypal.wallet.service;

import com.paypal.wallet.dto.AdminStatsDTO;
import com.paypal.wallet.dto.TransactionDTO;
import com.paypal.wallet.dto.UserProfileDTO;
import com.paypal.wallet.model.Transaction;
import com.paypal.wallet.model.User;
import com.paypal.wallet.repository.TransactionRepository;
import com.paypal.wallet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public AdminStatsDTO getSystemStats() {
        Long totalUsers = userRepository.count();
        Long totalTransactions = transactionRepository.count();
        Double totalAmount = transactionRepository.getTotalTransactionAmount();
        Long successful = transactionRepository.countByStatus("SUCCESS");
        Long failed = transactionRepository.countByStatus("FAILED");
        Long pending = transactionRepository.countByStatus("PENDING");

        return new AdminStatsDTO(
                totalUsers,
                totalTransactions,
                totalAmount != null ? totalAmount : 0.0,
                successful,
                failed,
                pending);
    }

    public List<UserProfileDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserProfileDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getBalance(),
                        user.getRole()))
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public String deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            return "User not found";
        }
        userRepository.deleteById(userId);
        return "User deleted successfully";
    }

    public String updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        userRepository.save(user);
        return "User role updated successfully";
    }

    public String promoteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String newRole = "ADMIN".equals(user.getRole()) ? "USER" : "ADMIN";
        user.setRole(newRole);
        userRepository.save(user);
        return "User role changed to " + newRole;
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
