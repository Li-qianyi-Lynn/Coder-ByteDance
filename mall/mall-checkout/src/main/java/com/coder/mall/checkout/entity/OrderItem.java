package com.coder.mall.checkout.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productId;
    private String productName;
    private Integer quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 5, scale = 2)
    private BigDecimal discount;

    @Column(name = "actual_price",
            precision = 10, scale = 2,
            insertable = false,  // 禁止插入
            updatable = false)   // 禁止更新
    private BigDecimal actualPrice;

    @Column(name = "total_price",
            precision = 10, scale = 2,
            insertable = false,
            updatable = false)
    private BigDecimal totalPrice;


    public void setOrder(CustomerOrder order) {

    }

    // Getters and Setters
}