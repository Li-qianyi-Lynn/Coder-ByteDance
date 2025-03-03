package com.coder.mall.cart.request;

/**
 * 获取购物车请求。
 */
public class GetCartReq {
    private int userId;

    public GetCartReq(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }
}

