package com.coder.mall.cart.service.impl;

import com.coder.mall.cart.model.dto.CartItem;
import com.coder.mall.cart.model.entity.Cart;
import com.coder.mall.cart.request.AddItemReq;
import com.coder.mall.cart.request.EmptyCartReq;
import com.coder.mall.cart.request.GetCartReq;
import com.coder.mall.cart.response.GetCartResp;
import com.coder.mall.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    // 模拟购物车存储
    private List<Cart> carts = new ArrayList<>();

    @Override
    public ResponseEntity<Void> addItem(AddItemReq request) {
        CartItem item = request.getItem();
        Cart cart = findOrCreateCart(request.getUserId());
        cart.getItems().add(item);
        System.out.println("Added item to cart: " + item.getProductId());
        return ResponseEntity.ok().build();  // 返回 200 OK，无返回内容
    }

    @Override
    public ResponseEntity<GetCartResp> getCart(GetCartReq request) {
        // 获取当前用户的所有购物车数量
        Map<object, objext> entries = getOpHash().entries(uid + "");
        Set<Map.Entry<object, object>> entries = entries.entrySet();
        // 获取内部的所有值
        List<Cart> carts = new ArrayList<>();
        for (Map.Entry<object, object> objectobjectEntry : entries) {
            Cart cart = (Cart) objectobjectEntry.getValue();
            carts.add(cart);
        }
        return carts;

//        Cart cart = findOrCreateCart(request.getUserId());
//        return ResponseEntity.ok(new GetCartResp(cart));  // 返回 200 OK 和购物车内容
    }

    @Override
    public ResponseEntity<Void> emptyCart(EmptyCartReq request) {
        Cart cart = findOrCreateCart(request.getUserId());
        cart.getItems().clear();
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
}
