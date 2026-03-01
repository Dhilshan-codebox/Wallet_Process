package com.paypal.wallet.repository;

import com.paypal.wallet.model.Transaction;
import com.paypal.wallet.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySender(User sender);

    List<Transaction> findByReceiver(User receiver);

    List<Transaction> findBySenderOrReceiver(User sender, User receiver);

    // Paginated queries
    Page<Transaction> findBySenderOrReceiver(User sender, User receiver, Pageable pageable);

    // Filter by date range
    @Query("SELECT t FROM Transaction t WHERE (t.sender = :user OR t.receiver = :user) " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Filter by status
    List<Transaction> findBySenderAndStatus(User sender, String status);

    List<Transaction> findByReceiverAndStatus(User receiver, String status);

    // Sort by amount
    @Query("SELECT t FROM Transaction t WHERE t.sender = :user OR t.receiver = :user ORDER BY t.amount DESC")
    List<Transaction> findByUserOrderByAmountDesc(@Param("user") User user);

    @Query("SELECT t FROM Transaction t WHERE t.sender = :user OR t.receiver = :user ORDER BY t.amount ASC")
    List<Transaction> findByUserOrderByAmountAsc(@Param("user") User user);

    // Count by status
    Long countByStatus(String status);

    // Get total transaction amount
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = 'SUCCESS'")
    Double getTotalTransactionAmount();

    // === Fraud Detection: Velocity Check ===
    List<Transaction> findBySenderAndTransactionDateAfter(User sender, LocalDateTime since);

    // === Fraud Detection: Count recent transactions ===
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.sender = :user AND t.transactionDate >= :since")
    long countRecentTransactions(@Param("user") User user, @Param("since") LocalDateTime since);

    // === Daily spent calculation ===
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.sender = :user " +
            "AND t.transactionDate >= :startOfDay AND t.status = 'SUCCESS'")
    Double getDailySpent(@Param("user") User user, @Param("startOfDay") LocalDateTime startOfDay);
}