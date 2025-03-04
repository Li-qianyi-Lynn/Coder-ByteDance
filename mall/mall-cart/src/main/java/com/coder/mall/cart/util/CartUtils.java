package com.coder.mall.cart.util;

import com.coder.mall.cart.model.dto.CartProductItem;

import java.math.BigDecimal;
import java.util.List;

public class CartUtils {

    // 计算购物车总价
    public static BigDecimal calculateTotalPrice(List<CartProductItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    // 判断购物车是否为空
    public static boolean isCartEmpty(List<CartProductItem> cartItems) {
        return cartItems == null || cartItems.isEmpty();
    }
}
