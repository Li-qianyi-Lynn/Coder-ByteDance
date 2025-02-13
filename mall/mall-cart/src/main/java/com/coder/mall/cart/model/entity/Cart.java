package com.coder.mall.cart.model.entity;

import com.coder.mall.cart.model.dto.CartItem;

import java.util.List;

/**
 * 购物车实体，包含用户 ID 和商品项列表。
 */
public class Cart {

    private Integer cartId;
    private int userId;
    private Integer cartNum;
    private List<CartItem> items;

    public Cart(int userId, List<CartItem> items) {
        this.userId = userId;
        this.items = items;
    }

    public int getUserId() {
        return userId;
    }

    public List<CartItem> getItems() {
        return items;
    }
}