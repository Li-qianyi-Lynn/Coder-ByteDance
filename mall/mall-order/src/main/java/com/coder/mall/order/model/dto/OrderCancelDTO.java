package com.coder.mall.order.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCancelDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    @NotNull(message = "订单号不能为空")
    private String orderNo;
    @NotNull(message = "token不能为空")
    private String token;
}
