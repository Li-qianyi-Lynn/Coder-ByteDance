package com.coder.mall.cart.repository;

import com.coder.mall.cart.model.dto.CartProductItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<CartProductItem, Long> {

    // 根据用户ID和商品ID查找购物车项
    CartProductItem findByUserIdAndItemId(Long userId, Long itemId);
}
