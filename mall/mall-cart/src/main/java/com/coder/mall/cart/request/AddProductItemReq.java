package com.coder.mall.cart.request;

import com.coder.mall.cart.model.dto.CartProductItem;

/**
 * 添加商品请求。
 */
public class AddProductItemReq {
    private int userId;
    private CartProductItem productItem;

    public AddProductItemReq(int userId, CartProductItem productItem) {
        this.userId = userId;
        this.productItem = productItem;
    }

    public int getUserId() {
        return userId;
    }

    public CartProductItem getProductItem() {
        return productItem;
    }
}
