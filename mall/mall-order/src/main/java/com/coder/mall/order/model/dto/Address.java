package com.coder.mall.order.model.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Address {
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String zipCode;

}

