package com.coder.mall.cart.controller;

import com.coder.framework.biz.context.holder.LoginUserContextHolder;
import com.coder.mall.cart.model.dto.CartProductItem;
import com.coder.mall.cart.model.dto.CartResponse;
import com.coder.mall.cart.model.dto.ProductDTO;
import com.coder.mall.cart.model.entity.Cart;
import com.coder.mall.cart.request.AddProductItemReq;
import com.coder.mall.cart.request.DeleteItemRequest;
import com.coder.mall.cart.request.UpdateItemRequest;
import com.coder.mall.cart.response.AddProductItemResp;
import com.coder.mall.cart.response.ApiResponse;
import com.coder.mall.cart.response.GetCartResp;
import com.coder.mall.cart.service.CartService;
import com.coder.mall.cart.service.ICartRedisService;
import com.coder.mall.cart.service.ProductService;
import com.coder.mall.cart.service.RedisService;
import lombok.NonNull;
import org.apache.catalina.connector.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static javax.security.auth.callback.ConfirmationCallback.OK;

@RestController
@RequestMapping("/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private ICartRedisService cartRedisService;

    @Autowired
    private ProductService productService;


    /**
     * 添加商品到购物车
     *
     * @param request
     * @return
     */
    @PostMapping("/addProductItemForCart")
    public ResponseEntity<AddProductItemResp> addProductItem(@RequestBody @Validated AddProductItemReq request) {
        Long userId = LoginUserContextHolder.getUserId();
        if (!Objects.equals(userId, request.getUserId())){
            // 如果 userId 不匹配，则设置 request 中的 userId 为当前用户 ID
            request.setUserId(userId);
        }
        cartRedisService.addProductItem(request);
        logger.info("Added item to cart: " + request.getProductId());
        return ResponseEntity.ok(new AddProductItemResp(HttpStatus.OK.value(), "Item added to cart successfully."));
    }

    /**
     * 获取购物车列表
     *
     * @param userId
     * @return
     */
    @GetMapping("/listOfCart/{userId}")
    public ResponseEntity<GetCartResp> getCart(@PathVariable Long userId) {
        Long userIdToken = LoginUserContextHolder.getUserId();
        if (!Objects.equals(userId, userIdToken)){
            // 如果 userId 不匹配，则设置 request 中的 userId 为当前用户 ID
            userId = userIdToken;
        }
        // 从 Redis 获取购物车数据
        Cart cart = cartRedisService.getCart(userId);
        List<Cart> carts = new ArrayList<>();
        // 并且将得到的有效商品列表放到carts中返回
        carts.add(cart);
        return ResponseEntity.ok(new GetCartResp(carts));
    }

    /**
     * 删除购物车某件商品
     *
     * @param deleteItem
     * @return
     */
    @DeleteMapping("/deleteItemOfCart")
    public ResponseEntity<ApiResponse> deleteProductOfCart(@RequestBody DeleteItemRequest deleteItem) {
        Long userId = LoginUserContextHolder.getUserId();
        if (!Objects.equals(userId, deleteItem.getUserId())){
            // 如果 userId 不匹配，则设置 request 中的 userId 为当前用户 ID
            deleteItem.setUserId(userId);
        }
        // 删除购物车中的某商品项
        cartRedisService.deleteCartItem(deleteItem);
        // 创建 ApiResponse 对象，返回 code 和 message
        ApiResponse response = new ApiResponse(200, "Cart item deleted successfully.");
        // 返回 ResponseEntity 包装 ApiResponse
        return ResponseEntity.ok(response);
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
    @DeleteMapping("/emptyCart/{userId}")
    public ResponseEntity<Void> emptyCart(@PathVariable Long userId) {
        Long userIdToken = LoginUserContextHolder.getUserId();
        if (!Objects.equals(userId, userIdToken)){
            // 如果 userId 不匹配，则设置 request 中的 userId 为当前用户 ID
            userId = userIdToken;
        }
        // 调用 RedisService 来清空购物车数据
        cartRedisService.clearCart(userId);
        logger.info("Cart emptied for user: " + userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 更新购物车某件商品的数量
     *
     * @param request
     * @return
     */
    @PutMapping("/updateQuantityOfCart")
    public ResponseEntity updateCart(@RequestBody @Validated UpdateItemRequest request) {
        Long userId = LoginUserContextHolder.getUserId();
        if (!Objects.equals(userId, request.getUserId())){
            // 如果 userId 不匹配，则设置 request 中的 userId 为当前用户 ID
            request.setUserId(userId);
        }
        // 更新购物车中某商品的数量
        cartRedisService.updateCart(request);
        return ResponseEntity.ok("Cart item updated successfully.");
    }

    /**
     * 保存购物车数据到MongoDB。在以下情况中调用该方法：
     * 1. 清空购物车
     * 2. 用户退出登陆的时候
     *
     * @param userId
     * @return
     */
    @PostMapping("/save/{userId}")
    public ResponseEntity<AddProductItemResp> saveCart(@PathVariable @NonNull Long userId) {
        Long userIdToken = LoginUserContextHolder.getUserId();
        if (!Objects.equals(userId, userIdToken)){
            // 如果 userId 不匹配，则设置 request 中的 userId 为当前用户 ID
            userId = userIdToken;
        }
        cartRedisService.saveCart(userId);
        logger.info("Save cart to mongoDB: " + userId);
        return ResponseEntity.ok(new AddProductItemResp(HttpStatus.OK.value(), "save cart to mongoDB successfully."));
    }
}