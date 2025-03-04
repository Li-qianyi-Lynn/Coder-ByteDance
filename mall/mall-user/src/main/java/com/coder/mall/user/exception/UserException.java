package com.coder.mall.user.exception;

import com.coder.mall.user.enums.ResultCode;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Data;

@Data
public class UserException extends RuntimeException {

    private Integer code;

    public UserException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
}
