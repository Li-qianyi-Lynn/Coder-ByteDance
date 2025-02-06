package com.coder.mall.cart.request;

/**
 * 清空购物车请求。
 */
public class EmptyCartReq {
    private int userId;

    public EmptyCartReq(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }
}