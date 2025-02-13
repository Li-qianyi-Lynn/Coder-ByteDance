package com.coder.mall.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import com.coder.mall.order.model.dto.OrderCancelResponseDTO;
import com.coder.mall.order.model.dto.OrderCreateDTO;
import com.coder.mall.order.model.entity.CustomerOrder;
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
        try {
            orderService.cancelOrder(userId, orderNo);
            OrderCancelResponseDTO responseDTO = OrderCancelResponseDTO.builder()
                    .code("200")
                    .message("订单取消成功")
                    .orderNo(orderNo)
                    .userId(userId)
                    .build();
            return Response.success(responseDTO);
        } catch (BizException e) {
            log.error("Cancel order failed for orderNo: {}, error: {}", orderNo, e.getMessage());
            OrderCancelResponseDTO responseDTO = OrderCancelResponseDTO.builder()
                    .code(e.getErrorCode())
                    .message(e.getErrorMessage())
                    .orderNo(orderNo)
                    .userId(userId)
                    .build();
            return Response.fail(e);
        } catch (Exception e) {
            log.error("Cancel order failed for orderNo: {}", orderNo, e);
            OrderCancelResponseDTO responseDTO = OrderCancelResponseDTO.builder()
                    .code(OrderErrorEnum.ORDER_CANCEL_FAILED.getErrorCode())
                    .message(OrderErrorEnum.ORDER_CANCEL_FAILED.getErrorMessage())
                    .orderNo(orderNo)
                    .userId(userId)
                    .build();
            return Response.fail(OrderErrorEnum.ORDER_CANCEL_FAILED);
        }
    }

    @GetMapping
    public Response<Page<CustomerOrder>> listOrders(
            @RequestHeader("X-User-ID") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Listing orders for user: {}, page: {}, size: {}", userId, page, size);

        Page<CustomerOrder> orderPage = orderService.listCustomerOrders(userId, page, size);

        return Response.success(orderPage);
    }
}