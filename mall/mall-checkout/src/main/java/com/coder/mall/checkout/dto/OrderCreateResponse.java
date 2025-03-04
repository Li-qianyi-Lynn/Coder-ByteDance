package com.coder.mall.checkout.dto;



import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrderCreateResponse {
    private String orderNo;
    private BigDecimal totalAmount;
    private String paymentUrl;  // 新增支付链接字段
}