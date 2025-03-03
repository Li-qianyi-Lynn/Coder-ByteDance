package com.coder.mall.order.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderCancelResponseDTO {
    private String code;
    private String message;
    private String orderNo;
    private String userId;
} 