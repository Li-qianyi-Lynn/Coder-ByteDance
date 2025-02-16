// package com.coder.mall.order.repository;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Repository;

// import com.coder.mall.order.model.entity.CustomerOrder;

// @Repository
// public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

//     Optional<CustomerOrder> findByOrderIdAndUserId(String orderId, String userId);

//     List<CustomerOrder> findByUserIdAndCreateTimeBetween(
//             String userId,
//             LocalDateTime startDate,
//             LocalDateTime endDate,
//             Pageable pageable
//     );

//     Page<CustomerOrder> findByUserId(String userId, Pageable pageable);

//     boolean existsByOrderIdAndUserId(String orderId, String userId);



// }
