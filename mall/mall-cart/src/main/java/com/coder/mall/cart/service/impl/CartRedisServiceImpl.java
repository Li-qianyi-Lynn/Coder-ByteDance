package com.coder.mall.cart.service.impl;

import com.coder.mall.cart.model.dto.CartProductItem;
import com.coder.mall.cart.model.entity.Cart;
import com.coder.mall.cart.request.AddProductItemReq;
import com.coder.mall.cart.request.DeleteItemRequest;
import com.coder.mall.cart.request.UpdateItemRequest;
import com.coder.mall.cart.service.ICartRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.coder.mall.cart.constant.CartConstants.CART_KEY;

@Service
public class CartRedisServiceImpl implements ICartRedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public HashOperations<String, Object, Object> getOpsHash() {
        return redisTemplate.opsForHash();
    }

    @Override
    public void addProductItem(AddProductItemReq request) {
        // 获取购物车数据
        Object userCart = redisTemplate.opsForValue().get(CART_KEY + request.getUserId().toString());
        // 将购物车数据转换为Map类型，Key值是商品的id，value是商品信息，但不包括价格信息
        Map<Long, CartProductItem> redisData = (Map<Long, CartProductItem>) userCart;

        if (redisData == null) {
            redisData = new HashMap<>();
        }
        // 获取对应Item
        CartProductItem cartProductItem = redisData.get(request.getProductId());
        if (cartProductItem == null) {
            // 如果没有item，那么直接put
            redisData.put(request.getProductId(), new CartProductItem(request.getProductId(), request.getQuantity()));
        } else {
            // 如果有item，那么对数量进行更新
            cartProductItem.setQuantity(cartProductItem.getQuantity() + request.getQuantity());
            redisData.put(request.getProductId(), cartProductItem);
        }

        // 将购物车数据存入Redis
        redisTemplate.opsForValue().set(CART_KEY + request.getUserId().toString(), redisData);

        // 设置过期时间，例如：过期时间为1小时（3600秒）
        redisTemplate.expire(CART_KEY + request.getUserId().toString(), 1, TimeUnit.HOURS);
    }

    @Override
    public Cart getCart(Long userId) {
        // 获取购物车数据
        Object userCart = redisTemplate.opsForValue().get(CART_KEY + userId.toString());
        // 将购物车数据转换为Map类型，Key值是商品的id，value是商品信息，但不包括价格信息
        Map<Long, CartProductItem> redisData = (Map<Long, CartProductItem>) userCart;

        if (redisData != null) {
            List<CartProductItem> values = redisData.values().stream().toList();
            // todo: 根据product表获取商品价格
            Cart cart = new Cart(userId, values);
            return cart;
        }
        return new Cart(userId);
    }

    @Override
    public void deleteCartItem(DeleteItemRequest deleteItemRequest) {
        // 获取购物车数据
        Object userCart = redisTemplate.opsForValue().get(CART_KEY + deleteItemRequest.getUserId());
        // 将购物车数据转换为Map类型，Key值是商品的id，value是商品信息，但不包括价格信息
        Map<Long, CartProductItem> redisData = (Map<Long, CartProductItem>) userCart;

        if (redisData != null) {
            redisData.remove(deleteItemRequest.getProductId());
            redisTemplate.opsForValue().set(CART_KEY + deleteItemRequest.getUserId(), redisData);
        }
    }

    @Override
    public void clearCart(Long userId) {
        // 获取购物车数据
        redisTemplate.delete(CART_KEY + userId);
    }

    @Override
    public void updateCart(UpdateItemRequest request) {
        // 获取购物车数据
        Object userCart = redisTemplate.opsForValue().get(CART_KEY + request.getUserId());
        // 将购物车数据转换为Map类型，Key值是商品的id，value是商品信息，但不包括价格信息
        Map<Long, CartProductItem> redisData = (Map<Long, CartProductItem>) userCart;

        if (redisData != null) {
            redisData.put(request.getProductId(), new CartProductItem(request.getProductId(), request.getQuantity()));
            redisTemplate.opsForValue().set(CART_KEY + request.getUserId(), redisData);

            // 更新缓存过期时间（如1小时）
            redisTemplate.expire(CART_KEY + request.getUserId(), 1, TimeUnit.HOURS);
        }
    }
}
