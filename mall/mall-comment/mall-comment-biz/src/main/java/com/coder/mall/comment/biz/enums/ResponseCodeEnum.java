package com.coder.mall.comment.biz.enums;

import com.coder.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("COMMENT-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("COMMENT-10001", "参数错误"),

    // ----------- 业务异常状态码 -----------
    COMMENT_NOT_FOUND("COMMENT-20000", "此评论不存在"),
    COMMENT_CANT_OPERATE("COMMENT-20001", "您无法操作该评论"),

    ;

    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;

}