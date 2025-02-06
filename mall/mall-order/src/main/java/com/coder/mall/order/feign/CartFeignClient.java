package com.coder.mall.order.feign;

import com.coder.mall.order.model.dto.Cart;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "cart-service")
public interface CartFeignClient {
    @GetMapping("/api/vi/carts")
    Cart getCart(@RequestHeader("X-User-ID") String userId);
}
