package com.coder.mall.order.constant;


import com.coder.common.exception.BaseExceptionInterface;

import lombok.Getter;

@Getter
public enum OrderErrorEnum implements BaseExceptionInterface{

    ORDER_NOT_FOUND("ORDER001", "订单不存在"),
    ORDER_STATUS_INVALID("ORDER002", "订单状态不正确"),
    ORDER_CREATE_FAILED("ORDER003", "订单创建失败"),
    ORDER_UPDATE_FAILED("ORDER004", "订单更新失败"),
    ORDER_CANCEL_FAILED("ORDER005", "订单取消失败"),
    ORDER_NOT_BELONGS_TO_USER("ORDER006", "订单不属于当前用户"),
    CART_EMPTY("ORDER007", "购物车为空"),
    PAYMENT_FAILED("ORDER008", "支付失败");

    private final String errorCode;
    private final String errorMessage;

    OrderErrorEnum(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }



}
