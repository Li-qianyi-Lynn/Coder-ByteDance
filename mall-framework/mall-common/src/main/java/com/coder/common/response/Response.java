package com.coder.common.response;

import com.coder.common.exception.BaseExceptionInterface;
import com.coder.common.exception.BizException;
import com.coder.common.util.DateUtils;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Response<T> implements Serializable {

    // 是否成功，默认为 true
    private boolean success = true;
    // 响应消息
    private String message;
    // 异常码
    private String errorCode;
    // 响应数据
    private T data;
    // 时间戳
    private long timestamp;

    // =================================== 成功响应 ===================================
    public static <T> Response<T> success() {
        Response<T> response = new Response<>();
        LocalDateTime now = LocalDateTime.now();
        long nowTime = DateUtils.localDateTime2Timestamp(now);
        response.setTimestamp(nowTime);
        return response;
    }

    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        LocalDateTime now = LocalDateTime.now();
        long nowTime = DateUtils.localDateTime2Timestamp(now);
        response.setTimestamp(nowTime);
        response.setData(data);
        return response;
    }

    // =================================== 失败响应 ===================================
    public static <T> Response<T> fail() {
        Response<T> response = new Response<>();
        LocalDateTime now = LocalDateTime.now();
        long nowTime = DateUtils.localDateTime2Timestamp(now);
        response.setTimestamp(nowTime);
        response.setSuccess(false);
        return response;
    }

    public static <T> Response<T> fail(String errorMessage) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        LocalDateTime now = LocalDateTime.now();
        long nowTime = DateUtils.localDateTime2Timestamp(now);
        response.setTimestamp(nowTime);
        response.setMessage(errorMessage);
        return response;
    }

    public static <T> Response<T> fail(String errorCode, String errorMessage) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        LocalDateTime now = LocalDateTime.now();
        long nowTime = DateUtils.localDateTime2Timestamp(now);
        response.setTimestamp(nowTime);
        response.setErrorCode(errorCode);
        response.setMessage(errorMessage);
        return response;
    }

    public static <T> Response<T> fail(BizException bizException) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        LocalDateTime now = LocalDateTime.now();
        long nowTime = DateUtils.localDateTime2Timestamp(now);
        response.setTimestamp(nowTime);
        response.setErrorCode(bizException.getErrorCode());
        response.setMessage(bizException.getErrorMessage());
        return response;
    }

    public static <T> Response<T> fail(BaseExceptionInterface baseExceptionInterface) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        LocalDateTime now = LocalDateTime.now();
        long nowTime = DateUtils.localDateTime2Timestamp(now);
        response.setTimestamp(nowTime);
        response.setErrorCode(baseExceptionInterface.getErrorCode());
        response.setMessage(baseExceptionInterface.getErrorMessage());
        return response;
    }

}