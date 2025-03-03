package com.coder.mall.order.model.entity;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class Cart {
    private List<CartItem> cartItems;
    private BigDecimal totalCost;
}

