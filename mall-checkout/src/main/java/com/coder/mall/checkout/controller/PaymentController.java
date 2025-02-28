package com.coder.mall.checkout.controller;

import com.coder.common.exception.BizException;
import com.coder.common.response.ApiResponse;
import com.coder.mall.checkout.dto.*;
import com.coder.mall.checkout.entity.CustomerOrder;
import com.coder.mall.checkout.entity.OrderItem;
import com.coder.mall.checkout.entity.PaymentRecord;
import com.coder.mall.checkout.repository.CustomerOrderRepository;
import com.coder.mall.checkout.service.ExternalOrderClient;
import com.coder.mall.checkout.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final CustomerOrderRepository orderRepository;
    private final ExternalOrderClient externalOrderClient;
    @Value("${http://localhost:3000/pay?orderNo={orderNo}}")
    private String paymentUrlTemplate;
    private String OrderNo;





    @Transactional
    @PostMapping("/createurl")
    public ApiResponse<OrderCreateResponse> createOrder(
            @RequestParam String orderNo,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("Authorization") String token) {

        // 调用外部订单服务
        ApiResponse<ExternalOrderResponse> response = externalOrderClient.getOrder(orderNo, userId, token);
        if (!response.isSuccess()) {
            throw new BizException("获取订单失败: " + response.getMessage());
        }

        // 转换订单数据
        CustomerOrder order = convertToOrder(response.getData());

        // 保存到本地数据库
        orderRepository.save(order);

        // 生成支付链接
        String paymentUrl = paymentUrlTemplate.replace("{orderNo}", orderNo);

        return ApiResponse.success(
                OrderCreateResponse.builder()
                        .orderNo(orderNo)
                        .totalAmount(order.getTotalCost())
                        .paymentUrl(paymentUrl)
                        .build());
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

    private CustomerOrder convertToOrder(ExternalOrderResponse external) {
        CustomerOrder order = new CustomerOrder();
        order.setOrderNo(external.getOrderNo());
        order.setUserId(String.valueOf(external.getUserId()));
        order.setTotalCost(external.getTotalCost());
        order.setCreateTime(external.getCreateTime());

        // 转换收货信息
        RecipientInfo recipient = new RecipientInfo();
        recipient.setName(external.getRecipientInfo().getName());
        recipient.setPhone(external.getRecipientInfo().getPhone());

        Address address = new Address();
        address.setProvince(external.getRecipientInfo().getAddress().getProvince());
        address.setCity(external.getRecipientInfo().getAddress().getCity());
        address.setDistrict(external.getRecipientInfo().getAddress().getDistrict());
        address.setDetail(external.getRecipientInfo().getAddress().getDetail());
        recipient.setAddress(address);

        order.setRecipientInfo(recipient);

        // 转换订单项
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<OrderItem> items = mapper.readValue(external.getOrderItems(),
                    new TypeReference<List<OrderItem>>() {});
            order.setOrderItems(items);
        } catch (JsonProcessingException e) {
            throw new BizException("订单项解析失败");
        }

        return order;
    }

}