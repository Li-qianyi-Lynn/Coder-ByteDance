package com.coder.mall.order.model.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.coder.mall.order.constant.OrderStatus;

import lombok.Data;

@Data
public class CustomerOrder {
    private Long orderId;
    private String orderNo;
    private String userId;
    private BigDecimal totalCost;
    private BigDecimal actual;
    private String status;
    private String recipientInfo;
    private String orderItems;
    private String paymentInfo;
    private Date createTime;
    private Date updateTime;
    private Date payTime;
    private Date deliveryTime;
    private Date completeTime;
    private String remark;
    private Boolean deleted;


    protected void onCreate() {
        createTime = new Date();
        updateTime = new Date();
        if (deleted == null) {
            deleted = false;
        }
    }
    protected void onUpdate() {
        updateTime = new Date();
    }

    public boolean canCancel() {
        return OrderStatus.CREATED.equals(status) || 
               OrderStatus.PENDING_PAYMENT.equals(status);
    }
}