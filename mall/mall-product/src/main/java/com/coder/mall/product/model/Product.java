package com.coder.mall.product.model;

import jakarta.persistence.*;
import lombok.Data;

//import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId; // 商品ID

    @Column(nullable = false)
    private Long dealerId; // 商家ID

    @Column(nullable = false)
    private String name; // 商品名称

    private String description; // 商品描述

    private String category; // 商品类别

    private String pictureUrl; // 商品图片URL

    @Column(nullable = false)
    private BigDecimal price; // 商品价格

    private String status; // 商品状态

    private LocalDateTime createdAt; // 创建时间

    private LocalDateTime updatedAt; // 更新时间

    private LocalDateTime deletedAt; // 删除时间

    // Getters and Setters
    // ...
}
