package com.coder.mall.cart.service;

import com.coder.mall.cart.request.AddItemReq;
import com.coder.mall.cart.request.EmptyCartReq;
import com.coder.mall.cart.request.GetCartReq;
import com.coder.mall.cart.response.GetCartResp;
import org.springframework.http.ResponseEntity;

/**
 * gRPC 服务接口，定义购物车相关操作。
 */
public interface CartService {
    /**
     * 添加商品到购物车。
     */
    ResponseEntity<Void> addItem(AddItemReq request);

    /**
     * 获取购物车内容。
     */
    ResponseEntity<GetCartResp> getCart(GetCartReq request);

    /**
     * 清空购物车。
     */
    ResponseEntity<Void> emptyCart(EmptyCartReq request);
}


//public interface CartService {
//    /**
//     * 添加商品到购物车。
//     */
//    void addItem(AddItemReq request, StreamObserver<AddItemResp> responseObserver);
//
//    /**
//     * 获取购物车内容。
//     */
//    void getCart(GetCartReq request, StreamObserver<GetCartResp> responseObserver);
//
//    /**
//     * 清空购物车。
//     */
//    void emptyCart(EmptyCartReq request, StreamObserver<EmptyCartResp> responseObserver);
//}