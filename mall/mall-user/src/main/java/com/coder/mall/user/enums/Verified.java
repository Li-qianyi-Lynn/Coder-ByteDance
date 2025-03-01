package com.coder.mall.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Verified implements BaseEnum {
    VERIFIED(1, "已验证"),
    UNVERIFIED(0, "未验证");

    @EnumValue
    @JsonValue
    private Integer code;
    private String name;

    Verified(Integer code, String name) {
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
