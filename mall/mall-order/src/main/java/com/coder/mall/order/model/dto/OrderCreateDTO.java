package com.coder.mall.order.model.dto;

import java.util.List;

import com.coder.mall.order.model.entity.OrderItem;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateDTO {
    @NotNull(message = "用户ID不能为空")
    private String userId;
    
    @NotEmpty(message = "订单项不能为空")
    private List<OrderItem> orderItems;
    
    @NotNull(message = "收货信息不能为空")
    private RecipientInfo recipientInfo;
} 