package com.coder.mall.checkout.dto;



import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private boolean success;
    private String transactionId;
    private String message;
    private BigDecimal amount;
    private LocalDateTime paymentTime;
}