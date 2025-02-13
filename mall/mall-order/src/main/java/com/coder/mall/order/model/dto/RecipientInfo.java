package com.coder.mall.order.model.dto;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
public class RecipientInfo {
    private String name;
    private String phone;
    private Address address;
}
