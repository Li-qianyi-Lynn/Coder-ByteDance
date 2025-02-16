package com.coder.mall.order.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coder.framework.common.exception.BizException;
import com.coder.framework.common.response.Response;
import com.coder.mall.order.constant.OrderErrorEnum;
import com.coder.mall.order.model.dto.Address;
import com.coder.mall.order.model.dto.OrderCancelResponseDTO;
import com.coder.mall.order.model.dto.OrderCreateDTO;
import com.coder.mall.order.model.dto.PageResult;
import com.coder.mall.order.model.dto.RecipientInfo;
import com.coder.mall.order.model.entity.CustomerOrder;
import com.coder.mall.order.model.entity.OrderItem;
import com.coder.mall.order.service.OrderScheduleService;
import com.coder.mall.order.service.OrderService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private OrderScheduleService orderScheduleService;  // 注入接口而不是实现类

    @PostMapping("/from-cart")
    public Response<CustomerOrder> createOrderFromCart(@RequestHeader("X-User-ID") String userId) {
        try {
            CustomerOrder order = orderService.createOrderFromCart(userId);
            return Response.success(order);
        } catch (BizException e) {
            log.error("Create order from cart failed for user: {}, error: {}", userId, e.getMessage());
            return Response.fail(e);
        } catch (Exception e) {
            log.error("Create order from cart failed for user: {}", userId, e);
            return Response.fail(OrderErrorEnum.ORDER_CREATE_FAILED.getErrorCode());
        }
    }

    @PostMapping
    public Response<CustomerOrder> createOrder(
        @RequestHeader("X-User-ID") String userId,
        @RequestBody @Valid OrderCreateDTO orderCreateDTO) {
    try {
        orderCreateDTO.setUserId(userId);
        log.info("Creating order with DTO: {}", orderCreateDTO);  // 添加日志
        CustomerOrder order = orderService.createOrder(orderCreateDTO);
        return Response.success(order);
    } catch (Exception e) {
        log.error("Create order failed", e);  // 添加错误日志
        return Response.fail(OrderErrorEnum.ORDER_CREATE_FAILED);
    }
}

    @GetMapping("/{orderId}")
    public Response<CustomerOrder> getOrder(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String orderId) {
        try {
            CustomerOrder order = orderService.getCustomerOrder(userId, orderId);
            if (order == null) {
                throw new BizException(OrderErrorEnum.ORDER_NOT_FOUND);
            }
            return Response.success(order);
        } catch (BizException e) {
            log.error("Get order failed for orderId: {}, error: {}", orderId, e.getMessage());
            return Response.fail(e);
        } catch (Exception e) {
            log.error("Get order failed for orderId: {}", orderId, e);
            return Response.fail(OrderErrorEnum.ORDER_NOT_FOUND.getErrorCode());
        }
    }

  

    @PostMapping("/{orderNo}/cancel")
    public Response<OrderCancelResponseDTO> cancelOrder(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String orderNo) {
        log.info("开始取消订单 - userId: {}, orderNo: {}", userId, orderNo);
        try {
            // 添加Redis连接测试
            try {
                stringRedisTemplate.opsForValue().get("test-key");
                log.info("Redis连接正常");
            } catch (Exception e) {
                log.error("Redis连接异常", e);
            }

            orderService.cancelOrder(userId, orderNo);
            log.info("订单取消成功 - orderNo: {}", orderNo);
            
            OrderCancelResponseDTO responseDTO = OrderCancelResponseDTO.builder()
                    .code("200")
                    .message("订单取消成功")
                    .orderNo(orderNo)
                    .userId(userId)
                    .build();
            return Response.success(responseDTO);
        } catch (BizException e) {
            log.error("取消订单业务异常 - orderNo: {}, errorCode: {}, errorMessage: {}", 
                    orderNo, e.getErrorCode(), e.getErrorMessage());
            OrderCancelResponseDTO responseDTO = OrderCancelResponseDTO.builder()
                    .code(e.getErrorCode())
                    .message(e.getErrorMessage())
                    .orderNo(orderNo)
                    .userId(userId)
                    .build();
            return Response.fail(e);
        } catch (Exception e) {
            log.error("取消订单系统异常 - orderNo: {}", orderNo, e);
            OrderCancelResponseDTO responseDTO = OrderCancelResponseDTO.builder()
                    .code(OrderErrorEnum.ORDER_CANCEL_FAILED.getErrorCode())
                    .message(OrderErrorEnum.ORDER_CANCEL_FAILED.getErrorMessage())
                    .orderNo(orderNo)
                    .userId(userId)
                    .build();
            return Response.fail(OrderErrorEnum.ORDER_CANCEL_FAILED);
        }
    }

    @GetMapping("/list")
    public Response<PageResult<CustomerOrder>> listOrders(
            @RequestHeader("X-User-ID") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PageResult<CustomerOrder> orders = orderService.listCustomerOrders(userId, page, size);
            return Response.success(orders);
        } catch (Exception e) {
            log.error("获取用户订单列表失败: userId={}", userId, e);
            return Response.fail(OrderErrorEnum.ORDER_QUERY_FAILED);
        }
    }

    // 添加Redis测试端点
    @GetMapping("/redis-test")
    public Response<String> testRedis() {
        try {
            String testKey = "test:connection:" + System.currentTimeMillis();
            stringRedisTemplate.opsForValue().set(testKey, "test");
            String value = stringRedisTemplate.opsForValue().get(testKey);
            log.info("Redis测试成功 - key: {}, value: {}", testKey, value);
            return Response.success("Redis连接正常: " + value);
        } catch (Exception e) {
            log.error("Redis测试失败", e);
            return Response.fail("Redis连接失败: " + e.getMessage());
        }
    }

    /**
     * 测试订单自动取消功能
     * @param userId 用户ID
     * @param timeoutSeconds 超时时间（秒）
     */
    @PostMapping("/test-auto-cancel")
    public Response<CustomerOrder> testAutoCancel(
            @RequestHeader("X-User-ID") String userId,
            @RequestParam(defaultValue = "15") int timeoutSeconds) {
        try {
            // 1. 创建测试订单
            OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
            orderCreateDTO.setUserId(userId);
            
            // 创建测试订单项
            List<OrderItem> orderItems = new ArrayList<>();
            OrderItem item = new OrderItem();
            item.setProductId("TEST-PRODUCT-001");
            item.setQuantity(1);
            item.setUnitPrice(new BigDecimal("99.99"));
            orderItems.add(item);
            
            orderCreateDTO.setOrderItems(orderItems);
            
            // 设置收件人信息
            RecipientInfo recipientInfo = new RecipientInfo();
            recipientInfo.setName("测试用户");
            recipientInfo.setPhone("13800138000");
            recipientInfo.setAddress(new Address("测试国家", "测试省份", "测试城市", "测试区域", "测试街道","test"));
            orderCreateDTO.setRecipientInfo(recipientInfo);

            // 2. 创建订单（此时会自动添加30分钟的超时时间）
            CustomerOrder order = orderService.createOrder(orderCreateDTO);
            
            // 3. 覆盖默认的超时时间，设置为较短的测试时间
            orderScheduleService.removeFromTimeoutQueue(order.getOrderNo());  // 先移除默认的
            orderScheduleService.addOrderToTimeoutQueue(order.getOrderNo(), timeoutSeconds, TimeUnit.SECONDS);
            
            log.info("测试订单已创建: {}, 将在{}秒后自动取消", order.getOrderNo(), timeoutSeconds);
            
            // 查询当前队列状态
            try {
                Set<String> timeoutOrders = orderScheduleService.getTimeoutOrders();
                log.info("当前延迟队列中的订单: {}", timeoutOrders);
            } catch (Exception e) {
                log.error("查询延迟队列失败", e);
            }
            
            return Response.success(order);
        } catch (Exception e) {
            log.error("测试订单创建失败", e);
            return Response.fail(OrderErrorEnum.ORDER_CREATE_FAILED);
        }
    }

    /**
     * 查询订单状态
     */
    @GetMapping("/{orderNo}/status")
    public Response<String> getOrderStatus(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String orderNo) {
        try {
            CustomerOrder order = orderService.getCustomerOrder(userId, orderNo);
            return Response.success(order.getStatus());
        } catch (Exception e) {
            log.error("查询订单状态失败", e);
            return Response.fail(OrderErrorEnum.ORDER_NOT_FOUND);
        }
    }

    /**
     * 查询延迟队列中的订单（仅用于测试）
     */
    @GetMapping("/timeout-queue")
    public Response<Map<String, String>> getTimeoutQueue() {
        try {
            Set<String> orders = orderScheduleService.getTimeoutOrders();
            Map<String, String> result = new HashMap<>();
            
            for (String orderNo : orders) {
                String key = "order:timeout:" + orderNo;
                String expireTime = stringRedisTemplate.opsForValue().get(key);
                result.put(orderNo, expireTime);
            }
            
            return Response.success(result);
        } catch (Exception e) {
            log.error("查询延迟队列失败", e);
            return Response.fail(OrderErrorEnum.SYSTEM_ERROR);
        }
    }
}