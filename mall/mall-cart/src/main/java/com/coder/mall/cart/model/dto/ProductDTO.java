package com.coder.mall.cart.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductDTO {

    private Long productId; // 商品ID

    private Long dealerId; // 商家ID

    private String name; // 商品名称

    private String description; // 商品描述

    private String category; // 商品类别

    private String pictureUrl; // 商品图片URL

    private BigDecimal price; // 商品价格

    private String status; // 商品状态
}