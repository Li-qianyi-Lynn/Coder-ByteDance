package com.coder.mall.order.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {
    private List<CartItem> cartItems;
    private BigDecimal totalCost;
}

