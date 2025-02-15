package com.coder.mall.cart.model.entity;

import com.coder.mall.cart.model.dto.CartProductItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车实体，包含用户 ID 和商品项列表。
 */
@Getter
@Setter
@AllArgsConstructor
public class Cart {

    // 购物车ID
    private Integer cartId;
    // 用户ID
    private Long userId;
    // 购物车中商品总数（可以在添加商品时动态更新）
    private Integer cartNum;
    // 购物车商品项列表
    private List<CartProductItem> productItems;
    // 购物车总价，初始化为零
    private BigDecimal totalCost = BigDecimal.ZERO;

    public Cart(Long userId) {
        this.userId = userId;
        this.productItems = new ArrayList<>();
    }

    public Cart(Long userId, List<CartProductItem> productItems) {
        this.userId = userId;
        this.productItems = productItems;
    }
}