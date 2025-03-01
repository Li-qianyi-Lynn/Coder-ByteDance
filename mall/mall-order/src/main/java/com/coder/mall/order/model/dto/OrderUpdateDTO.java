package com.coder.mall.order.model.dto;

import java.util.List;

import com.coder.mall.order.model.entity.OrderItem;
import com.coder.mall.order.model.entity.PaymentInfo;
import com.coder.mall.order.model.entity.RecipientInfo;

import lombok.Data;

@Data
public class OrderUpdateDTO {
    private String userId;
    private String orderNo;
    private String token;
    private List<OrderItem> orderItems;
    private RecipientInfo recipientInfo;
    private PaymentInfo paymentInfo;
    private String extraInfo;
 
}

