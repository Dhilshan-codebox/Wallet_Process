package com.paypal.wallet.service;

import com.paypal.wallet.model.FraudAlert;
import com.paypal.wallet.model.Transaction;
import com.paypal.wallet.model.User;
import com.paypal.wallet.repository.FraudAlertRepository;
import com.paypal.wallet.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class FraudDetectionService {

    @Autowired
    private FraudAlertRepository fraudAlertRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.fraud.large-transaction-threshold:5000}")
    private double largeTransactionThreshold;

    @Value("${app.fraud.velocity-limit:5}")
    private int velocityLimit;

    @Value("${app.fraud.velocity-window-minutes:10}")
    private int velocityWindowMinutes;

    @Value("${app.fraud.anomaly-zscore-threshold:3.0}")
    private double zScoreThreshold;

    public enum FraudResult {
        ALLOW, FLAG, BLOCK
    }

    /**
     * Main entry point: runs all fraud detection rules.
     * Returns BLOCK if transaction should be stopped,
     * FLAG if it should proceed but be investigated,
     * ALLOW if clean.
     */
    public FraudResult analyze(User sender, double amount) {
        // Rule 1: Velocity check — blocks transfer if too many recent transactions
        if (isVelocityExceeded(sender)) {
            logAlert(sender, amount, "VELOCITY", "CRITICAL",
                    "User made more than " + velocityLimit + " transactions in " + velocityWindowMinutes + " minutes.");
            sendFraudAlertEmail(sender, "Suspicious Activity: Too Many Transactions",
                    "We blocked a transaction of $" + amount + " because you exceeded the transaction velocity limit.");
            return FraudResult.BLOCK;
        }

        // Rule 2: Large amount rule
        if (amount > largeTransactionThreshold) {
            logAlert(sender, amount, "LARGE_AMOUNT", "HIGH",
                    "Transaction of $" + amount + " exceeds threshold of $" + largeTransactionThreshold);
            sendFraudAlertEmail(sender, "Large Transaction Alert",
                    "A transaction of $" + amount + " was flagged as unusually large.");
            return FraudResult.FLAG;
        }

        // Rule 3: Unusual hours (outside 06:00 – 23:00)
        LocalTime now = LocalTime.now();
        if (now.isBefore(LocalTime.of(6, 0)) || now.isAfter(LocalTime.of(23, 0))) {
            logAlert(sender, amount, "UNUSUAL_TIME", "MEDIUM",
                    "Transaction attempted at unusual hour: " + now);
            sendFraudAlertEmail(sender, "Unusual Time Transaction Alert",
                    "A transaction of $" + amount + " was attempted at an unusual time (" + now + ").");
            return FraudResult.FLAG;
        }

        // Rule 4: ML-style statistical anomaly — Z-score on user's transaction history
        if (isStatisticalAnomaly(sender, amount)) {
            logAlert(sender, amount, "ANOMALY", "MEDIUM",
                    "Transaction amount $" + amount + " is statistically anomalous for this user.");
            sendFraudAlertEmail(sender, "Anomalous Transaction Detected",
                    "A transaction of $" + amount + " is significantly higher than your typical spending pattern.");
            return FraudResult.FLAG;
        }

        return FraudResult.ALLOW;
    }

    // --- Rule implementations ---

    private boolean isVelocityExceeded(User user) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(velocityWindowMinutes);
        List<Transaction> recentTxns = transactionRepository
                .findBySenderAndTransactionDateAfter(user, windowStart);
        return recentTxns.size() >= velocityLimit;
    }

    private boolean isStatisticalAnomaly(User user, double amount) {
        List<Transaction> history = transactionRepository.findBySender(user);
        if (history.size() < 5) {
            return false; // Not enough data
        }
        double[] amounts = history.stream()
                .mapToDouble(Transaction::getAmount)
                .toArray();

        double mean = 0;
        for (double a : amounts)
            mean += a;
        mean /= amounts.length;

        double variance = 0;
        for (double a : amounts)
            variance += Math.pow(a - mean, 2);
        double stdDev = Math.sqrt(variance / amounts.length);

        if (stdDev == 0)
            return false;

        double zScore = Math.abs((amount - mean) / stdDev);
        return zScore > zScoreThreshold;
    }

    // --- Helper methods ---

    private void logAlert(User user, double amount, String type, String severity, String details) {
        FraudAlert alert = new FraudAlert(user, amount, type, severity, details);
        fraudAlertRepository.save(alert);
    }

    private void sendFraudAlertEmail(User user, String subject, String message) {
        try {
            String body = "Hello " + user.getName() + ",\n\n"
                    + message + "\n\n"
                    + "If this was you, no action is needed. If not, please contact support immediately.\n\n"
                    + "WalletApp Security Team";
            emailService.sendTransactionEmail(user.getEmail(), "🚨 " + subject, body);
        } catch (Exception e) {
            System.out.println("Fraud alert email failed: " + e.getMessage());
        }
    }
}
