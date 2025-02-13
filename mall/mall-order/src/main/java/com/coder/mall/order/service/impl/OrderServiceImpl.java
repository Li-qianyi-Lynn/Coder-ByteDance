package com.coder.mall.order.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.coder.framework.common.exception.BizException;
import com.coder.mall.order.constant.OrderErrorEnum;
import com.coder.mall.order.constant.OrderStatus;
import com.coder.mall.order.constant.RedisKeyConstant;
import com.coder.mall.order.mapper.CustomerOrderMapper;
import com.coder.mall.order.model.dto.Cart;
import com.coder.mall.order.model.dto.CartItem;
import com.coder.mall.order.model.dto.OrderCreateDTO;
import com.coder.mall.order.model.dto.PaymentInfo;
import com.coder.mall.order.model.dto.RecipientInfo;
import com.coder.mall.order.model.entity.CustomerOrder;
import com.coder.mall.order.model.entity.DealerOrder;
import com.coder.mall.order.model.entity.OrderItem;
import com.coder.mall.order.repository.CustomerOrderRepository;
import com.coder.mall.order.repository.DealerOrderRepository;
import com.coder.mall.order.service.OrderService;
import com.coder.mall.order.utils.OrderNoGenerator;
import com.coder.mall.order.utils.SequenceGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

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

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SequenceGenerator sequenceGenerator;

    @Autowired
    private OrderNoGenerator orderNoGenerator;

    @Autowired
    private CustomerOrderMapper customerOrderMapper;

    @Autowired
    private ObjectMapper objectMapper;  // JSON 处理

    private BigDecimal calculateTotalCost(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return items.stream()
                .map(item -> {
                    // 如果没有折扣，默认为1
                    BigDecimal discount = item.getDiscount() != null ? 
                        item.getDiscount() : BigDecimal.ONE;
                    
                    // 计算实际价格 = 单价 * 折扣 * 数量
                    return item.getUnitPrice()
                            .multiply(discount)
                            .multiply(new BigDecimal(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private List<OrderItem> convertCartToOrderItems(Cart cart) {  //购物车项转换为订单项
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
                            .setScale(2, RoundingMode.HALF_UP); //保留2位小数

                    orderItem.setActualPrice(itemCost);
                    return orderItem;
                })
                .collect(Collectors.toList());
    }

    @Override
    public CustomerOrder getCustomerOrder(String userId, String orderNo) {
        log.info("Fetching order by orderNo: {} for user: {}", orderNo, userId);
        try {
            // 1. 参数校验
            if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(orderNo)) {
                throw new BizException(OrderErrorEnum.PARAM_ERROR);
            }

            // 2. 先从缓存获取
            CustomerOrder order = getOrderFromCache(orderNo);
            
            // 3. 缓存未命中，从数据库获取
            if (order == null) {
                order = customerOrderMapper.selectByOrderNoAndUserId(orderNo, userId);
                if (order == null) {
                    throw new BizException(OrderErrorEnum.ORDER_NOT_FOUND);
                }
                
                // 放入缓存
                cacheOrder(order);
            }
            
            // 4. 验证订单所属
            if (!userId.equals(order.getUserId())) {
                log.warn("Order {} does not belong to user {}", orderNo, userId);
                throw new BizException(OrderErrorEnum.ORDER_NOT_BELONGS_TO_USER);
            }

            return order;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Get customer order failed: orderNo={}, userId={}", orderNo, userId, e);
            throw new BizException(OrderErrorEnum.SYSTEM_ERROR);
        }
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
        if (!OrderStatus.CREATED.equals(order.getStatus())) {
            throw new BizException(OrderErrorEnum.ORDER_STATUS_INVALID);
        }

        // 2. 更新订单状态
        order.setStatus(OrderStatus.PENDING_PAYMENT.name());
        customerOrderRepository.save(order);

        // 3. 发送消息到MQ
        rabbitTemplate.convertAndSend(ORDER_EXCHANGE, ORDER_ROUTING_KEY, order);
        log.info("Order placed and message sent: {}", orderId);
    }

    @Override
    @Transactional
    public void cancelOrder(String userId, String orderNo) {
        log.info("Cancelling order: {} for user: {}", orderNo, userId);
        try {
            // 1. 参数校验
            if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(orderNo)) {
                throw new BizException(OrderErrorEnum.PARAM_ERROR);
            }

            // 2. 先从缓存获取订单
            CustomerOrder order = getOrderFromCache(orderNo);
            
            // 3. 缓存未命中，从数据库获取
            if (order == null) {
                order = customerOrderMapper.selectByOrderNoAndUserId(orderNo, userId);
                if (order == null) {
                    throw new BizException(OrderErrorEnum.ORDER_NOT_FOUND);
                }
            }

            // 4. 验证订单所属
            if (!userId.equals(order.getUserId())) {
                log.warn("Order {} does not belong to user {}", orderNo, userId);
                throw new BizException(OrderErrorEnum.ORDER_NOT_BELONGS_TO_USER);
            }

            // 5. 验证订单状态是否可取消
            if (!OrderStatus.valueOf(order.getStatus()).canCancel()) {
                log.warn("Order status cannot be cancelled, current status: {}", order.getStatus());
                throw new BizException(OrderErrorEnum.ORDER_STATUS_INVALID);
            }

            // 6. 更新订单状态
            order.setStatus(OrderStatus.CANCELLED.name());
            order.setUpdateTime(new Date());
            
            // 7. 保存到数据库（同时更新状态和删除标记）
            int rows = customerOrderMapper.updateStatusAndDeletedByOrderNo(orderNo, OrderStatus.CANCELLED.name(), 1);
            if (rows != 1) {
                throw new BizException(OrderErrorEnum.ORDER_CANCEL_FAILED);
            }

            // 8. 删除缓存
            String idKey = RedisKeyConstant.ORDER_CACHE + ":id:" + order.getOrderId();
            String noKey = RedisKeyConstant.ORDER_CACHE + ":no:" + orderNo;
            redisTemplate.delete(Arrays.asList(idKey, noKey));

            log.info("Order cancelled and soft deleted successfully: {}", orderNo);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Cancel order failed: {}", orderNo, e);
            throw new BizException(OrderErrorEnum.ORDER_CANCEL_FAILED);
        }
    }

    private void cacheOrder(CustomerOrder order) {
        try {
            String orderJson = objectMapper.writeValueAsString(order);
            
            redisTemplate.execute(new SessionCallback<List<Object>>() {
                @Override
                public List<Object> execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    
                    String idKey = RedisKeyConstant.ORDER_CACHE + ":id:" + order.getOrderId();
                    String noKey = RedisKeyConstant.ORDER_CACHE + ":no:" + order.getOrderNo();
                    
                    operations.opsForValue().set(idKey, orderJson, RedisKeyConstant.ORDER_CACHE_HOURS, TimeUnit.HOURS);
                    operations.opsForValue().set(noKey, orderJson, RedisKeyConstant.ORDER_CACHE_HOURS, TimeUnit.HOURS);
                    
                    return operations.exec();
                }
            });
            
            log.info("Cached order: {}", order.getOrderNo());
        } catch (Exception e) {
            log.error("Cache order failed: {}", order.getOrderNo(), e);
        }
    }
    
    private CustomerOrder getOrderFromCache(String orderNo) {
        try {
            String cacheKey = RedisKeyConstant.ORDER_CACHE + ":no:" + orderNo;
            String orderJson = (String) redisTemplate.opsForValue().get(cacheKey);
            
            if (orderJson != null) {
                return objectMapper.readValue(orderJson, CustomerOrder.class);
            }
        } catch (Exception e) {
            log.warn("Get order from cache failed: {}", orderNo, e);
            redisTemplate.delete(RedisKeyConstant.ORDER_CACHE + ":no:" + orderNo);
        }
        return null;
    }

    private void clearCart(String userId) {
        try {
            String cartKey = RedisKeyConstant.CART + ":" + userId;
            Boolean result = redisTemplate.delete(cartKey);
            log.info("Cleared cart for user: {}, result: {}", userId, result);
        } catch (Exception e) {
            log.error("Clear cart failed for user: {}", userId, e);
            throw new BizException(OrderErrorEnum.SYSTEM_ERROR);
        }
    }

    private Cart getCartFromRedis(String userId) {
        try {
            String cartKey = RedisKeyConstant.CART + ":" + userId;
            String cartJson = (String) redisTemplate.opsForValue().get(cartKey);
            
            if (cartJson != null) {
                return objectMapper.readValue(cartJson, Cart.class);
            }
        } catch (Exception e) {
            log.error("Get cart from redis failed: {}", userId, e);
            throw new BizException(OrderErrorEnum.SYSTEM_ERROR);
        }
        return null;
    }

    public void testOrderData(String userId) {
        //分页查询
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<CustomerOrder> orderPage = customerOrderRepository.findByUserId(userId, pageable);

        log.info("Total elements: {}", orderPage.getTotalElements());
        log.info("Total pages: {}", orderPage.getTotalPages());
        log.info("Current page number: {}", orderPage.getNumber());

        List<CustomerOrder> orders = orderPage.getContent();
        orders.forEach(order -> {
            log.info("Order ID: {}", order.getOrderId());
            log.info("Total Cost: {}", order.getTotalCost());
            log.info("Status: {}", order.getStatus());
            log.info("Items count: {}", order.getOrderItems().length());
        });
    }

    @Override
    public Page<CustomerOrder> listCustomerOrders(String userId, int page, int size) {
        log.info("Fetching orders for user: {}, page: {}, size: {}", userId, page, size);

        // 创建分页请求
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));

        // 执行分页查询
        Page<CustomerOrder> orderPage = customerOrderRepository.findByUserId(userId, pageable);

        log.info("Found {} orders, total pages: {}",
                orderPage.getNumberOfElements(),
                orderPage.getTotalPages());

        return orderPage;
    }

    @Override
    public CustomerOrder createOrder(OrderCreateDTO orderCreateDTO) {
        try {
            CustomerOrder order = new CustomerOrder();
            order.setOrderNo(orderNoGenerator.generateOrderNo());
            order.setUserId(orderCreateDTO.getUserId());
            order.setTotalCost(calculateTotalCost(orderCreateDTO.getOrderItems()));
            order.setStatus(OrderStatus.PENDING_PAYMENT.name());
            
            // 转换复杂对象为 JSON
            order.setRecipientInfo(objectMapper.writeValueAsString(orderCreateDTO.getRecipientInfo()));
            order.setOrderItems(objectMapper.writeValueAsString(orderCreateDTO.getOrderItems()));
            
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            
            // 保存到 MySQL
            customerOrderMapper.insert(order);
            
            // 缓存到 Redis
            String cacheKey = "order:" + order.getOrderId();
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(order));
            
            return order;
        } catch (Exception e) {
            log.error("Create order failed", e);
            throw new BizException(OrderErrorEnum.ORDER_CREATE_FAILED);
        }
    }

    @Override
    @Transactional
    public CustomerOrder createOrderFromCart(String userId) {
        log.info("Creating order from cart for user: {}", userId);
        try {
            // 1. 参数校验
            if (StringUtils.isEmpty(userId)) {
                throw new BizException(OrderErrorEnum.PARAM_ERROR);
               
            }

            // 2. 获取购物车信息
            Cart cart = getCartFromRedis(userId);
            if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                throw new BizException(OrderErrorEnum.CART_EMPTY);
            }

            // 3. 创建订单
            CustomerOrder order = new CustomerOrder();
            order.setOrderNo(orderNoGenerator.generateOrderNo());
            order.setUserId(userId);
            
            try {
                // 4. 处理订单项
                List<OrderItem> orderItems = cart.getCartItems().stream()
                        .map(OrderItem::fromCartItem)
                        .collect(Collectors.toList());
                
                // 5. 设置订单信息
                order.setOrderItems(objectMapper.writeValueAsString(orderItems));
                order.setTotalCost(calculateTotalCost(orderItems));
                order.setStatus(OrderStatus.PENDING_PAYMENT.name());  
                order.setCreateTime(new Date());
                order.setUpdateTime(new Date());

                // 6. 保存订单到 MySQL
                int rows = customerOrderMapper.insert(order);
                if (rows != 1) {
                    throw new BizException(OrderErrorEnum.ORDER_CREATE_FAILED);
                }
                
                // 7. 缓存订单
                String cacheKey = "order:" + order.getOrderId();
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(order), 1, TimeUnit.HOURS);

                // 8. 清空购物车
                clearCart(userId);

                log.info("Created order successfully: {}, status: {}", order.getOrderId(), order.getStatus());
                return order;
                
            } catch (JsonProcessingException e) {
                log.error("JSON processing failed", e);
                throw new BizException(OrderErrorEnum.SYSTEM_ERROR);
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Create order from cart failed for user: {}", userId, e);
            throw new BizException(OrderErrorEnum.ORDER_CREATE_FAILED);
        }
    }
}