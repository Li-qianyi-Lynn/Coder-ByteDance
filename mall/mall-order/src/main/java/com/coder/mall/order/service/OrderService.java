package com.coder.mall.order.service;

import com.coder.mall.order.model.dto.PaymentInfo;
import com.coder.mall.order.model.dto.RecipientInfo;
import com.coder.mall.order.model.entity.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    String createCustomerOrder(String userId);
    CustomerOrder getCustomerOrder(String userId, String orderId);
    DealerOrder getDealerOrder(String dealerId, String orderId);
    List<CustomerOrder> listCustomerHistoryOrders(String userId,
                                                  LocalDateTime startDate, LocalDateTime endDate, int page, int pageSize);
    List<DealerOrder> listDealerHistoryOrders(String dealerId,
                                              LocalDateTime startDate, LocalDateTime endDate, int page, int pageSize);
    CustomerOrder updateOrder(String userId, String orderId,
                              List<OrderItem> orderItems, RecipientInfo recipientInfo,
                              PaymentInfo paymentInfo, String extraInfo);
    void cancelOrder(String userId, String orderId);
    void placeOrder(String userId, String orderId);
    Page<CustomerOrder> listCustomerOrders(String userId, int page, int size);








}
