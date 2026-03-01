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

    @Autowired
    private OtpService otpService;

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private DeviceFingerprintService deviceFingerprintService;

    public String register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "User already exists with this email";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setBalance(1000.0);
        user.setTwoFactorEnabled(false);
        user.setDailyTransactionLimit(10000.0);
        user.setSingleTransactionLimit(5000.0);
        user.setDailySpent(0.0);
        user.setCurrency("USD");

        // Generate blockchain wallet address
        User saved = userRepository.save(user);
        String walletAddress = blockchainService.generateWalletAddress(saved.getEmail(), saved.getId());
        saved.setWalletAddress(walletAddress);
        userRepository.save(saved);

        return "User registered successfully!";
    }

    /**
     * Step 1 of login: validates credentials.
     * - If 2FA is enabled → sends OTP and returns {requiresOtp: true, token: null}
     * - If 2FA is disabled → returns full JWT immediately
     */
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            // Send OTP and ask frontend to complete 2FA
            otpService.generateAndSendOtp(user);
            return new LoginResponse(null, user.getEmail(), user.getName(),
                    user.getRole(), user.getBalance(), true, user.getTwoFactorEnabled(),
                    user.getCurrency(), user.getWalletAddress());
        }

        // No 2FA — do device fingerprint + geo check then issue JWT
        try {
            deviceFingerprintService.processLogin(user, userAgent, ipAddress);
        } catch (Exception e) {
            System.out.println("Device fingerprint check failed: " + e.getMessage());
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new LoginResponse(token, user.getEmail(), user.getName(),
                user.getRole(), user.getBalance(), false, user.getTwoFactorEnabled(),
                user.getCurrency(), user.getWalletAddress());
    }

    /**
     * Step 2 of login (2FA path): validates OTP and issues full JWT.
     */
    public LoginResponse verifyOtpAndLogin(String email, String otp, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!otpService.validateOtp(user, otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // OTP valid — do device fingerprint + geo check then issue JWT
        try {
            deviceFingerprintService.processLogin(user, userAgent, ipAddress);
        } catch (Exception e) {
            System.out.println("Device fingerprint check failed: " + e.getMessage());
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new LoginResponse(token, user.getEmail(), user.getName(),
                user.getRole(), user.getBalance(), false, user.getTwoFactorEnabled(),
                user.getCurrency(), user.getWalletAddress());
    }
}