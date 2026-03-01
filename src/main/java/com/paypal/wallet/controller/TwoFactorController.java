package com.paypal.wallet.controller;

import com.paypal.wallet.dto.LoginResponse;
import com.paypal.wallet.model.User;
import com.paypal.wallet.repository.UserRepository;
import com.paypal.wallet.service.AuthService;
import com.paypal.wallet.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/2fa")
public class TwoFactorController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private AuthService authService;

    /**
     * POST /auth/2fa/enable
     * Enables 2FA for the currently authenticated user and sends a test OTP.
     */
    @PostMapping("/enable")
    public ResponseEntity<String> enable2FA() {
        try {
            User user = getCurrentUser();
            if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
                return ResponseEntity.badRequest().body("2FA is already enabled.");
            }
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
            // Send verification OTP so user knows it works
            otpService.generateAndSendOtp(user);
            return ResponseEntity.ok("2FA enabled successfully. A test OTP has been sent to " + user.getEmail());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /auth/2fa/disable
     * Disables 2FA for the currently authenticated user.
     */
    @PostMapping("/disable")
    public ResponseEntity<String> disable2FA() {
        try {
            User user = getCurrentUser();
            user.setTwoFactorEnabled(false);
            otpService.clearOtp(user);
            userRepository.save(user);
            return ResponseEntity.ok("2FA disabled successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /auth/2fa/verify
     * Body: {"email": "...", "otp": "123456"}
     * Verifies OTP during login and returns full JWT if valid.
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {
        try {
            String email = body.get("email");
            String otp = body.get("otp");

            if (email == null || otp == null) {
                return ResponseEntity.badRequest().body("email and otp are required.");
            }

            String ip = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            LoginResponse response = authService.verifyOtpAndLogin(email, otp, ip, userAgent);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /auth/2fa/resend
     * Resends the OTP for the given email (used during login 2FA step).
     */
    @PostMapping("/resend")
    public ResponseEntity<String> resendOtp(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            if (email == null) {
                return ResponseEntity.badRequest().body("email is required.");
            }
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
                return ResponseEntity.badRequest().body("2FA is not enabled for this user.");
            }
            otpService.generateAndSendOtp(user);
            return ResponseEntity.ok("OTP resent to " + email);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
