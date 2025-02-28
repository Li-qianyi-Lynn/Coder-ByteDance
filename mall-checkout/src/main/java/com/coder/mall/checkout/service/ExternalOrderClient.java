package com.coder.mall.checkout.service;

import com.coder.common.response.ApiResponse;
import com.coder.mall.checkout.dto.ExternalOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(name = "order-service", url = "${external.order-service.url}")
public interface ExternalOrderClient {

    @GetMapping("/{orderNo}")
    ApiResponse<ExternalOrderResponse> getOrder(
            @PathVariable("orderNo") String orderNo,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("Authorization") String token);
}