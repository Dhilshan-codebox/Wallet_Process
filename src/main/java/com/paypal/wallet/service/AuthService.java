package com.paypal.wallet.service;

import com.paypal.wallet.dto.LoginRequest;
import com.paypal.wallet.dto.LoginResponse;
import com.paypal.wallet.model.User;
import com.paypal.wallet.repository.UserRepository;
import com.paypal.wallet.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public String register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "User already exists with this email";
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setBalance(1000.0); // Initial balance
        userRepository.save(user);
        
        return "User registered successfully!";
    }
    
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        
        return new LoginResponse(
            token,
            user.getEmail(),
            user.getName(),
            user.getRole(),
            user.getBalance()
        );
    }
}