package com.coder.mall.checkout.service;




import com.coder.common.exception.BizException;
import com.coder.mall.checkout.dto.BankNotifyRequest;
import com.coder.mall.checkout.entity.PaymentRecord;
import com.coder.mall.checkout.entity.CustomerOrder;
import com.coder.mall.checkout.repository.CustomerOrderRepository;
import com.coder.mall.checkout.repository.PaymentRecordRepository;
import com.coder.mall.checkout.security.BankSignatureUtil;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncNotifyService {

    private final CustomerOrderRepository orderRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    @Value("${bank.notify.secret}")
    private String bankSecretKey;

    @Transactional
    public void processBankNotify(BankNotifyRequest request) {
        log.info("签名原始数据：{}|{}|{}|{}|{}|{}|{}",
                request.getVersion(),
                request.getMerchantId(),
                request.getTransactionId(),
                request.getOrderNo(),
                request.getAmount(),
                request.getStatus(),
                request.getSignType());

        log.info("开始处理银行通知，订单号：{}", request.getOrderNo()); // 新增日志
        // 1. 基础校验
        validateRequest(request);

        // 2. 签名验证
        String signData = buildSignData(request);
        if (!BankSignatureUtil.verifySignature(signData, request.getSign(), bankSecretKey)) {
            log.error("银行通知签名验证失败：{}", request);
            throw new BizException("非法请求");
        }

        // 3. 查询关联订单
        CustomerOrder order = orderRepository.findByOrderNo(request.getOrderNo())
                .orElseThrow(() -> new BizException("订单不存在"));

        // 4. 幂等性检查
        if (paymentRecordRepository.existsByTransactionId(request.getTransactionId())) {
            log.info("重复通知已处理：{}", request.getTransactionId());
            return;
        }

        // 5. 金额一致性校验
        if (order.getTotalCost().compareTo(request.getAmount()) != 0) {
            log.error("金额不一致：订单金额={}，通知金额={}",
                    order.getTotalCost(), request.getAmount());
            throw new BizException("金额不一致");
        }

        // 6. 更新支付记录
        PaymentRecord record = createPaymentRecord(request, order);
        paymentRecordRepository.save(record);

        // 7. 更新订单状态
        updateOrderStatus(order, request);
    }

    // 新增退款处理方法
    @Transactional
    public void processRefundNotify(BankNotifyRequest request) {
        // 1. 基础校验（与支付共用）
        validateRequest(request);

        // 2. 签名验证（需要单独构建退款签名数据）
        String refundSignData = buildRefundSignData(request);
        if (!BankSignatureUtil.verifySignature(refundSignData, request.getSign(), bankSecretKey)) {
            log.error("退款通知签名验证失败：{}", request);
            throw new BizException("非法退款请求");
        }

        // 3. 查询关联订单（需要校验订单是否可退款）
        CustomerOrder order = orderRepository.findByOrderNo(request.getOrderNo())
                .orElseThrow(() -> new BizException("订单不存在"));

        // 4. 退款状态校验（确保订单处于可退款状态）
        if (!order.getStatus().equals("PAID")) {
            log.error("订单当前状态不可退款：{}", order.getStatus());
            throw new BizException("订单不满足退款条件");
        }

        // 5. 幂等性检查（复用支付记录表）
        if (paymentRecordRepository.existsByTransactionId(request.getTransactionId())) {
            log.info("重复退款通知已处理：{}", request.getTransactionId());
            return;
        }

        // 6. 创建退款记录（金额取负数）
        PaymentRecord refundRecord = createRefundRecord(request, order);
        paymentRecordRepository.save(refundRecord);

        // 7. 更新订单状态
        updateOrderRefundStatus(order, request);
    }

    // 构建退款签名数据（需包含退款专用字段）
    private String buildRefundSignData(BankNotifyRequest request) {
        return String.join("|",
                request.getTransactionId(),
                request.getOrderNo(),
                request.getAmount().toPlainString(),
                "REFUND", // 明确区分支付/退款类型
                request.getStatus()
        );
    }

    // 创建退款记录（与支付记录结构一致，金额为负）
    private PaymentRecord createRefundRecord(BankNotifyRequest request, CustomerOrder order) {
        PaymentRecord record = new PaymentRecord();
        record.setTransactionId(request.getTransactionId());
        record.setOrderNo(order.getOrderNo());
        record.setAmount(request.getAmount().negate()); // 负数表示退款
        record.setPaymentStatus(convertRefundStatus(request.getStatus()));
        record.setPaymentGateway("BANK_CARD");
        record.setCreatedTime(LocalDateTime.now());
        return record;
    }

    // 退款状态转换逻辑
    private String convertRefundStatus(String bankStatus) {
        return switch (bankStatus) {
            case "REFUND_SUCCESS" -> "REFUNDED";
            case "REFUND_FAIL" -> "REFUND_FAILED";
            default -> throw new BizException("未知退款状态码");
        };
    }

    // 更新订单退款状态
    private void updateOrderRefundStatus(CustomerOrder order, BankNotifyRequest request) {
        if ("REFUND_SUCCESS".equals(request.getStatus())) {
            order.setStatus("REFUNDED");
            orderRepository.save(order);
            log.info("订单状态更新为已退款：{}", order.getOrderNo());
        } else {
            log.warn("退款处理失败：{}", order.getOrderNo());
        }
    }

    // 构建退款签名数据（需包含退款专用字段）





    private void validateRequest(BankNotifyRequest request) {
        if (request.getTransactionId() == null || request.getOrderNo() == null) {
            throw new BizException("缺少必要参数");
        }
    }

    private String buildSignData(BankNotifyRequest request) {
        return String.join("|",
                request.getTransactionId(),
                request.getOrderNo(),
                request.getAmount().toPlainString(),
                request.getStatus()
        );
    }

    private PaymentRecord createPaymentRecord(BankNotifyRequest request, CustomerOrder order) {
        PaymentRecord record = new PaymentRecord();
        record.setTransactionId(request.getTransactionId());
        record.setOrderNo(order.getOrderNo());
        record.setAmount(request.getAmount());
        record.setPaymentStatus(convertStatus(request.getStatus()));
        record.setPaymentGateway("BANK_CARD");
        record.setCreatedTime(LocalDateTime.now());
        return record;
    }

    private void updateOrderStatus(CustomerOrder order, BankNotifyRequest request) {
        if ("SUCCESS".equals(request.getStatus())) {
            if (!"PAID".equals(order.getStatus())) {
                order.setStatus("PAID");
                orderRepository.save(order);
                log.info("订单状态更新为已支付：{}", order.getOrderNo());
            }
        } else {
            order.setStatus("PAY_FAILED");
            orderRepository.save(order);
            log.warn("订单支付失败：{}", order.getOrderNo());
        }
    }

    private String convertStatus(String bankStatus) {
        return switch (bankStatus) {
            case "SUCCESS" -> "PAID";
            case "FAIL" -> "FAILED";
            case "PROCESSING" -> "PROCESSING";
            default -> throw new BizException("未知状态码");
        };
    }

}