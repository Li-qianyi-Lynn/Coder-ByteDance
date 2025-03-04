package com.coder.mall.checkout.dto;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Embeddable
public class RecipientInfo {
    @Column(length = 64)
    private String name;

    @Column(length = 20)
    private String phone;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "province", column = @Column(name = "recipient_province")),
            @AttributeOverride(name = "city", column = @Column(name = "recipient_city")),
            @AttributeOverride(name = "district", column = @Column(name = "recipient_district")),
            @AttributeOverride(name = "street", column = @Column(name = "recipient_street")),
            @AttributeOverride(name = "detail", column = @Column(name = "recipient_detail"))
    })
    private Address address;
}
