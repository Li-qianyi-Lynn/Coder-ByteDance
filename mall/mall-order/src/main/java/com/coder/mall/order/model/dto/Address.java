package com.coder.mall.order.model.dto;

// import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// @Document
public class Address {
    // for us address
    // private String streetAddress;
    // private String city;
    // private String state;
    // private String country;
    // private String zipCode;

    // for china address
    private String country;
    private String province;
    private String city;
    private String district;
    private String street;
    private String zipCode;
}

