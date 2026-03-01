package com.paypal.wallet.service;

import com.paypal.wallet.model.User;
import com.paypal.wallet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    private final SecureRandom random = new SecureRandom();

    /**
     * Generates a 6-digit OTP, stores it on the user record with expiry,
     * and sends it via email.
     */
    public void generateAndSendOtp(User user) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        userRepository.save(user);

        String subject = "🔐 Your WalletApp Verification Code";
        String body = "Hello " + user.getName() + ",\n\n"
                + "Your one-time verification code is:\n\n"
                + "  " + otp + "\n\n"
                + "This code expires in " + otpExpiryMinutes + " minutes.\n"
                + "If you did not request this, please secure your account immediately.\n\n"
                + "Stay safe,\nWalletApp Security Team";
        emailService.sendTransactionEmail(user.getEmail(), subject, body);
    }

    /**
     * Validates the OTP entered by the user.
     * 
     * @return true if valid and not expired
     */
    public boolean validateOtp(User user, String enteredOtp) {
        if (user.getOtpCode() == null || user.getOtpExpiry() == null) {
            return false;
        }
        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            clearOtp(user);
            return false;
        }
        boolean valid = user.getOtpCode().equals(enteredOtp);
        if (valid) {
            clearOtp(user);
        }
        return valid;
    }

    /**
     * Clears OTP fields after use or expiry.
     */
    public void clearOtp(User user) {
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }
}
