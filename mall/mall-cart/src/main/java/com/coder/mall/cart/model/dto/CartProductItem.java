package com.coder.mall.cart.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 购物车商品项
 */
@Getter
@Setter
public class CartProductItem {
    // 商品ID
    private Long productId;
    // 商品数量
    private int quantity;
    // 是否有效
    private Boolean isValid;
    // 商品单价
    private BigDecimal unitPrice;

    public CartProductItem(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public CartProductItem(Long productId, int quantity, Boolean is_valid, BigDecimal unit_price) {
        this.productId = productId;
        this.quantity = quantity;
        // 默认有效
        this.isValid = is_valid != null ? is_valid : true;
        // 默认价格为0
        this.unitPrice = unit_price != null ? unit_price : BigDecimal.ZERO;
    }

}
