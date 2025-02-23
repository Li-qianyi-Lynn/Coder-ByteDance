package com.coder.mall.cart.service;

import com.coder.mall.cart.model.dto.ProductDTO;

import java.util.List;

public interface ProductService {
    List<ProductDTO> listProductsByIds(List<Long> productIds);
}