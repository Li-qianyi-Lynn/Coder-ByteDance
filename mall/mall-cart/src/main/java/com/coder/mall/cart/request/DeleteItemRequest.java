package com.coder.mall.cart.request;

import lombok.Data;

@Data
public class DeleteItemRequest {
    private Long userId;
    private Long productId;

    public DeleteItemRequest() {
        // 无参构造方法，Spring 可以通过此方法创建对象
    }

    public DeleteItemRequest(Long userId, Long productId){
        this.productId = productId;
        this.userId = userId;

        assertValid();
    }
    private void assertValid() {
        // 使用assert进行数据验证
        assert userId != null || userId == 0 : "userId cannot be null or 0";
        assert productId != null || productId == 0 : "productId cannot be null or 0";
    }

}
