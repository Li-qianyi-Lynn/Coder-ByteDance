package com.coder.mall.order.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OrderGetDTO {
    @NotNull(message = "订单号不能为空")
    private String orderNo;
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    private String token;
}
