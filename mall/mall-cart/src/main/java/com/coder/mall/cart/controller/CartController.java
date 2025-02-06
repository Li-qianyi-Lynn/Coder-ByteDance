package com.coder.mall.cart.controller;

import com.coder.mall.cart.request.AddItemReq;
import com.coder.mall.cart.request.EmptyCartReq;
import com.coder.mall.cart.request.GetCartReq;
import com.coder.mall.cart.response.GetCartResp;
import com.coder.mall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // 添加商品到购物车
    @PostMapping("/add")
    public ResponseEntity<Void> addItem(@RequestBody AddItemReq request) {
        return cartService.addItem(request);
    }

    // 获取购物车内容
    @GetMapping("/{userId}")
    public ResponseEntity<GetCartResp> getCart(@PathVariable int userId) {
        GetCartReq request = new GetCartReq(userId);
        return cartService.getCart(request);
    }

    // 清空购物车
    @DeleteMapping("/empty/{userId}")
    public ResponseEntity<Void> emptyCart(@PathVariable int userId) {
        EmptyCartReq request = new EmptyCartReq(userId);
        return cartService.emptyCart(request);
    }
}






//@RestController
//@RequestMapping("/cart")
//public class CartController {
//
//    // 模拟购物车存储
//    private List<Cart> carts = new ArrayList<>();
//
//    // 添加商品到购物车
//    @PostMapping("/add")
//    public void addItem(@RequestBody AddItemReq request) {
//        CartItem item = request.getItem();
//        Cart cart = findOrCreateCart(request.getUserId());
//        cart.getItems().add(item);
//        System.out.println("Added item to cart: " + item.getProductId());
//    }
//
//    // 获取购物车内容
//    @GetMapping("/{userId}")
//    public GetCartResp getCart(@PathVariable int userId) {
//        Cart cart = findOrCreateCart(userId);
//        return new GetCartResp(cart);
//    }
//
//    // 清空购物车
//    @DeleteMapping("/empty/{userId}")
//    public void emptyCart(@PathVariable int userId) {
//        Cart cart = findOrCreateCart(userId);
//        cart.getItems().clear();
//        System.out.println("Cart has been emptied.");
//    }
//
//    // 辅助方法：根据用户 ID 查找或创建购物车
//    private Cart findOrCreateCart(int userId) {
//        return carts.stream()
//                .filter(cart -> cart.getUserId() == userId)
//                .findFirst()
//                .orElseGet(() -> {
//                    Cart newCart = new Cart(userId, new ArrayList<>());
//                    carts.add(newCart);
//                    return newCart;
//                });
//    }
//}