package com.coder.mall.cart.response;

import com.coder.mall.cart.model.entity.Cart;

/**
 * 获取购物车响应，包含购物车内容。
 */
public class GetCartResp {
    private Cart cart;

    public GetCartResp(Cart cart) {
        this.cart = cart;
    }

    public Cart getCart() {
        return cart;
    }
}
