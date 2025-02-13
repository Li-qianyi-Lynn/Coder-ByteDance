package com.coder.mall.order.model.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CartItem {
    private String productId;
    private String productName;
    private Integer quantity;
    private Boolean isValid;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal totalPrice;
    public String getProductName() {
        return productName;

    }

}
