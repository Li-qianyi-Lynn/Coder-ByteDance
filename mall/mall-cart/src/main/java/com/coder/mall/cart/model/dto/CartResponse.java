package com.coder.mall.cart.model.dto;

import com.coder.mall.cart.model.entity.Cart;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车响应DTO
 */
@Getter
@Setter
@AllArgsConstructor
public class CartResponse {
    List<Cart> carts;
    private boolean success;
    private String message;

    // 构造方法
    public CartResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}

