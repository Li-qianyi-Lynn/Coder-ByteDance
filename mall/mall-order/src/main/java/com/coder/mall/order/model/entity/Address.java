package com.coder.mall.order.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
// @Document
public class Address {
    // for us address
    // private String streetAddress;
    // private String city;
    // private String state;
    // private String country;
    // private String zipCode;

    // for china address
    private String province;
    private String city;
    private String district;
    
    @JsonProperty("detail")
    private String detailAddress;
    
    // private String street;
    private String country;
    private String zipCode;
}

