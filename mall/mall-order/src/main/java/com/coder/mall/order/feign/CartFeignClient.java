package com.coder.mall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mall-cart")
public interface CartFeignClient {
    @DeleteMapping("/cart/deleteItemOfCart")
    void deleteItemFromCart(@RequestParam("userId") Long userId,
                          @RequestParam("productId") Long productId,
                          @RequestHeader("Authorization") String token);
}