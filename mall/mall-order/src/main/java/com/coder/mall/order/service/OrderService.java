package com.coder.mall.order.service;

import com.coder.mall.order.constant.OrderStatus;
import com.coder.mall.order.model.dto.DealerGetDTO;
import com.coder.mall.order.model.dto.ListCustomerHistoryDTO;
import com.coder.mall.order.model.dto.ListDealerHistoryDTO;
import com.coder.mall.order.model.dto.OrderCancelDTO;
import com.coder.mall.order.model.dto.OrderCreateDTO;
import com.coder.mall.order.model.dto.OrderGetDTO;
import com.coder.mall.order.model.dto.OrderPlaceDTO;
import com.coder.mall.order.model.dto.OrderUpdateDTO;
import com.coder.mall.order.model.entity.CustomerOrder;
import com.coder.mall.order.model.entity.DealerOrder;
import com.coder.mall.order.model.entity.PageResult;

public interface OrderService {
    // // 从购物车创建订单
    // CustomerOrder createOrderFromCart(String userId);
    
    // 直接创建订单（使用传入的订单信息）
    CustomerOrder createCustomerOrder(OrderCreateDTO orderCreateDTO);
    CustomerOrder getCustomerOrder(OrderGetDTO orderGetDTO);
    DealerOrder getDealerOrder(DealerGetDTO dealerGetDTO);
    PageResult<CustomerOrder> listCustomerHistoryOrders(ListCustomerHistoryDTO listCustomerHistoryDTO);
    PageResult<DealerOrder> listDealerHistoryOrders(ListDealerHistoryDTO listDealerHistoryDTO);
    CustomerOrder updateOrder(OrderUpdateDTO orderUpdateDTO);
    void cancelOrder(OrderCancelDTO orderCancelDTO);
    // 确认订单
    CustomerOrder placeOrder(OrderPlaceDTO orderPlaceDTO);

    /**
     * 更新订单支付状态
     * @param orderNo 订单号
     * @param paymentStatus 支付状态
     * @return 更新是否成功
     */
    int updateOrderPaymentStatus(String orderNo, OrderStatus paymentStatus);




    
}
