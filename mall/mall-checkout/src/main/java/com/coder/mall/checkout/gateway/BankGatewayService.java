package com.coder.mall.checkout.gateway;




import com.coder.mall.checkout.dto.PaymentResponse;
import com.coder.mall.checkout.entity.PaymentRecord;

// 合并网关接口
public interface BankGatewayService {
    // 支付操作
    PaymentResponse processPayment(PaymentRecord paymentRecord);

    // 查询操作
    BankQueryResponse queryPayment(String transactionId);
    BankQueryResponse queryRefund(String refundId);
}
