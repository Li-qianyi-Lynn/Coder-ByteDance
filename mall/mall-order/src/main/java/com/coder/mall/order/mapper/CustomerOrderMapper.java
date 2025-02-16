package com.coder.mall.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.coder.mall.order.model.entity.CustomerOrder;

@Mapper
public interface CustomerOrderMapper {
    int insert(CustomerOrder order);
    
    CustomerOrder selectById(@Param("orderId") Long orderId);
    
    // 不带分页的查询
    List<CustomerOrder> selectByUserId(@Param("userId") String userId);
    
    // 带分页的查询
    List<CustomerOrder> selectByUserIdWithPage(@Param("userId") String userId, 
                                             @Param("offset") int offset, 
                                             @Param("limit") int limit);
    
    int updateStatus(@Param("orderNo") String orderNo, @Param("status") String status);

    CustomerOrder selectByOrderNoAndUserId(@Param("orderNo") String orderNo, 
                                         @Param("userId") String userId);

    int updateStatusAndDeletedByOrderNo(@Param("orderNo") String orderNo, 
                                      @Param("status") String status,
                                      @Param("deleted") Integer deleted);

    CustomerOrder selectByOrderNo(@Param("orderNo") String orderNo);

    int countByUserId(@Param("userId") String userId);

    List<CustomerOrder> selectHistoryOrders(@Param("userId") String userId,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("offset") int offset,
                                          @Param("limit") int limit);

    int countHistoryOrders(@Param("userId") String userId,
                          @Param("startDate") String startDate,
                          @Param("endDate") String endDate);
}