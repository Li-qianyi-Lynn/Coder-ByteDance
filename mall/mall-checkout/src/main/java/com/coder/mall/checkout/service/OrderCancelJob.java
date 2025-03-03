package com.coder.mall.checkout.service;

import com.coder.mall.checkout.entity.CustomerOrder;
import com.coder.mall.checkout.repository.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelJob {

    private final CustomerOrderRepository orderRepository;

    @Scheduled(fixedRate = 300_000)  // 每5分钟执行一次
    @Transactional
    public void cancelUnpaidOrders() {
        LocalDateTime threshold = LocalDateTime.now().minus(30, ChronoUnit.MINUTES);

        List<CustomerOrder> orders = orderRepository
                .findByStatusAndCreateTimeBefore("PENDING_PAYMENT", threshold);

        for (CustomerOrder order : orders) {
            order.setStatus("CANCELLED");
            log.info("订单自动取消：{}", order.getOrderNo());
        }

        orderRepository.saveAll(orders);
    }
}