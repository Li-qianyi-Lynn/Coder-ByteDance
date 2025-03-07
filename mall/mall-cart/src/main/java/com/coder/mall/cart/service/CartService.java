package com.coder.mall.cart.service;

import com.coder.mall.cart.request.AddProductItemReq;
import com.coder.mall.cart.request.EmptyCartReq;
import com.coder.mall.cart.request.GetCartReq;
import com.coder.mall.cart.response.GetCartResp;
import org.springframework.http.ResponseEntity;

/**
 * gRPC 服务接口，定义购物车相关操作
 */
public interface CartService {
    /**
     * 添加商品到购物车
     */
    ResponseEntity<Void> addProductItem(AddProductItemReq request);

    /**
     * 获取购物车内容
     */
    ResponseEntity<GetCartResp> getCart(GetCartReq request);

    /**
     * 清空购物车
     */
    ResponseEntity<Void> emptyCart(EmptyCartReq request);
}