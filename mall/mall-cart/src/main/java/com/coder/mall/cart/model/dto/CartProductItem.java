package com.coder.mall.cart.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 购物车商品项。
 */
@Getter
@Setter
@AllArgsConstructor
public class CartProductItem {
    // 商品ID
    private Long productId;
    // 商品数量
    private int quantity;
    // 是否有效
    private Boolean is_valid;
    // 商品单价
    private BigDecimal unit_price;

    public CartProductItem(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
