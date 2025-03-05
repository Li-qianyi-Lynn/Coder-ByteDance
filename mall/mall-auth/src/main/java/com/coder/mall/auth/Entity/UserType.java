package com.coder.mall.auth.Entity;

public enum UserType {
    CONSUMER("消费者"),
    MERCHANT("商家");

    private final String desc;

    UserType(String desc) {
        this.desc = desc;
    }
}