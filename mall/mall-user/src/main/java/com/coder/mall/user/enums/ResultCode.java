package com.coder.mall.user.enums;

import lombok.Getter;

@Getter
public enum ResultCode {

    PHONE_EMPTY(502, "手机号码为空"),
    CODE_EMPTY(503, "验证码为空"),
    SEND_SMS_TOO_OFTEN(504, "验证法发送过于频繁"),
    CODE_EXPIRED(505, "验证码已过期"),
    CODE_ERROR(506, "验证码错误"),
    ACCOUNT_DISABLED_ERROR(507, "该用户已被禁用"),
    ACCOUNT_NOT_EXIST_ERROR(508, "该用户不存在"),
    ACCOUNT_NOT_LOGIN_ERROR(509, "用户未登录"),
    ACCOUNT_EXIST_ERROR(510, "该用户已存在"),

    TOKEN_EXPIRED(610, "token已过期"),
    TOKEN_INVALID(602, "token非法");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
