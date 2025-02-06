package com.coder.mall.order.model.entity;

import com.coder.mall.order.constant.OrderStatus;
import com.coder.mall.order.model.dto.PaymentInfo;
import com.coder.mall.order.model.dto.RecipientInfo;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "customer_orders")//todo
public class CustomerOrder {
    @Id
    private String orderId;
    private String userId;
    private List<OrderItem> orderItems;
    private BigDecimal totalCost;
    private RecipientInfo recipientInfo;
    private PaymentInfo paymentInfo;
    private OrderStatus status;
    private Map<String, String> extraInfo;

    @CreatedDate
    private Date createTime;
    @LastModifiedDate
    private Date updateTime;
}
