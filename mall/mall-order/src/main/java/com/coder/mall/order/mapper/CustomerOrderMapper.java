package com.coder.mall.order.mapper;

import java.sql.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.coder.mall.order.constant.OrderStatus;
import com.coder.mall.order.model.entity.CustomerOrder;

@Mapper
public interface CustomerOrderMapper {
    int insert(CustomerOrder order);
    
    CustomerOrder selectById(@Param("orderId") Long orderId);
    
    // 不带分页的查询
    List<CustomerOrder> selectByUserId(@Param("userId") Long userId);
    
    // 带分页的查询
    List<CustomerOrder> selectByUserIdWithPage(@Param("userId") Long userId, 
                                             @Param("offset") int offset, 
                                             @Param("limit") int limit);
    
    int updateStatus(@Param("orderNo") String orderNo, @Param("status") OrderStatus status);

    CustomerOrder selectByOrderNoAndUserId(@Param("orderNo") String orderNo, 
                                         @Param("userId") Long userId);

    int updateStatusAndDeletedByOrderNo(@Param("orderNo") String orderNo, 
                                      @Param("status") String status,
                                      @Param("deleted") Integer deleted);

    CustomerOrder selectByOrderNo(@Param("orderNo") String orderNo);

    int countByUserId(@Param("userId") long userId);

    List<CustomerOrder> selectHistoryOrders(@Param("userId") Long userId,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("offset") int offset,
                                          @Param("limit") int limit);

    int countHistoryOrders(@Param("userId") Long userId,
                          @Param("startDate") String startDate,
                          @Param("endDate") String endDate);

    int updateOrder(CustomerOrder order);

    int countHistoryOrdersForDealer(@Param("dealerId") Long dealerId,
                                   @Param("startDate") String startDate,
                                   @Param("endDate") String endDate);

    int countCustomerOrders(@Param("userId") Long userId);

    List<CustomerOrder> selectCustomerOrders(@Param("userId") Long userId,
                                          @Param("offset") int offset,
                                          @Param("size") int size);

    /**
     * 更新订单支付信息
     */
    int updatePaymentInfo(@Param("orderNo") String orderNo, 
                         @Param("paymentTime") Date paymentTime, 
                         @Param("paymentStatus") OrderStatus paymentStatus);

    /**
     * 更新订单支付状态
     * @param orderNo 订单号
     * @param paymentStatus 支付状态
     * @return 影响的行数
     */
    int updatePaymentStatus(@Param("orderNo") String orderNo, 
                           @Param("paymentStatus") OrderStatus paymentStatus);
}