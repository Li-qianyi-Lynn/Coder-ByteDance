package com.coder.mall.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户登录参数")
public class LoginVo {
    @Schema(description = "手机号")
    private String mobile;
    @Schema(description = "短信验证码")
    private String code;
}
