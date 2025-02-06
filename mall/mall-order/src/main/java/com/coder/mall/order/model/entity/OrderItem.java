package com.coder.mall.order.model.entity;

import com.coder.mall.order.model.dto.CartItem;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 订单项实体类
 */
@Data
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 购物车商品信息
     */
    private CartItem cartItem;

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 商品单价
     */
    private BigDecimal unitPrice;

    /**
     * 商品总价（单价 * 数量）
     */
    private BigDecimal cost;

    /**
     * 商品图片URL
     */
    private String pictureUrl;

    /**
     * 商品规格信息（JSON格式）
     */
    private String specifications;

    /**
     * 商家ID
     */
    private String dealerId;

    /**
     * 是否已评价
     */
    private Boolean reviewed;

    /**
     * 商品折扣信息
     */
    private BigDecimal discount;

    /**
     * 实际支付金额（考虑折扣后）
     */
    private BigDecimal actualCost;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 计算实际支付金额
     */
    public void calculateActualCost() {
        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            this.actualCost = this.cost.multiply(BigDecimal.ONE.subtract(discount))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
            this.actualCost = this.cost;
        }
    }

    /**
     * 从购物车项初始化订单项
     */
    public void initFromCartItem(CartItem cartItem) {
        this.cartItem = cartItem;
        this.productId = cartItem.getProductId();
        this.quantity = cartItem.getQuantity();
        this.unitPrice = cartItem.getUnitPrice();
        this.cost = unitPrice.multiply(new BigDecimal(quantity))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        this.reviewed = false;
        this.calculateActualCost();
    }
}