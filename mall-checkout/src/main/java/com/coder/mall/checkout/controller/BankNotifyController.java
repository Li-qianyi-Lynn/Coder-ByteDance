package com.coder.mall.checkout.controller;

import com.coder.mall.checkout.dto.BankNotifyRequest;
import com.coder.mall.checkout.service.AsyncNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/bank/notify")
@RequiredArgsConstructor
public class BankNotifyController {

    private final AsyncNotifyService asyncNotifyService;

    @PostMapping("/payment")
    public String handlePaymentNotify(@RequestBody BankNotifyRequest request) {
        try {
            log.info("收到银行支付通知：{}", request);
            asyncNotifyService.processBankNotify(request);
            return "SUCCESS";
        } catch (Exception e) {
            log.error("处理支付通知异常：", e);
            return "FAIL|" + e.getMessage();
        }
    }

    @PostMapping("/refund")
    public String handleRefundNotify(@RequestBody BankNotifyRequest request) {
        try {
            log.info("收到银行退款通知：{}", request);
            asyncNotifyService.processRefundNotify(request); // 新增退款处理方法
            return "SUCCESS";
        } catch (Exception e) {
            log.error("处理退款通知异常：", e);
            return "FAIL|" + e.getMessage();
        }
    }
}