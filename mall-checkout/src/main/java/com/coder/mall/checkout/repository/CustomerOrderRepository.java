package com.coder.mall.checkout.repository;

import com.coder.mall.checkout.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    Optional<CustomerOrder> findByOrderNo(String orderNo);
    Optional<CustomerOrder> findByOrderNoAndUserId(String orderNo, String userId);

    boolean existsByOrderNo(String orderNo);

    List<CustomerOrder> findByStatusAndCreateTimeBefore(String pendingPayment, LocalDateTime threshold);

}
