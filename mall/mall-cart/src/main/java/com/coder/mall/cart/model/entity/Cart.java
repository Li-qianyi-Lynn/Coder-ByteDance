package com.coder.mall.cart.model.entity;

import com.coder.mall.cart.model.dto.CartProductItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车实体，包含用户 ID 和商品项列表。
 */
public class Cart {

    private Integer cartId;
    private int userId;
    private Integer cartNum;
    private List<CartProductItem> productItems;

    public Cart(int userId) {
        this.userId = userId;
        this.productItems = new ArrayList<>();
    }

    public Cart(int userId, List<CartProductItem> productItems) {
        this.userId = userId;
        this.productItems = productItems;
    }

    public Cart(int userId, int productId, int quantity) {
        this.userId = userId;
        this.productItems = new ArrayList<>();
        this.productItems.add(new CartProductItem(productId, quantity));  // 将 CartItem 添加到 items 列表中
    }

    public int getUserId() {
        return userId;
    }

    public List<CartProductItem> getProductItems() {
        return productItems;
    }
}