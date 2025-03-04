package com.coder.mall.order.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
// @Document
public class RecipientInfo {
    // @JsonProperty("name")
    private String recipientName;
    private String phone;
    private Address address;
}
