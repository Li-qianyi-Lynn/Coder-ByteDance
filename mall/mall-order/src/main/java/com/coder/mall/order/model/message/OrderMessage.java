package com.coder.mall.order.model.message;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.coder.mall.order.constant.OrderStatus;

import lombok.Data;

@Data
public class OrderMessage implements Serializable {
    private String orderNo;
    private Long userId;
    private OrderStatus status;
    private Date timestamp;
    private Map<String, Object> extraData;
} 