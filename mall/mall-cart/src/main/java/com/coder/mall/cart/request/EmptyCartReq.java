package com.coder.mall.cart.request;

/**
 * 清空购物车请求
 */
public class EmptyCartReq {
    private Long userId;

    public EmptyCartReq(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}