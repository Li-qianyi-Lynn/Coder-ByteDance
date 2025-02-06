package com.coder.mall.order.constant;

public enum OrderStatus {

    // 订单创建，但未确认支付信息
    CREATED("CREATED", "订单已创建"),

    // 订单已确认，等待支付
    PENDING_PAYMENT("PENDING_PAYMENT", "待支付"),

    // 已支付，等待商家发货
    PAID("PAID", "已支付"),

    // 商家已发货，等待用户收货
    SHIPPED("SHIPPED", "已发货"),

    // 用户已确认收货，订单完成
    COMPLETED("COMPLETED", "已完成"),

    // 订单已取消（可能是超时未支付、用户主动取消等）
    CANCELLED("CANCELLED", "已取消"),

    // 订单已退款
    REFUNDED("REFUNDED", "已退款"),

    // 订单异常或失败
    FAILED("FAILED", "订单失败");

    private final String code;
    private final String description;

    OrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean canCancel() {
        return true;
    }
}
