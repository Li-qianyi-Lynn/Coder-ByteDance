package com.coder.mall.checkout.controller;

import com.coder.common.exception.BizException;
import com.coder.common.response.ApiResponse;
import com.coder.mall.checkout.dto.*;
import com.coder.mall.checkout.entity.CustomerOrder;
import com.coder.mall.checkout.entity.PaymentRecord;
import com.coder.mall.checkout.repository.CustomerOrderRepository;
import com.coder.mall.checkout.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final CustomerOrderRepository orderRepository;
    @Value("${http://localhost:3000/pay?orderNo={orderNo}}")
    private String paymentUrlTemplate;
    private String OrderNo;





    @Transactional
    @PostMapping("/create")
    public ApiResponse<OrderCreateResponse> createOrder(
            @Valid @RequestBody OrderCreateDTO request) {
        log.info("收到创建订单请求：{}", request.toString()); // 添加DTO的toString方法
        log.info("开始创建订单，用户ID：{}", request.getUserId());

        // 生成唯一订单号
        String orderNo = generateOrderNo();
        log.info("订单号：{}",orderNo);
        String paymentUrl = paymentUrlTemplate.replace("{orderNo}",orderNo);
        // 创建订单实体
        CustomerOrder order = new CustomerOrder();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setOrderItems(request.getOrderItems());
        order.setRecipientInfo(request.getRecipientInfo());
        order.setStatus("PENDING_PAYMENT");
        order.setCreateTime(LocalDateTime.now());
        // 计算订单总金额
        calculateOrderTotal(order);
        // 保存订单
        orderRepository.save(order);
        log.info("订单创建成功，订单号：{}", orderNo);

        return ApiResponse.success(
                OrderCreateResponse.builder()
                        .orderNo(orderNo)
                        .totalAmount(order.getTotalCost())
                        .paymentUrl(paymentUrl)
                        .build()
        );
    }




    // 支付处理接口
    @PostMapping("/process")
    public ApiResponse<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request) {

        log.info("开始处理支付，订单号：{}", request.getOrderNo());

        // 验证订单存在性
        validateOrderExists(request.getOrderNo());

        PaymentRecord record = new PaymentRecord();
        record.setAmount(request.getPaymentInfo().getAmount());
        record.setPaymentGateway("BANK_CARD");
        PaymentResponse response = paymentService.processOrderPayment(
                request.getOrderNo(),
                record
        );

        return ApiResponse.success(response);
    }

    // 订单取消接口（可选）
    @PostMapping("/cancel")
    public ApiResponse<OrderCancelResponseDTO> cancelOrder(
            @RequestParam String orderNo) {

        log.info("取消订单请求，订单号：{}", orderNo);

        CustomerOrder order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BizException("订单不存在"));

        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BizException("当前状态不可取消");
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);

        return ApiResponse.success(
                OrderCancelResponseDTO.builder()
                        .code("SUCCESS")
                        .message("订单取消成功")
                        .orderNo(orderNo)
                        .userId(order.getUserId())
                        .build()
        );
    }

    // 生成订单号（示例实现之后要删除）
    private String generateOrderNo() {
        return "ORD" +
                LocalDateTime.now().toString().replaceAll("[^0-9]", "") +
                UUID.randomUUID().toString().substring(0, 4);
    }

    // 计算订单总金额
    private void calculateOrderTotal(CustomerOrder order) {
        BigDecimal total = order.getOrderItems().stream()
                .map(item -> {
                    BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
                    int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                    BigDecimal discount = item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO;

                    return unitPrice.multiply(BigDecimal.valueOf(quantity)).subtract(discount);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalCost(total);
    }

    // 订单存在性验证
    private void validateOrderExists(String orderNo) {
        if (!orderRepository.existsByOrderNo(orderNo)) {
            log.error("订单不存在：{}", orderNo);
            throw new BizException("订单不存在");
        }
    }
}