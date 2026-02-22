package com.paypal.wallet.service;

import com.paypal.wallet.dto.DepositRequest;
import com.paypal.wallet.model.User;
import com.paypal.wallet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public String deposit(DepositRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            return "Invalid amount. Amount must be greater than 0.";
        }
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setBalance(user.getBalance() + request.getAmount());
        userRepository.save(user);
        
        return "Deposit successful! Added $" + request.getAmount() 
               + ". New balance: $" + user.getBalance();
    }
    
    public Double getBalance() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return user.getBalance();
    }
}