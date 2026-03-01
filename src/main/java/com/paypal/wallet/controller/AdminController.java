package com.paypal.wallet.controller;

import com.paypal.wallet.dto.AdminStatsDTO;
import com.paypal.wallet.dto.TransactionDTO;
import com.paypal.wallet.dto.UserProfileDTO;
import com.paypal.wallet.model.FraudAlert;
import com.paypal.wallet.repository.FraudAlertRepository;
import com.paypal.wallet.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    private FraudAlertRepository fraudAlertRepository;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getSystemStats() {
        try {
            return ResponseEntity.ok(adminService.getSystemStats());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileDTO>> getAllUsers() {
        try {
            return ResponseEntity.ok(adminService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        try {
            return ResponseEntity.ok(adminService.getAllTransactions());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(adminService.deleteUser(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<String> updateUserRole(@PathVariable Long userId,
            @RequestParam String role) {
        try {
            return ResponseEntity.ok(adminService.updateUserRole(userId, role));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** GET /api/admin/fraud-alerts — all fraud alerts, newest first */
    @GetMapping("/fraud-alerts")
    public ResponseEntity<List<FraudAlert>> getFraudAlerts() {
        try {
            List<FraudAlert> alerts = fraudAlertRepository.findAll();
            alerts.sort((a, b) -> b.getAlertedAt().compareTo(a.getAlertedAt()));
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /** PUT /api/admin/fraud-alerts/{id}/resolve — mark alert resolved */
    @PutMapping("/fraud-alerts/{id}/resolve")
    public ResponseEntity<String> resolveFraudAlert(@PathVariable Long id) {
        return fraudAlertRepository.findById(id).map(alert -> {
            alert.setResolved(true);
            alert.setResolvedAt(LocalDateTime.now());
            fraudAlertRepository.save(alert);
            return ResponseEntity.ok("Alert resolved");
        }).orElse(ResponseEntity.notFound().build());
    }

    /** PUT /api/admin/users/{userId}/promote — toggle USER ↔ ADMIN */
    @PutMapping("/users/{userId}/promote")
    public ResponseEntity<String> promoteUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(adminService.promoteUser(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}