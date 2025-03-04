package com.coder.mall.checkout.dto;

import java.util.List;


import com.coder.mall.checkout.entity.OrderItem;
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

    @Override
    public String toString() {
        return "OrderCreateDTO{" +
                "userId='" + userId + '\'' +
                ", orderItems=" + orderItems +
                ", recipientInfo=" + recipientInfo +
                '}';
    }
} 