package com.coder.mall.order.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.coder.mall.order.model.dto.ProductDTO;

@FeignClient(name = "mall-product")
public interface ProductFeignClient {
    
    @GetMapping("/api/products/dealer/{dealerId}")
    List<ProductDTO> listProductsByDealerId(
        @PathVariable("dealerId") Long dealerId,
        @RequestParam("page") int page,
        @RequestParam("pageSize") int pageSize
    );
} 