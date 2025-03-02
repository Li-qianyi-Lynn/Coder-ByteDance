package com.coder.mall.user.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.coder.framework.common.response.Response;

@RestControllerAdvice
public class UserExceptionHandler {
    @ExceptionHandler
    public Response handle(Exception e) {
        e.printStackTrace();
        return Response.fail();
    }

    @ExceptionHandler(UserException.class)
    public Response handle(UserException e) {
        String message = e.getMessage();
        Integer code = e.getCode();
        e.printStackTrace();
        return Response.fail(String.valueOf(code), message);
    }
}
