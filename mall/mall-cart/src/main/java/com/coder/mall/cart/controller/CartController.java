package com.coder.mall.cart.controller;

import com.coder.mall.cart.model.entity.Cart;
import com.coder.mall.cart.request.AddProductItemReq;
import com.coder.mall.cart.request.DeleteItemRequest;
import com.coder.mall.cart.request.UpdateItemRequest;
import com.coder.mall.cart.response.GetCartResp;
import com.coder.mall.cart.service.CartService;
import com.coder.mall.cart.service.ICartRedisService;
import com.coder.mall.cart.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private ICartRedisService cartRedisService;

    /**
     * 添加商品到购物车
     *
     * @param request
     * @return
     */
    @PostMapping("/addProductItemForCart")
    public ResponseEntity<Void> addProductItem(@RequestBody @Validated AddProductItemReq request) {
//        // 调用 cartService 处理添加商品的业务逻辑
//        cartService.addProductItem(request);
        cartRedisService.addProductItem(request);
        logger.info("Added item to cart: " + request.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 获取购物车列表
     *
     * @param userId
     * @return
     */
    @GetMapping("/listOfCart/{userId}")
    public ResponseEntity<GetCartResp> getCart(@PathVariable Long userId) {
        // 原方案：调用 cartService 获取购物车数据
        // return cartService.getCart(new GetCartReq(userId));

        // 从 Redis 获取购物车数据
        Cart cart = cartRedisService.getCart(userId);
        List<Cart> carts = new ArrayList<>();
        carts.add(cart);
        return ResponseEntity.ok(new GetCartResp(carts));
    }

    /**
     * 删除购物车某件商品
     *
     * @param deleteItem
     * @return
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteProductOfCart(@RequestBody DeleteItemRequest deleteItem) {
        cartRedisService.deleteCartItem(deleteItem);
        return ResponseEntity.ok("Cart item deleted successfully.");
    }

//    /**
//     * 删除购物车单/多件商品
//     * @param deleteItems
//     * @return
//     */
//    @DeleteMapping("/deletes")
//    public ResponseEntity delCart(@RequestBody List<DeleteItemRequest> deleteItems) {
//        for (DeleteItemRequest deleteItem : deleteItems) {
//            cartRedisService.deleteCartItem(deleteItem);
//        }
//        return ResponseEntity.ok("Cart item deleted successfully.");
//    }

    /**
     * 清空购物车所有商品
     *
     * @param userId
     * @return
     */
    @DeleteMapping("/empty/{userId}")
    public ResponseEntity<Void> emptyCart(@PathVariable Long userId) {
        // 原方案：调用 cartService 清空购物车
        // return cartService.emptyCart(new EmptyCartReq(userId));

        // 调用 RedisService 来清空购物车
        cartRedisService.clearCart(userId);
        logger.info("Cart emptied for user: " + userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 更新购物车商品数量
     *
     * @param request
     * @return
     */
    @PutMapping("/update")
    public ResponseEntity updateCart(@RequestBody @Validated UpdateItemRequest request) {
        cartRedisService.updateCart(request);
        return ResponseEntity.ok("Cart item updated successfully.");
    }
}