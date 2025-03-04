package com.coder.mall.checkout.entity;



import jakarta.persistence.*;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment_records")
public class PaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String orderNo;

    @Column(unique = true, nullable = false, length = 64)
    private String transactionId;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 20)
    private String paymentStatus;

    @Column(length = 32)
    private String paymentGateway;

    @Column(updatable = false)
    private LocalDateTime createdTime;

    @Column
    private LocalDateTime updatedTime;

    // getters/setters...
}