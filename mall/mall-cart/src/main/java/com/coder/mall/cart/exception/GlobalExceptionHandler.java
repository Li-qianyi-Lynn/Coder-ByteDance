package com.coder.mall.cart.exception;

import com.coder.mall.cart.model.dto.CartResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 处理自定义异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<CartResponse> handleException(Exception ex) {
        // 可以根据需要返回更详细的错误信息
        return new ResponseEntity<>(new CartResponse(false, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
