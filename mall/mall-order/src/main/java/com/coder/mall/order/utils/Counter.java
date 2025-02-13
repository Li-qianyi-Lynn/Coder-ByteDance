package com.coder.mall.order.utils;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "counters")
public class Counter {
    @Id
    private String id;
    private Long sequenceValue;
} 