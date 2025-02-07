package com.coder.mall.order.controller;

import com.coder.common.exception.BizException;
import com.coder.common.response.Response;
import com.coder.mall.order.constant.OrderErrorEnum;
import com.coder.mall.order.model.entity.CustomerOrder;
import com.coder.mall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public Response<String> createOrder(@RequestHeader("X-User-ID") String userId) {
        try {
            String orderId = orderService.createCustomerOrder(userId);
            return Response.success(orderId);
        } catch (BizException e) {
            log.error("Create order failed for user: {}, error: {}", userId, e.getMessage());
            return Response.fail(e);
        } catch (Exception e) {
            log.error("Create order failed for user: {}", userId, e);
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
            return Response.fail(OrderErrorEnum.ORDER_NOT_FOUND);
        }
    }

    @PostMapping("/{orderId}/cancel")
    public Response<Void> cancelOrder(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String orderId) {
        try {
            orderService.cancelOrder(userId, orderId);
            return Response.success();
        } catch (BizException e) {
            log.error("Cancel order failed for orderId: {}, error: {}", orderId, e.getMessage());
            return Response.fail(e);
        } catch (Exception e) {
            log.error("Cancel order failed for orderId: {}", orderId, e);
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