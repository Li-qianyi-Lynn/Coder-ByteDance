package com.coder.mall.order.model.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
// import org.springframework.data.mongodb.core.mapping.Document;

import com.coder.mall.order.constant.OrderStatus;

import lombok.Data;

@Data
// @Document(collection = "dealer_orders")
public class DealerOrder {
    @Id
    private String orderNo;
    private Long dealerId;
    private String userId;
    private List<OrderItem> orderItems;
    private BigDecimal amount;
    private RecipientInfo recipientInfo;
    private Date createTime;
    private OrderStatus status;
   

}
