package com.coder.mall.cart.request;

import com.coder.mall.cart.model.dto.CartItem;

/**
 * 添加商品请求。
 */
public class AddItemReq {
    private int userId;
    private CartItem item;

    public AddItemReq(int userId, CartItem item) {
        this.userId = userId;
        this.item = item;
    }

    public int getUserId() {
        return userId;
    }

    public CartItem getItem() {
        return item;
    }
}
