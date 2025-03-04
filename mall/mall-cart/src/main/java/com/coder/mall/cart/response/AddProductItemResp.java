package com.coder.mall.cart.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 添加商品响应（无具体数据返回）。
 */
@Getter
@Setter
@AllArgsConstructor
public class AddProductItemResp {
    private Integer code;
    private String message;
}


