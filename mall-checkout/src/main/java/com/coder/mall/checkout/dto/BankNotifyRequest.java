package com.coder.mall.checkout.dto;



import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BankNotifyRequest {

    // BankNotifyRequest
    private LocalDateTime notifyTime;
    private LocalDateTime tradeTime;

    // 添加JSON反序列化支持
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getNotifyTime() {
        return notifyTime;
    }



    // 基础字段
    private String version;       // 接口版本
    private String merchantId;    // 商户号
    private String transactionId; // 银行交易号
    private String orderNo ;       // 商户订单号
    private BigDecimal amount;    // 交易金额
    private String currency;      // 货币类型
    private String status;        // 交易状态



    // 安全字段
    private String signType;      // 签名类型
    private String sign;          // 签名

    // 错误信息
    private String errorCode;
    private String errorMsg;

    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }
}