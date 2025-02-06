package com.coder.mall.order.model.entity;

import com.coder.mall.order.model.dto.RecipientInfo;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = "dealer_orders")
public class DealerOrder {
    @Id
    private String orderId;
    private String dealerId;
    private String userId;
    private List<OrderItem> orderItems;
    private BigDecimal amount;
    private RecipientInfo recipientInfo;
    private Date createTime;

}
