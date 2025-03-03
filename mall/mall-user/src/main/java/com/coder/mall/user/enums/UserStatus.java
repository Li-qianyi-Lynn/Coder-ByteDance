package com.coder.mall.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserStatus implements BaseEnum {

    ENABLE(1, "正常"),
    DISABLE(0, "禁用");

    @EnumValue
    @JsonValue
    private Integer code;
    private String name;

    UserStatus(Integer code, String name) {
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
