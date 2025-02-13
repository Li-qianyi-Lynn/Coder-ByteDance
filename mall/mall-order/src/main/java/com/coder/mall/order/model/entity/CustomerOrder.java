package com.coder.mall.order.model.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.coder.mall.order.constant.OrderStatus;

import lombok.Data;

@Data
@Document(collection = "customer_orders")
public class CustomerOrder {
    @Id
    private String id;          
    private Long orderId;       // 自增订单ID（新增）
    private String orderNo;     // 订单号（新增）
    private String userId;
    private String recipientInfo;  // JSON 格式
    private String orderItems;     // JSON 格式
    private String paymentInfo;    // JSON 格式
    private BigDecimal totalCost;
    private String status;
    private Map<String, String> extraInfo;

    @CreatedDate
    private Date createTime;
    @LastModifiedDate
    private Date updateTime;

    public void setStatus(String status) {
        this.status = status;
    }

    @SuppressWarnings("unlikely-arg-type")
    public boolean canCancel() {
        return OrderStatus.CREATED.equals(status) || OrderStatus.PENDING_PAYMENT.equals(status);
    }

        
    
}
