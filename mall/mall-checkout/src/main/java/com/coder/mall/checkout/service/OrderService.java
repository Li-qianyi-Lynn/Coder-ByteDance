package com.coder.mall.checkout.service;

import com.coder.mall.checkout.entity.CustomerOrder;
import com.coder.mall.checkout.repository.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

// 文件位置：OrderService.java（需新建）
@Service
@RequiredArgsConstructor
public class OrderService {

    private final CustomerOrderRepository orderRepository;

    // 随机订单号生成（推荐）
    public String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() +
                String.format("%04d", new Random().nextInt(10000));
    }

    // 固定订单号（测试用）
    public String getFixedOrderNo() {
        return "12345678";
    }

    @Transactional
    public CustomerOrder createOrder() {
        CustomerOrder order = new CustomerOrder();
        order.setOrderNo(generateOrderNo()); // 设置生成的订单号
        return orderRepository.save(order);
    }
}