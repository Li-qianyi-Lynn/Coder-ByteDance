package com.coder.mall.user.model.vo;

import com.coder.mall.user.custom.converter.StringToUserTypeConverter;
import com.coder.mall.user.enums.UserType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户注册参数")
public class RegisterVo {
    @Schema(description = "手机号")
    private String mobile;

//    @JsonDeserialize(converter = StringToUserTypeConverter.class)
    @Schema(description = "用户类型")
    private UserType userType;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "短信验证码")
    private String code;
}
