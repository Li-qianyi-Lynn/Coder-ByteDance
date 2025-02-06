package com.coder.mall.order.model.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document
public class PaymentInfo {
    private String firstName;
    private String lastName;
    private String email;
    private Address address;
    private BigDecimal amount;
    private String creditCardNumber;
    private String creditCardCvv;
    private Integer creditCardExpirationYear;
    private Integer creditCardExpirationMonth;

}
