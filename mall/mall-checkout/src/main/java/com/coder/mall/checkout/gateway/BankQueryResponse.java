package com.coder.mall.checkout.gateway;

import lombok.Data;

import java.math.BigDecimal;

// 查询响应DTO
@Data
public class BankQueryResponse {
    private boolean success;
    private String status;
    private String transactionId;
    private BigDecimal amount;
    private String errorCode;
    private String errorMsg;
}
