package com.coder.mall.checkout.dto;

import jakarta.persistence.*;
import lombok.Data;


import java.math.BigDecimal;
@Data
@Embeddable
public class PaymentInfo {
    @Column(length = 64)
    private String firstName;

    @Column(length = 64)
    private String lastName;

    @Column(length = 128)
    private String email;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "province", column = @Column(name = "payment_province")),
            @AttributeOverride(name = "city", column = @Column(name = "payment_city")),
            @AttributeOverride(name = "district", column = @Column(name = "payment_district")),
            @AttributeOverride(name = "street", column = @Column(name = "payment_street")),
            @AttributeOverride(name = "detail", column = @Column(name = "payment_detail"))
    })
    private Address address;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 19)
    private String creditCardNumber;

    @Column(length = 4)
    private String creditCardCvv;

    @Column(length = 4)
    private String creditCardExpiration;
}