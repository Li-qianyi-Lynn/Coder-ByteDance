package com.coder.mall.order.service;

import java.time.LocalDateTime;
import java.util.List;

import com.coder.mall.order.model.dto.OrderCreateDTO;
import com.coder.mall.order.model.dto.PageResult;
import com.coder.mall.order.model.dto.PaymentInfo;
import com.coder.mall.order.model.dto.RecipientInfo;
import com.coder.mall.order.model.entity.CustomerOrder;
import com.coder.mall.order.model.entity.OrderItem;

public interface OrderService {
    // 从购物车创建订单
    CustomerOrder createOrderFromCart(String userId);
    
    // 直接创建订单（使用传入的订单信息）
    CustomerOrder createOrder(OrderCreateDTO orderCreateDTO);
    
    CustomerOrder getCustomerOrder(String userId, String orderNo);
    // DealerOrder getDealerOrder(String dealerId, String orderId);
    PageResult<CustomerOrder> listCustomerHistoryOrders(String userId,
                                                  LocalDateTime startDate, LocalDateTime endDate, int page, int pageSize);
    // PageResult<DealerOrder> listDealerHistoryOrders(String dealerId,
    //                                           LocalDateTime startDate, LocalDateTime endDate, int page, int pageSize);
    CustomerOrder updateOrder(String userId, String orderId,
                              List<OrderItem> orderItems, RecipientInfo recipientInfo,
                              PaymentInfo paymentInfo, String extraInfo);
    void cancelOrder(String userId, String orderId);
    // void placeOrder(String userId, String orderId);
    PageResult<CustomerOrder> listCustomerOrders(String userId, int page, int size);
    
}
