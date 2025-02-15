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

import java.util.List;
import java.util.Map;

@Service
public class CartRedisServiceImpl implements ICartRedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public static final String CART_KEY = "cart";

    public HashOperations<String, Object, Object> getOpsHash() {
        return redisTemplate.opsForHash();
    }

    @Override
    public void addProductItem(AddProductItemReq request) {
        // 获取购物车数据
        Object userCart = redisTemplate.opsForValue().get(CART_KEY + request.getUserId().toString());
        // 将购物车数据转换为Map类型，Key值是商品的id，value是商品信息，但不包括价格信息
        Map<Long, CartProductItem> redisData = (Map<Long, CartProductItem>) userCart;
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
        redisTemplate.opsForValue().set(CART_KEY + request.getUserId().toString(), redisData);
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
        if (redisData == null) {
            return;
        }
        redisData.remove(deleteItemRequest.getProductId());
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

        // 不管之前购物车中有没有对应的数据，都进行put操作（有就覆盖，没有则添加）
        redisData.put(request.getProductId(), new CartProductItem(request.getProductId(), request.getQuantity()));

        // 添加完之后，重新将map写入redis
        redisTemplate.opsForValue().set(CART_KEY + request.getUserId(), redisData);
    }


//    @Override
//    public Integer addCart(Cart cart) {
//        Cart existingCart = (Cart) getOpsHash().get(cart.getUsersId() + "", cart.getGoodsId() + "");
////        if (existingCart != null) {
//            // 如果商品已存在，更新数量
//            existingCart.setCartNum(cart.getCartNum() + existingCart.getCartNum());
//            getOpsHash().put(cart.getUsersId() + "", cart.getGoodsId() + "", existingCart);
//            return 1;
//        } else {
//            // 如果商品不存在，添加新商品
//            getOpsHash().put(cart.getUsersId() + "", cart.getGoodsId() + "", cart);
//            return 2;
//        }
//    }
//
//    @Override
//    public List<Cart> listCart(Integer uid) {
//        return new ArrayList<>((Collection<? extends Cart>) getOpsHash().values(uid + ""));
//    }
//
//    @Override
//    public Boolean delCart(Cart cart) {
//        Long deleteCount = getOpsHash().delete(cart.getUsersId() + "", cart.getGoodsId() + "");
//        return deleteCount > 0;
//    }


//    @Override
//    public void updateCart(Cart cart, CartUpdateType type) {
//        Cart existingCart = (Cart) getOpsHash().get(cart.getUsersId() + "", cart.getGoodsId() + "");
//        if (existingCart == null) {
//            return;
//        }
//
//        switch (type) {
//            case ADD:
//                existingCart.setCartNum(existingCart.getCartNum() + 1);
//                break;
//            case SUBTRACT:
//                existingCart.setCartNum(existingCart.getCartNum() - 1);
//                break;
//            case UPDATE:
//                existingCart.setCartNum(cart.getCartNum());
//                break;
//        }
//
//        getOpsHash().put(cart.getUsersId() + "", cart.getGoodsId() + "", existingCart);
//    }
//
//    @Override
//    public void delAll(Cart cart) {
//        Map<Object, Object> entries = getOpsHash().entries(cart.getUsersId() + "");
//        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
//            getOpsHash().delete(cart.getUsersId() + "", entry.getKey());
//        }
//    }
}
