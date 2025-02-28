package com.coder.mall.checkout.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 新增 ExternalOrderResponse.java
@Data
public class ExternalOrderResponse {
    private String orderNo;
    private Long userId;
    private BigDecimal totalCost;
    private ExternalRecipientInfo recipientInfo;
    private String orderItems; // JSON字符串
    private LocalDateTime createTime;

    @Data
    public static class ExternalRecipientInfo {
        private String name;
        private String phone;
        private ExternalAddress address;
    }

    @Data
    public static class ExternalAddress {
        private String province;
        private String city;
        private String district;
        private String detail;
    }
}