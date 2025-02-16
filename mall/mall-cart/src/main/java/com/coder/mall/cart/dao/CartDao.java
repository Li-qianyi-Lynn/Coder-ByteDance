package com.coder.mall.cart.dao;

import com.coder.mall.cart.model.entity.Cart;

public interface CartDao {
    void saveOrUpdate(Cart cart);

    void deleteByUserId(Long userId);
}
