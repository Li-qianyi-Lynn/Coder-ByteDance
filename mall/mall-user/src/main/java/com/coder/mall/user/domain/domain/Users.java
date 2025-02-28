package com.coder.mall.user.domain.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.coder.mall.user.enums.Gender;
import com.coder.mall.user.enums.UserStatus;
import com.coder.mall.user.enums.UserType;
import com.coder.mall.user.enums.Verified;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * @TableName users
 */
@TableName(value = "users")
@Schema(name = "用户信息表")
@Data
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(name = "用户ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "手机号码")
    private String mobile;

    @Schema(description = "密码")
    @TableField(value = "password", select = false)
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像url")
    private String profileUrl;

    @Schema(description = "用户类型")
    @TableField(value = "type")
    private UserType userType;

    @Schema(name = "邮箱")
    @Email
    private String email;

    @Schema(description = "性别")
    private Gender gender;

    @Schema(description = "生日")
    private Date birthdate;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonIgnore
    private Date createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonIgnore
    private Date updateTime;

    @Schema(description = "用户状态")
    @TableField(value = "is_active")
    private UserStatus userStatus;

    @Schema(description = "是否验证")
    @TableField(value = "is_verified")
    private Verified isVerified;

    @TableField("is_deleted")
    @Schema(name = "逻辑删除")
    @JsonIgnore
    @TableLogic
    private Integer isDeleted;


}