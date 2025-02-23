package com.coder.mall.cart.service;

import com.coder.mall.cart.model.entity.Cart;
import com.coder.mall.cart.request.AddProductItemReq;
import com.coder.mall.cart.request.DeleteItemRequest;
import com.coder.mall.cart.request.UpdateItemRequest;

public interface ICartRedisService {

    /**
     * 添加购物车商品
     *
     * @param request
     */
    void addProductItem(AddProductItemReq request);

    /**
     * 查看购物车商品
     *
     * @param userId
     * @return
     */
    Cart getCart(Long userId);

    /**
     * 删除购物车单个商品
     *
     * @param deleteItem
     */
    void deleteCartItem(DeleteItemRequest deleteItem);

//    /**
//     * 删除购物车单/多个商品
//     * @param deleteItems
//     */
//    void deleteCartItems(List<DeleteItemRequest> deleteItems);

    /**
     * 删除购物车所有商品
     *
     * @param userId
     */
    void clearCart(Long userId);

    /**
     * 更新购物车某件商品数量
     *
     * @param cartProductItem
     */
    void updateCart(UpdateItemRequest cartProductItem);

    /**
     * 更新整个购物车
     */
    void updateCart(Long userId, Cart cart);


    /**
     * 保存购物车数据
     * @param userId
     */
    void saveCart(Long userId);
}
