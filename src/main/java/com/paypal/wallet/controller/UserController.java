package com.paypal.wallet.controller;

import com.paypal.wallet.dto.UpdateProfileRequest;
import com.paypal.wallet.dto.UserProfileDTO;
import com.paypal.wallet.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile() {
        try {
            UserProfileDTO profile = userService.getUserProfile();
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ── Find any user by email (used by transfer.js to resolve receiver ID) ──
    @GetMapping("/find")
    public ResponseEntity<UserProfileDTO> findByEmail(@RequestParam String email) {
        try {
            UserProfileDTO user = userService.findByEmail(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            String result = userService.updateProfile(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}