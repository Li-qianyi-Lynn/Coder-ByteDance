package com.coder.mall.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserType implements BaseEnum {

    BUYER(1, "买家"),
    SELLER(2, "卖家");

    @EnumValue
    @JsonValue
    private Integer code;
    private String name;

    UserType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
