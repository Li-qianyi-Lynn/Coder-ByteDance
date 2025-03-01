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
import com.coder.mall.order.model.dto.ListCustomerHistoryDTO;
import com.coder.mall.order.model.dto.OrderCancelDTO;
import com.coder.mall.order.model.dto.OrderCancelResponseDTO;
import com.coder.mall.order.model.dto.OrderCreateDTO;
import com.coder.mall.order.model.dto.OrderGetDTO;
import com.coder.mall.order.model.dto.OrderPlaceDTO;
import com.coder.mall.order.model.entity.Address;
import com.coder.mall.order.model.entity.CustomerOrder;
import com.coder.mall.order.model.entity.OrderItem;
import com.coder.mall.order.model.entity.PageResult;
import com.coder.mall.order.model.entity.RecipientInfo;
import com.coder.mall.order.service.OrderScheduleService;
import com.coder.mall.order.service.OrderService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private OrderScheduleService orderScheduleService; 

    @Autowired
    private ObjectMapper objectMapper;

    // @PostMapping("/from-cart")
    // public Response<CustomerOrder> createOrderFromCart(@RequestHeader("X-User-ID") String userId) {
    //     try {
    //         CustomerOrder order = orderService.createOrderFromCart(userId);
    //         return Response.success(order);
    //     } catch (BizException e) {
    //         log.error("Create order from cart failed for user: {}, error: {}", userId, e.getMessage());
    //         return Response.fail(e);
    //     } catch (Exception e) {
    //         log.error("Create order from cart failed for user: {}", userId, e);
    //         return Response.fail(OrderErrorEnum.ORDER_CREATE_FAILED.getErrorCode());
    //     }
    // }

    /**
     * 创建订单 已完成
     */
    @PostMapping
    public Response<CustomerOrder> createOrder(
            @RequestHeader("X-User-ID") Long userId,
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid OrderCreateDTO orderCreateDTO) {
        try {
            orderCreateDTO.setUserId(userId);
            log.info("Creating order with DTO: {}", orderCreateDTO);  
            CustomerOrder order = orderService.createCustomerOrder(orderCreateDTO);
            return Response.success(order);
        } catch (Exception e) {
            log.error("Create order failed", e); 
            return Response.fail(OrderErrorEnum.ORDER_CREATE_FAILED);
        }
    }

    /**
     * 查询订单 已完成
     */
    @GetMapping("/{orderNo}")
    public Response<CustomerOrder> getOrder(
            @RequestHeader("X-User-ID") Long userId,
            @RequestHeader("Authorization") String token,
            @PathVariable String orderNo) {
            
        log.info("接收到查询订单请求, orderNo: {}, userId: {}", orderNo, userId);
        
        try {
            OrderGetDTO orderGetDTO = new OrderGetDTO();
            orderGetDTO.setOrderNo(orderNo);
            orderGetDTO.setUserId(userId);
            orderGetDTO.setToken(token);
            
            log.info("构建查询参数: {}", orderGetDTO);
            
            CustomerOrder order = orderService.getCustomerOrder(orderGetDTO);
            return Response.success(order);
            
        } catch (BizException e) {
            log.warn("业务异常: orderNo={}, userId={}, error={}", orderNo, userId, e.getMessage());
            return Response.fail(e);
        } catch (Exception e) {
            log.error("系统异常: orderNo={}, userId={}", orderNo, userId, e);
            return Response.fail(OrderErrorEnum.ORDER_GET_FAILED);
        }
    }

  
    /**
     * 取消订单 已完成
     */
    @PostMapping("/{orderNo}/cancel")
    public Response<OrderCancelResponseDTO> cancelOrder(
            @RequestHeader("X-User-ID") Long userId,
            @RequestHeader("Authorization") String token,
            @PathVariable String orderNo) {
        log.info("开始取消订单 - userId: {}, orderNo: {}", userId, orderNo);
        try {
            // 创建取消订单DTO并设置必要参数
            OrderCancelDTO cancelDTO = new OrderCancelDTO();
            cancelDTO.setOrderNo(orderNo);
            cancelDTO.setUserId(userId);
            
            orderService.cancelOrder(cancelDTO);  // 传入正确设置的DTO
            log.info("订单取消成功 - orderNo: {}", orderNo);
            
            OrderCancelResponseDTO responseDTO = OrderCancelResponseDTO.builder()
                    .code("200")
                    .message("订单取消成功")
                    .orderNo(orderNo)
                    .userId(userId.toString())
                    .build();
            return Response.success(responseDTO);
        } catch (BizException e) {
            log.error("取消订单业务异常 - orderNo: {}, errorCode: {}, errorMessage: {}", 
                    orderNo, e.getErrorCode(), e.getErrorMessage());
            OrderCancelResponseDTO responseDTO = OrderCancelResponseDTO.builder()
                    .code(e.getErrorCode())
                    .message(e.getErrorMessage())
                    .orderNo(orderNo)
                    .userId(userId.toString())
                    .build();
            return Response.fail(e);
        } catch (Exception e) {
            log.error("取消订单系统异常 - orderNo: {}", orderNo, e);
            OrderCancelResponseDTO responseDTO = OrderCancelResponseDTO.builder()
                    .code(OrderErrorEnum.ORDER_CANCEL_FAILED.getErrorCode())
                    .message("系统异常，请稍后再试")
                    .orderNo(orderNo)
                    .userId(userId.toString())
                    .build();
            return Response.fail(OrderErrorEnum.ORDER_CANCEL_FAILED);
        }
    }

    /**
     * 查询用户订单列表 已完成
     */
    @GetMapping("/list")
    public Response<PageResult<CustomerOrder>> listOrders(
            @RequestHeader("X-User-ID") Long userId,
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            ListCustomerHistoryDTO listDTO = new ListCustomerHistoryDTO();
            listDTO.setUserId(userId);
            listDTO.setPage(page);
            listDTO.setPageSize(size);
            
            log.info("查询用户订单列表，参数：{}", listDTO);
            PageResult<CustomerOrder> orders = orderService.listCustomerHistoryOrders(listDTO);
            return Response.success(orders);
        } catch (Exception e) {
            log.error("获取用户订单列表失败: userId={}, page={}, size={}", userId, page, size, e);
            return Response.fail(OrderErrorEnum.ORDER_QUERY_FAILED);
        }
    }


    /**
     * 查询订单状态 已完成
     */
    @GetMapping("/{orderNo}/status")
    public Response<String> getOrderStatus(
            @RequestHeader("X-User-ID") Long userId,
            @RequestHeader("Authorization") String token,
            @PathVariable String orderNo) {
        try {
            // 创建并设置 OrderGetDTO
            OrderGetDTO orderGetDTO = new OrderGetDTO();
            orderGetDTO.setOrderNo(orderNo);
            orderGetDTO.setUserId(userId);
            orderGetDTO.setToken(token);
            
            log.info("查询订单状态，参数：{}", orderGetDTO);
            
            CustomerOrder order = orderService.getCustomerOrder(orderGetDTO);
            return Response.success(order.getStatus().toString());
        } catch (BizException e) {
            log.warn("查询订单状态失败：{}", e.getMessage());
            return Response.fail(e);
        } catch (Exception e) {
            log.error("查询订单状态失败", e);
            return Response.fail(OrderErrorEnum.ORDER_NOT_FOUND);
        }
    }

    /**
     * 确认订单并获取支付链接
     * @param userId 用户ID
     * @param token 用户token
     * @param orderNo 订单号
     * @return 包含支付链接的订单信息
     */
    @PostMapping("/{orderNo}/placeorder")
    public Response<Map<String, Object>> placeOrder(
            @RequestHeader("X-User-ID") Long userId,
            @RequestHeader("Authorization") String token,
            @PathVariable String orderNo) {
            
        log.info("接收到确认订单请求 - orderNo: {}, userId: {}, token: {}", orderNo, userId, token);
        
        // 参数校验
        if (orderNo == null || userId == null) {
            log.warn("订单确认参数为空 - orderNo: {}, userId: {}", orderNo, userId);
            return Response.fail(OrderErrorEnum.PARAM_ERROR);
        }
        
        try {
            // 构建确认订单DTO
            OrderPlaceDTO orderPlaceDTO = OrderPlaceDTO.builder()  // 使用建造者模式
                .orderNo(orderNo)
                .userId(userId)
                .token(token)
                .build();
            
            // 调用服务确认订单
            CustomerOrder order = orderService.placeOrder(orderPlaceDTO);
            
            if (order == null) {
                log.warn("订单确认失败，未找到订单 - orderNo: {}", orderNo);
                return Response.fail(OrderErrorEnum.ORDER_NOT_FOUND);
            }
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("orderNo", order.getOrderNo());
            result.put("totalAmount", order.getTotalCost());
            
            // 从订单的paymentInfo中提取支付链接
            if (order.getPaymentInfo() != null) {
                Map<String, String> paymentInfo = objectMapper.readValue(
                    order.getPaymentInfo(), 
                    new TypeReference<Map<String, String>>() {}
                );
                result.put("paymentUrl", paymentInfo.get("paymentUrl"));
            }
            
            log.info("订单确认成功，返回支付信息：{}", result);
            return Response.success(result);
            
        } catch (BizException e) {
            log.warn("确认订单业务异常 - orderNo: {}, error: {}", orderNo, e.getMessage());
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("确认订单系统异常 - orderNo: {}, error: {}", orderNo, e.getMessage(), e);
            return Response.fail(OrderErrorEnum.ORDER_PLACE_FAILED);
        }
    }

    //测试用
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
            @RequestHeader("X-User-ID") Long userId,
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "15") int timeoutSeconds) {
        try {
            // 创建测试订单
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
            recipientInfo.setRecipientName("测试用户");
            recipientInfo.setPhone("13800138000");
            recipientInfo.setAddress(new Address("测试国家", "测试省份", "测试城市", "测试区域", "测试街道","test"));
            orderCreateDTO.setRecipientInfo(recipientInfo);

            // 2. 创建订单（此时会自动添加30分钟的超时时间）
            CustomerOrder order = orderService.createCustomerOrder(orderCreateDTO);
            
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