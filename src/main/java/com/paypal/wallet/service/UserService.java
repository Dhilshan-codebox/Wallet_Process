package com.paypal.wallet.service;

import com.paypal.wallet.dto.UpdateProfileRequest;
import com.paypal.wallet.dto.UserProfileDTO;
import com.paypal.wallet.model.User;
import com.paypal.wallet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserProfileDTO getUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserProfileDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBalance(),
                user.getRole());
    }

    // ── Look up any user by email (for receiver lookup during transfer) ──
    public UserProfileDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return new UserProfileDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBalance(),
                user.getRole());
    }

    public String updateProfile(UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()
                    && !request.getEmail().equals(email)) {
                return "Email already in use";
            }
            user.setEmail(request.getEmail());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return "Profile updated successfully";
    }
}