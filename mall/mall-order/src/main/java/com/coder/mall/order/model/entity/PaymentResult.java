package com.coder.mall.order.model.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResult {
    private String orderNo;
    private boolean success;
    private Date paymentTime;
    private String paymentInfo;
} 