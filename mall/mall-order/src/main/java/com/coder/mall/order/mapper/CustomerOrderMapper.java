package com.coder.mall.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.coder.mall.order.model.entity.CustomerOrder;

@Mapper
public interface CustomerOrderMapper {
    // 插入订单
    int insert(CustomerOrder order);
    
    // 根据订单ID查询
    CustomerOrder selectById(@Param("orderId") Long orderId);
    
    // 根据用户ID查询订单列表
    List<CustomerOrder> selectByUserId(@Param("userId") String userId);
    
    // 更新订单状态
    int updateStatus(@Param("orderNo") String orderNo, 
    @Param("status") String status);

    // 根据订单编号和用户ID查询订单
    CustomerOrder selectByOrderNoAndUserId(@Param("orderNo") String orderNo, 
                                         @Param("userId") String userId);

    // 根据订单编号和用户ID更新订单状态和删除标记
    int updateStatusAndDeletedByOrderNo(@Param("orderNo") String orderNo, 
                                      @Param("status") String status, 
                                      @Param("deleted") Integer deleted);

   
} 