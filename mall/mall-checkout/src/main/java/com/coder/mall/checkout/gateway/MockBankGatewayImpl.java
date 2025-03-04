package com.coder.mall.checkout.gateway;

// src/main/java/com/coder/mall/payment/service/gateway/MockBankGatewayImpl.java

import com.coder.mall.checkout.dto.PaymentResponse;
import com.coder.mall.checkout.entity.PaymentRecord;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service("bankPaymentGateway")
public class MockBankGatewayImpl implements BankGatewayService {

    @Override
    public PaymentResponse processPayment(PaymentRecord paymentRecord) {
        // 模拟银行支付处理逻辑
        boolean success = paymentRecord.getAmount().compareTo(BigDecimal.ZERO) > 0;

        PaymentResponse response = new PaymentResponse();
        response.setSuccess(success);
        response.setTransactionId(UUID.randomUUID().toString());
        response.setAmount(paymentRecord.getAmount());
        response.setPaymentTime(LocalDateTime.now());
        response.setMessage(success ? "支付成功" : "支付失败");

        return response;
    }

    @Override
    public BankQueryResponse queryPayment(String transactionId) {
        BankQueryResponse response = new BankQueryResponse();
        response.setSuccess(true);
        response.setStatus("SUCCESS");
        response.setTransactionId(transactionId);
        response.setAmount(new BigDecimal("100.00"));
        return response;
    }

    @Override
    public BankQueryResponse queryRefund(String refundId) {
        // 模拟退款查询逻辑
        BankQueryResponse response = new BankQueryResponse();
        response.setSuccess(true);
        response.setStatus("REFUND_SUCCESS");
        return response;
    }


}