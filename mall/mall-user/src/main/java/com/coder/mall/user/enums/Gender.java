package com.coder.mall.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender implements BaseEnum {

    MALE(1, "男"),
    FEMALE(0, "女");

    @EnumValue
    @JsonValue
    private Integer code;
    private String name;

    Gender(Integer code, String name) {
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
