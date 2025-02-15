package com.coder.mall.cart.request;

import lombok.Data;

@Data
public class UpdateItemRequest {
    private Long userId;
    private Long productId;
    private int quantity;

    public UpdateItemRequest(Long userId, Long productId, int quantity){
        this.productId = productId;
        this.userId = userId;
        this.quantity = quantity;

        assertValid();
    }
    private void assertValid() {
        // 使用assert进行数据验证
        assert userId != null || userId == 0 : "userId cannot be null or 0";
        assert productId != null || productId == 0 : "productId cannot be null or 0";
        assert quantity == 0 : "quantity cannot be 0";
    }

}
