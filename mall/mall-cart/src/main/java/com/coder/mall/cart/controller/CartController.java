package com.coder.mall.cart.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class CartController {
    
    @DeleteMapping("/cart/deleteItemOfCart")
    public void deleteItemFromCart(@RequestParam("userId") Long userId,
                                 @RequestParam("productId") Long productId,
                                 @RequestHeader("Authorization") String token) {
        log.info("删除购物车商品 - userId: {}, productId: {}", userId, productId);
        // 模拟删除成功
        try {
            // 模拟处理时间
            Thread.sleep(100);
            log.info("商品删除成功");
        } catch (InterruptedException e) {
            log.error("删除商品时发生错误", e);
            throw new RuntimeException("删除商品失败");
        }
    }
} 