package com.coder.mall.checkout.constant;

import lombok.Getter;

public interface BaseExceptionInterface {

    // 获取异常码
    String getErrorCode();

    // 获取异常信息
    String getErrorMessage();
}