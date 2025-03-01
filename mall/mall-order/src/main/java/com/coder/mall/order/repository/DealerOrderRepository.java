package com.coder.mall.order.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.coder.mall.order.model.entity.DealerOrder;

@Repository
// public interface DealerOrderRepository extends MongoRepository<DealerOrder, String> {
public interface DealerOrderRepository {
    Optional<DealerOrder> findByOrderIdAndDealerId(String orderId, String dealerId);

    List<DealerOrder> findByDealerIdAndCreateTimeBetween(
            String dealerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    Page<DealerOrder> findByDealerId(String dealerId, Pageable pageable);

    boolean existsByOrderIdAndDealerId(String orderId, String dealerId);


}
