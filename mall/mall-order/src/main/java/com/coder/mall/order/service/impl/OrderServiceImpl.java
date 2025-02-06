package com.coder.mall.order.service.impl;

import com.coder.common.exception.BizException;
import com.coder.mall.order.constant.OrderErrorEnum;
import com.coder.mall.order.constant.OrderStatus;
import com.coder.mall.order.model.dto.Cart;
import com.coder.mall.order.model.dto.CartItem;

import com.coder.mall.order.model.dto.PaymentInfo;
import com.coder.mall.order.model.dto.RecipientInfo;
import com.coder.mall.order.model.entity.CustomerOrder;
import com.coder.mall.order.model.entity.DealerOrder;
import com.coder.mall.order.model.entity.OrderItem;
import com.coder.mall.order.repository.CustomerOrderRepository;
import com.coder.mall.order.repository.DealerOrderRepository;
import com.coder.mall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_QUEUE = "order.queue";
    private static final String ORDER_EXCHANGE = "order.exchange";
    private static final String ORDER_ROUTING_KEY = "order.routing.key";

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private DealerOrderRepository dealerOrderRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public String createCustomerOrder(String userId) {
        log.info("Creating order for user: {}", userId);
        try {
            // 1. 获取购物车信息
            Cart cart = getCartFromRedis(userId);
            if (cart == null || cart.getCartItems().isEmpty()) {
                throw new BizException(OrderErrorEnum.CART_EMPTY);
            }

            // 2. 创建订单
            CustomerOrder order = new CustomerOrder();
            order.setUserId(userId);
            order.setOrderItems(convertCartToOrderItems(cart));
            order.setStatus(OrderStatus.CREATED);
            order.setTotalCost(calculateTotalCost(order.getOrderItems()));

            // 3. 保存订单
            CustomerOrder savedOrder = customerOrderRepository.save(order);

            // 4. 清空购物车
            clearCart(userId);

            log.info("Successfully created order: {} for user: {}", savedOrder.getOrderId(), userId);
            return savedOrder.getOrderId();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create order for user: {}", userId, e);
            throw new BizException(OrderErrorEnum.ORDER_CREATE_FAILED);
        }
    }

    private BigDecimal calculateTotalCost(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return orderItems.stream()
                .map(item -> {
                    BigDecimal unitPrice = item.getCartItem().getUnitPrice();
                    int quantity = item.getCartItem().getQuantity();
                    return unitPrice.multiply(new BigDecimal(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);  // 保留两位小数，四舍五入


    }

    private List<OrderItem> convertCartToOrderItems(Cart cart) {
        if (cart == null || cart.getCartItems() == null) {
            throw new BizException(OrderErrorEnum.CART_EMPTY);
        }

        return cart.getCartItems().stream()
                .filter(CartItem::getIsValid)  // 只转换有效的购物车项
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setCartItem(cartItem);

                    // 计算单个商品的总价
                    BigDecimal itemCost = cartItem.getUnitPrice()
                            .multiply(new BigDecimal(cartItem.getQuantity()))
                            .setScale(2, RoundingMode.HALF_UP);

                    orderItem.setCost(itemCost);
                    return orderItem;
                })
                .collect(Collectors.toList());
    }

    @Override
    public CustomerOrder getCustomerOrder(String userId, String orderId) {
        log.info("Fetching order: {} for user: {}", orderId, userId);
        return customerOrderRepository.findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(() -> new BizException(OrderErrorEnum.ORDER_NOT_FOUND));
    }

    @Override
    public DealerOrder getDealerOrder(String dealerId, String orderId) {
        return null;
    }

    @Override
    public List<CustomerOrder> listCustomerHistoryOrders(String userId, LocalDateTime startDate, LocalDateTime endDate, int page, int pageSize) {
        return null;
    }

    @Override
    public List<DealerOrder> listDealerHistoryOrders(String dealerId, LocalDateTime startDate, LocalDateTime endDate, int page, int pageSize) {
        return null;
    }

    @Override
    public CustomerOrder updateOrder(String userId, String orderId, List<OrderItem> orderItems, RecipientInfo recipientInfo, PaymentInfo paymentInfo, String extraInfo) {
        return null;
    }

    @Override
    @Transactional
    public void placeOrder(String userId, String orderId) {
        log.info("Placing order: {} for user: {}", orderId, userId);
        CustomerOrder order = getCustomerOrder(userId, orderId);

        // 1. 验证订单状态
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BizException(OrderErrorEnum.ORDER_STATUS_INVALID);
        }

        // 2. 更新订单状态
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        customerOrderRepository.save(order);

        // 3. 发送消息到MQ
        rabbitTemplate.convertAndSend(ORDER_EXCHANGE, ORDER_ROUTING_KEY, order);
        log.info("Order placed and message sent: {}", orderId);
    }

    @Override
    @Transactional
    public void cancelOrder(String userId, String orderId) {
        log.info("Cancelling order: {} for user: {}", orderId, userId);
        CustomerOrder order = getCustomerOrder(userId, orderId);

        // 验证订单是否可以取消
        if (!order.getStatus().canCancel()) {
            throw new BizException(OrderErrorEnum.ORDER_STATUS_INVALID);
        }

        // 更新订单状态
        order.setStatus(OrderStatus.CANCELLED);
        customerOrderRepository.save(order);
        log.info("Order cancelled: {}", orderId);
    }


    private Cart getCartFromRedis(String userId) {
        String cartKey = "cart:" + userId;
        return (Cart) redisTemplate.opsForValue().get(cartKey);
    }

    private void clearCart(String userId) {
        String cartKey = "cart:" + userId;
        redisTemplate.delete(cartKey);
    }
}