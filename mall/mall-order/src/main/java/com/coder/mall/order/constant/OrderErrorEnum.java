package com.coder.mall.order.constant;


import com.coder.framework.common.exception.BaseExceptionInterface;

import lombok.Getter;

@Getter
public enum OrderErrorEnum implements BaseExceptionInterface{

    ORDER_NOT_FOUND("ORDER001", "订单不存在"),
    ORDER_STATUS_INVALID("ORDER002", "订单状态不正确"),
    ORDER_CREATE_FAILED("ORDER003", "订单创建失败"),
    ORDER_UPDATE_FAILED("ORDER004", "订单更新失败"),
    ORDER_CANCEL_FAILED("ORDER005", "订单取消失败"),
    ORDER_NOT_BELONGS_TO_USER("ORDER006", "订单不属于当前用户"),
    ORDER_STATUS_ERROR("ORDER007", "订单状态错误"),
    PAYMENT_FAILED("ORDER008", "支付失败"),
    ORDER_ITEMS_EMPTY("ORDER009", "订单项不能为空"), 
    ORDER_ITEMS_INVALID("ORDER010", "订单项不合法"), 
    PARAM_ERROR("ORDER011", "UserID错误"), 
    SYSTEM_ERROR("ORDER012", "系统错误"), 
    CART_EMPTY("ORDER013", "购物车为空"),
    ORDER_QUERY_FAILED("ORDER014", "订单查询失败"), 
    CART_ITEM_DELETE_FAILED("ORDER015", "购物车商品删除失败"),
    ORDER_GET_FAILED("ORDER016", "获取订单失败"),
    ORDER_LIST_FAILED("ORDER017", "获取订单列表失败"), 
    PRODUCT_SERVICE_ERROR("ORDER018", "商品服务错误"), 
    ORDER_PARSE_ERROR("ORDER019", "订单解析错误"),
    CART_UPDATE_FAILED("ORDER020", "从购物车删除商品失败"),
    ORDER_MESSAGE_SEND_FAILED("ORDER021", "发送订单消息失败"),
    PAYMENT_URL_GENERATION_FAILED("ORDER022", "生成支付链接失败"),
    ORDER_PLACE_FAILED("ORDER023", "订单创建失败");


    private final String errorCode;
    private final String errorMessage;

    OrderErrorEnum(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }



}
