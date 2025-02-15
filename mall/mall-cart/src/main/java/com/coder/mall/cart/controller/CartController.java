package com.coder.mall.cart.controller;

import com.coder.mall.cart.model.dto.CartProductItem;
import com.coder.mall.cart.model.entity.Cart;
import com.coder.mall.cart.request.AddProductItemReq;
import com.coder.mall.cart.response.GetCartResp;
import com.coder.mall.cart.service.CartService;
import com.coder.mall.cart.service.ICartRedisService;
import com.coder.mall.cart.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ICartRedisService cartRedisService;

    /**
     * 添加商品到购物车
     *
     * @param request
     * @return
     */
    @PostMapping("/add")
    public ResponseEntity<Void> addProductItem(@RequestBody AddProductItemReq request) {
        // 调用 cartService 处理添加商品的业务逻辑
        cartService.addProductItem(request);

        // 通过 cartRedisService 更新购物车数据到 Redis
        CartProductItem productItem = request.getProductItem();
        Cart cart = new Cart(request.getUserId(), productItem.getProductId(), productItem.getQuantity());
        cartRedisService.addCart(cart);

        logger.info("Added item to cart: " + productItem.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 获取购物车列表
     *
     * @param userId
     * @return
     */
//    @GetMapping("/list/{userId}")
//    public ResponseEntity<GetCartResp> getCart(@PathVariable int userId) {
//        GetCartReq request = new GetCartReq(userId);
//        return cartService.getCart(request);
//    }
    @GetMapping("/list/{userId}")
    public ResponseEntity<GetCartResp> getCart(@PathVariable int userId) {
        // 从 Redis 获取购物车数据
        List<Cart> cartList = cartRedisService.listCart(userId);

        // 创建响应对象
        GetCartResp response = new GetCartResp(cartList);

        return ResponseEntity.ok(response);
    }


    /**
     * 清空购物车
     *
     * @param userId
     * @return
     */
//    @DeleteMapping("/empty/{userId}")
//    public ResponseEntity<Void> emptyCart(@PathVariable int userId) {
//        EmptyCartReq request = new EmptyCartReq(userId);
//        return cartService.emptyCart(request);
//    }
    @DeleteMapping("/empty/{userId}")
    public ResponseEntity<Void> emptyCart(@PathVariable int userId) {
        // 调用 RedisService 来清空购物车
        cartRedisService.delAll(new Cart(userId));

//        log.info("Cart emptied for user: " + userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 删除购物车某件商品
     *
     * @param cart
     * @return
     */
    @DeleteMapping("/delete")
    public Boolean delCart(@RequestBody Cart cart) {
        return cartRedisService.delCart(cart);
    }

    /**
     * 删除购物车所有商品
     *
     * @param cart
     */
    @DeleteMapping("/deleteAll")
    public void delAll(@RequestBody Cart cart) {
        cartRedisService.delAll(cart);
    }

    /**
     * 更新购物车商品数量
     *
     * @param cart
     * @param type
     */
    @PutMapping("/update")
    public void updateCart(@RequestBody Cart cart, @RequestParam String type) {
        cartRedisService.updateCart(cart, type);
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