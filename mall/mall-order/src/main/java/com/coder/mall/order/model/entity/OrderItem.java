package com.coder.mall.order.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.coder.mall.order.model.dto.CartItem;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;


/**
 * 订单项实体类
 */

 @Data
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;    // 原始单价
    private BigDecimal discount;     // 折扣率 (0.1-1.0)
    private BigDecimal actualPrice;  // 实际单价（计算折扣后）
    private BigDecimal totalPrice;   // 总价
    
    @JsonIgnore
    private transient CartItem cartItem;
    
    public void initFromCartItem(CartItem cartItem) {
        this.cartItem = cartItem;
        this.productId = cartItem.getProductId();
        this.productName = cartItem.getProductName();
        this.quantity = cartItem.getQuantity();
        this.unitPrice = cartItem.getUnitPrice();
        this.discount = cartItem.getDiscount() != null ? cartItem.getDiscount() : BigDecimal.ONE;
        this.calculateActualPrice();
        this.calculateTotalPrice();
    }
    
    public void calculateActualPrice() {
        this.actualPrice = this.unitPrice.multiply(this.discount)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    public void calculateTotalPrice() {
        this.totalPrice = this.actualPrice.multiply(new BigDecimal(this.quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    public static OrderItem fromCartItem(CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.initFromCartItem(cartItem);
        return orderItem;
    }
}

