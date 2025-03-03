package com.coder.mall.cart.request;

import com.coder.mall.cart.model.dto.CartProductItem;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加商品请求。
 */
@Data
@NoArgsConstructor
public class AddProductItemReq {
    private Long userId;
    private Long productId;
    private int quantity;

    public AddProductItemReq(Long userId, Long productId, int quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        assertValid();
    }

    private void assertValid() {
        // 使用assert进行数据验证
        assert userId != null || userId == 0 : "userId cannot be null or 0";
        assert productId != null || productId == 0 : "productId cannot be null or 0";
        assert quantity == 0 : "product quantity cannot be 0";
    }
}
