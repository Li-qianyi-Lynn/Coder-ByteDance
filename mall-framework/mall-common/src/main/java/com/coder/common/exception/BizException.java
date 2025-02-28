package com.coder.common.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BizException extends RuntimeException {
    // 异常码
    private String errorCode;
    // 错误信息
    private String errorMessage;

    public BizException(String message) {
        super(message);
        this.errorMessage = message;
    }
    public BizException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public BizException(BaseExceptionInterface baseExceptionInterface) {
        this.errorCode = baseExceptionInterface.getErrorCode();
        this.errorMessage = baseExceptionInterface.getErrorMessage();
    }



}
