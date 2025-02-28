package com.coder.mall.checkout.service;




import com.coder.common.exception.BizException;
import com.coder.mall.checkout.dto.PaymentResponse;
import com.coder.mall.checkout.entity.CustomerOrder;
import com.coder.mall.checkout.entity.PaymentRecord;
import com.coder.mall.checkout.gateway.BankGatewayService;



import com.coder.mall.checkout.repository.CustomerOrderRepository;
import com.coder.mall.checkout.repository.PaymentRecordRepository;



import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BankGatewayService bankGatewayService;
    private final CustomerOrderRepository orderRepository;
    private final PaymentRecordRepository paymentRecordRepository;


    @Transactional
    public PaymentResponse processOrderPayment(String orderNo, PaymentRecord paymentRecord) {
        // 1. 验证订单
        CustomerOrder order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BizException("订单不存在"));

        // 2. 检查订单状态
        if (!order.getStatus().equals("PENDING_PAYMENT")) {
            throw new BizException("当前订单状态不允许支付");
        }

        // 3. 创建支付记录
        paymentRecord.setOrderNo(orderNo);
        paymentRecord.setCreatedTime(LocalDateTime.now());
        paymentRecord.setPaymentStatus("PROCESSING");

        // 4. 调用支付网关
        PaymentResponse response = bankGatewayService.processPayment(paymentRecord);

        // 5. 更新支付记录
        paymentRecord.setTransactionId(response.getTransactionId());
        paymentRecord.setPaymentStatus(response.isSuccess() ? "SUCCESS" : "FAILED");
        paymentRecord.setUpdatedTime(LocalDateTime.now());
        paymentRecordRepository.save(paymentRecord);

        // 6. 更新订单状态
        if (response.isSuccess()) {
            order.setStatus("PAID");
            orderRepository.save(order);
        }

        return response;
    }
}