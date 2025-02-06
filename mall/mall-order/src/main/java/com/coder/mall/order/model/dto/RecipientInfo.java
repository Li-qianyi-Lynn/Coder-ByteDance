package com.coder.mall.order.model.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class RecipientInfo {
    private String recipientName;
    private String phone;
    private Address address;
}
