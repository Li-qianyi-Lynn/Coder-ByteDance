package com.coder.mall.cart.response;

public class ApiResponse {
    private int code;
    private String message;

    // 构造方法
    public ApiResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // Getter 和 Setter
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

