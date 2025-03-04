package com.coder.mall.checkout.entity;

import com.coder.mall.checkout.dto.PaymentInfo;
import com.coder.mall.checkout.dto.RecipientInfo;
import com.coder.mall.checkout.entity.OrderItem;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderNo;

    @Column(nullable = false)
    private String userId;

    @Embedded
    @AttributeOverrides({
            // 覆盖 RecipientInfo 的 name 和 phone 字段
            @AttributeOverride(name = "name", column = @Column(name = "recipient_name")),
            @AttributeOverride(name = "phone", column = @Column(name = "recipient_phone")),
            // 覆盖 Address 字段（已在 RecipientInfo 中定义）
            @AttributeOverride(name = "address.province", column = @Column(name = "recipient_province")),
            @AttributeOverride(name = "address.city", column = @Column(name = "recipient_city")),
            @AttributeOverride(name = "address.district", column = @Column(name = "recipient_district")),
            @AttributeOverride(name = "address.street", column = @Column(name = "recipient_street")),
            @AttributeOverride(name = "address.detail", column = @Column(name = "recipient_detail"))
    })
    private RecipientInfo recipientInfo;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false) // 明确指定外键列
    private List<OrderItem> orderItems = new ArrayList<>(); // 必须初始化集合

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "payment_amount")),
            @AttributeOverride(name = "creditCardNumber", column = @Column(name = "payment_credit_card_number")),
            @AttributeOverride(name = "creditCardCvv", column = @Column(name = "payment_credit_card_cvv")),
            @AttributeOverride(name = "creditCardExpiration", column = @Column(name = "payment_credit_card_expiration")),
            @AttributeOverride(name = "firstName", column = @Column(name = "payment_first_name")),
            @AttributeOverride(name = "lastName", column = @Column(name = "payment_last_name")),
            @AttributeOverride(name = "email", column = @Column(name = "payment_email"))
            // 如果PaymentInfo中的address需要覆盖，添加对应条目
    })
    private PaymentInfo paymentInfo;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalCost;

    private String status;

    @Column(updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public void addOrderItem(OrderItem item) {
        item.setOrder(this);
        orderItems.add(item);
    }



    // Getters and Setters
}