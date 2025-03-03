package com.coder.mall.cart.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "external-service", url = "http://external-service.com/api")
public interface ExternalServiceFeignClient {

    @GetMapping("/checkItemAvailability/{itemId}")
    boolean checkItemAvailability(@PathVariable("itemId") String itemId);
}
