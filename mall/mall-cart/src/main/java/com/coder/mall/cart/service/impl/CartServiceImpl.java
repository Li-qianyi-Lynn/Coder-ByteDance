package com.coder.mall.cart.service.impl;

import com.coder.mall.cart.controller.CartController;
import com.coder.mall.cart.model.dto.CartProductItem;
import com.coder.mall.cart.model.entity.Cart;
import com.coder.mall.cart.request.AddProductItemReq;
import com.coder.mall.cart.request.EmptyCartReq;
import com.coder.mall.cart.request.GetCartReq;
import com.coder.mall.cart.response.GetCartResp;
import com.coder.mall.cart.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    // 模拟购物车存储
    private List<Cart> carts = new ArrayList<>();

    @Override
    public ResponseEntity<Void> addProductItem(AddProductItemReq request) {
        CartProductItem productItem = request.getProductItem();
        Cart cart = findOrCreateCart(request.getUserId());
        cart.getProductItems().add(productItem);
        // 日志打印移至AOP或日志框架处理
        return ResponseEntity.ok().build();  // 返回 200 OK，无返回内容
    }

    @Override
    public ResponseEntity<GetCartResp> getCart(GetCartReq request) {
        // 获取当前用户的所有购物车数量
        Map<Object, Object> entries = getOpHash();  // 修改为正确的方法
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
        return ResponseEntity.ok().build();  // 返回 200 OK，无返回内容
    }

    // 辅助方法：根据用户 ID 查找或创建购物车
    private Cart findOrCreateCart(int userId) {
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
            map.put(cart.getUserId(), cart);  // 假设 cart.getUserId() 作为 key
        }
        return map;
    }
}
