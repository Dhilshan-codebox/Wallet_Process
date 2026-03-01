package com.paypal.wallet.repository;

import com.paypal.wallet.model.FraudAlert;
import com.paypal.wallet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {

    List<FraudAlert> findByUserOrderByAlertedAtDesc(User user);

    List<FraudAlert> findByUserAndAlertType(User user, String alertType);

    List<FraudAlert> findByResolvedFalse();

    List<FraudAlert> findByUserAndAlertedAtAfter(User user, LocalDateTime since);

    long countByResolvedFalse();

    long countByUser(User user);
}
