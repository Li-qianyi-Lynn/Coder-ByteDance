package com.coder.mall.cart.response;

import com.coder.mall.cart.model.entity.Cart;

import java.util.List;

/**
 * 获取购物车响应，包含购物车内容。
 */
public class GetCartResp {
    private List<Cart> carts;

    public GetCartResp(List<Cart> carts) {
        this.carts = carts;
    }

    public List<Cart> getCarts() {
        return carts;
    }
}

