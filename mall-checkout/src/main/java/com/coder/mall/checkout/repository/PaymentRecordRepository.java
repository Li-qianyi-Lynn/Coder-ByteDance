package com.coder.mall.checkout.repository;

// src/main/java/com/coder/mall/payment/repository/PaymentRecordRepository.java

import com.coder.mall.checkout.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    Optional<PaymentRecord> findByTransactionId(String transactionId);

    boolean existsByTransactionId(String transactionId);
    List<PaymentRecord> findByAmount(BigDecimal amount);
}