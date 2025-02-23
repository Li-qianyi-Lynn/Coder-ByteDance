package com.coder.mall.checkout.dto;

// src/main/java/com/coder/mall/payment/dto/PaymentRequest.java



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "支付信息不能为空")
    private PaymentInfo paymentInfo;
}