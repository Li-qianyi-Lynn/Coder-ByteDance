package com.coder.mall.auth.exception;



import com.coder.mall.auth.util.BaseResp;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class) // 捕获自定义异常
    public BaseResp<?> handleAuthException(AuthException e) {
        return BaseResp.error(400, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public BaseResp<?> handleException(Exception e) {
        return BaseResp.error(500, "系统内部错误");
    }
}
