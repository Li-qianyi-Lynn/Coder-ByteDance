package com.coder.mall.auth.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResp<T> {
    private Integer status;
    private String message;
    private T data;

    public static <T> BaseResp<T> success(T data) {
        return new BaseResp<>(200, "success", data);
    }

    public static <T> BaseResp<T> error(Integer status, String message) {
        return new BaseResp<>(status, message, null);
    }
}