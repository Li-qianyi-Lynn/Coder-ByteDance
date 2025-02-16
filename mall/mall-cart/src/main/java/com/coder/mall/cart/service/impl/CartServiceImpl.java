package com.coder.mall.cart.service.impl;

import com.coder.mall.cart.model.dto.CartProductItem;
import com.coder.mall.cart.model.entity.Cart;
import com.coder.mall.cart.request.AddProductItemReq;
import com.coder.mall.cart.request.EmptyCartReq;
import com.coder.mall.cart.request.GetCartReq;
import com.coder.mall.cart.response.GetCartResp;
import com.coder.mall.cart.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.coder.mall.cart.constant.CartConstants.CART_KEY;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    private final RedisTemplate redisTemplate;

    // 模拟购物车存储
    private List<Cart> carts = new ArrayList<>();

    public CartServiceImpl(@Qualifier("redisTemplate") RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ResponseEntity<Void> addProductItem(AddProductItemReq request) {
//        CartProductItem productItem = request.getProductItem();
//        Cart cart = findOrCreateCart(request.getUserId());
//        cart.getProductItems().add(productItem);
//        // 日志打印移至AOP或日志框架处理
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<GetCartResp> getCart(GetCartReq request) {
        // 获取当前用户的所有购物车数量
        Map<Object, Object> entries = getOpHash();
        Set<Map.Entry<Object, Object>> entrySet = entries.entrySet();
        // 获取内部的所有值
        List<Cart> carts = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entrySet) {
            Cart cart = (Cart) entry.getValue();
            carts.add(cart);
        }
        return ResponseEntity.ok(new GetCartResp(carts));
    }

    @Override
    public ResponseEntity<Void> emptyCart(EmptyCartReq request) {
        Cart cart = findOrCreateCart(request.getUserId());
        cart.getProductItems().clear();
        System.out.println("Cart has been emptied.");
        return ResponseEntity.ok().build();
    }

    // 辅助方法：根据用户 ID 查找或创建购物车
    private Cart findOrCreateCart(Long userId) {
        Optional<Cart> cartOpt = carts.stream()
                .filter(cart -> cart.getUserId() == userId)
                .findFirst();

        return cartOpt.orElseGet(() -> {
            Cart newCart = new Cart(userId, new ArrayList<>());
            carts.add(newCart);
            return newCart;
        });
    }

    // 模拟获取数据的方法，模拟存储获取
    private Map<Object, Object> getOpHash() {
        Map<Object, Object> map = new HashMap<>();
        for (Cart cart : carts) {
            // cart.getUserId() 作为 key
            map.put(cart.getUserId(), cart);
        }
        return map;
    }

    private Map<Long, CartProductItem> getCartData(Long userId) {
        Object userCart = redisTemplate.opsForValue().get(CART_KEY + userId.toString());
        return userCart == null ? new HashMap<>() : (Map<Long, CartProductItem>) userCart;
    }

}
