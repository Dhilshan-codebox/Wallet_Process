package com.paypal.wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.paypal.wallet.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
