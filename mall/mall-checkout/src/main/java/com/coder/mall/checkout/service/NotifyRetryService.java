package com.coder.mall.checkout.service;


import com.coder.mall.checkout.entity.PaymentRecord;
import com.coder.mall.checkout.gateway.BankGatewayService;
import com.coder.mall.checkout.gateway.BankQueryResponse;
import com.coder.mall.checkout.repository.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyRetryService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final BankGatewayService bankGatewayService;

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 3000))
    public void retryConfirmPayment(String transactionId) {
        PaymentRecord record = paymentRecordRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("支付记录不存在"));

        if (!"PROCESSING".equals(record.getPaymentStatus())) {
            return;
        }

        BankQueryResponse response = bankGatewayService.queryPayment(transactionId);
        if (response.isSuccess()) {
            updatePaymentStatus(record, response);
        }
    }

    private void updatePaymentStatus(PaymentRecord record, BankQueryResponse response) {
        record.setPaymentStatus(response.getStatus());
        paymentRecordRepository.save(record);
        log.info("支付状态更新完成：{}", record.getTransactionId());
    }
}