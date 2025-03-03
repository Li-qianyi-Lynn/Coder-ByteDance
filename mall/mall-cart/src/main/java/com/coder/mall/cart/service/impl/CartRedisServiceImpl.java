package com.coder.mall.cart.service.impl;

import com.coder.mall.cart.constant.CartConstants;
import com.coder.mall.cart.dao.CartDao;
import com.coder.mall.cart.model.dto.CartProductItem;
import com.coder.mall.cart.model.dto.ProductDTO;
import com.coder.mall.cart.model.entity.Cart;
import com.coder.mall.cart.request.AddProductItemReq;
import com.coder.mall.cart.request.DeleteItemRequest;
import com.coder.mall.cart.request.UpdateItemRequest;
import com.coder.mall.cart.service.ICartRedisService;
import com.coder.mall.cart.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.coder.mall.cart.constant.CartConstants.CART_KEY;

@Service
public class CartRedisServiceImpl implements ICartRedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CartDao cartDao;

    @Autowired
    private ProductService productService;

    public HashOperations<String, Object, Object> getOpsHash() {
        return redisTemplate.opsForHash();
    }

    @Override
    public void addProductItem(AddProductItemReq request) {
        // 获取购物车数据
        Object userCart = redisTemplate.opsForValue().get(CART_KEY + request.getUserId().toString());
        // 将购物车数据转换为Map类型，Key值是商品的id，value是商品信息，但不包括价格信息
        Map<String, CartProductItem> redisData = (Map<String, CartProductItem>) userCart;

        if (redisData == null) {
            redisData = new HashMap<>();
        }
        // 获取对应Item
        CartProductItem cartProductItem = redisData.get(request.getProductId().toString());
        if (cartProductItem == null) {
            // 如果没有item，那么直接put
            redisData.put(request.getProductId().toString(),
                    new CartProductItem(request.getProductId(), request.getQuantity()));
        } else {
            // 如果有item，那么对数量进行更新
            cartProductItem.setQuantity(cartProductItem.getQuantity() + request.getQuantity());
            redisData.put(request.getProductId().toString(), cartProductItem);
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
        Map<String, CartProductItem> redisData = (Map<String, CartProductItem>) userCart;
        if (redisData == null) {
            return new Cart(userId);
        }
        List<CartProductItem> values = redisData.values().stream().toList();
        if (values.size() == 0) {
            return new Cart(userId);
        }
        // 创建一个新的map用来存储未过期的商品
        Map<String, CartProductItem> redisResult = new HashMap<>();
        // 商品Id和商品的map
        Map<Long, CartProductItem> productId2NumMap = new HashMap<>();
        // 购物车中的商品项
        List<CartProductItem> productResult = new ArrayList<>();
        values.stream()
                .forEach(item -> productId2NumMap.put(item.getProductId(), item));
        // 使用 map 提取 productId
        List<Long> productIds = values.stream()
                .map(CartProductItem::getProductId)
                .collect(Collectors.toList());
        List<ProductDTO> products = productService.listProductsByIds(productIds);
        if (null == products || products.size() == 0) {
            return new Cart(userId);
        }
        // 商品总价
        BigDecimal totalPrice = BigDecimal.ZERO;
        // 遍历查出来的商品信息，如果是已下架或者缺货状态，那么从redis中删除
        for (ProductDTO productDTO : products) {
            // 如果商品是正常可销售的，那么把商品写入redis中
            if (CartConstants.AVAILABLE.equals(productDTO.getStatus())) {
                Long productId = productDTO.getProductId();
                // 获取Redis中存储的商品id和数量
                CartProductItem cartProductItem = productId2NumMap.get(productId);
                redisResult.put(productId.toString(), cartProductItem);
                // 给返回结果添加数据
                cartProductItem.setIsValid(true);
                cartProductItem.setUnitPrice(productDTO.getPrice());
                productResult.add(cartProductItem);
                // 总价 = 总价 + 商品价格 * 商品数量
                totalPrice = totalPrice.add(productDTO.getPrice()
                        .multiply(BigDecimal.valueOf(cartProductItem.getQuantity())));
            } else {
                // 如果商品是下架的状态返回商品失效信息
                CartProductItem cartProductItem = productId2NumMap.get(productDTO.getProductId());
                // 给返回结果添加数据(不可用)
                cartProductItem.setIsValid(false);
                cartProductItem.setUnitPrice(productDTO.getPrice());
                productResult.add(cartProductItem);
            }
        }
        // 将数据重新写入
        redisTemplate.opsForValue().set(CART_KEY + userId, redisResult);
        // todo: 根据product表获取商品价格
        Cart cart = new Cart(userId, productResult.size(), productResult, totalPrice);
        return cart;
    }

    @Override
    public void deleteCartItem(DeleteItemRequest deleteItemRequest) {
        // 获取购物车数据
        Object userCart = redisTemplate.opsForValue().get(CART_KEY + deleteItemRequest.getUserId());
        // 将购物车数据转换为Map类型，Key值是商品的id，value是商品信息，但不包括价格信息
        Map<String, CartProductItem> redisData = (Map<String, CartProductItem>) userCart;

        if (redisData != null) {
            redisData.remove(deleteItemRequest.getProductId().toString());
            redisTemplate.opsForValue().set(CART_KEY + deleteItemRequest.getUserId(), redisData);
        }
    }

    @Override
    public void clearCart(Long userId) {
        // 获取购物车数据
        redisTemplate.delete(CART_KEY + userId);
        // 持久化-同时删除数据库中的数据
        cartDao.deleteByUserId(userId);
    }

    @Override
    public void updateCart(UpdateItemRequest request) {
        // 获取购物车数据
        Object userCart = redisTemplate.opsForValue().get(CART_KEY + request.getUserId());
        // 将购物车数据转换为Map类型，Key值是商品的id，value是商品信息，但不包括价格信息
        Map<String, CartProductItem> redisData = (Map<String, CartProductItem>) userCart;

        // 如果redisData为空，那么应该先初始化
        if (redisData != null) {
            redisData = new HashMap<>();
        }
        redisData.put(request.getProductId().toString(),
                new CartProductItem(request.getProductId(), request.getQuantity()));
        redisTemplate.opsForValue().set(CART_KEY + request.getUserId(), redisData);

        // 更新缓存过期时间（如1小时）
        redisTemplate.expire(CART_KEY + request.getUserId(), 1, TimeUnit.HOURS);
    }

    @Override
    public void updateCart(Long userId, Cart cart) {
        // 获取购物车数据
        Object userCart = redisTemplate.opsForValue().get(CART_KEY + userId);
        // 将购物车数据转换为Map类型，Key值是商品的id，value是商品信息，但不包括价格信息
        Map<String, CartProductItem> redisData = (Map<String, CartProductItem>) userCart;

        if (redisData == null) {
            redisData = new HashMap<>();
        }

        // 遍历购物车中的所有商品项，并更新 Redis 数据
        for (CartProductItem item : cart.getProductItems()) {
            redisData.put(item.getProductId().toString(), item);
        }

        redisTemplate.opsForValue().set(CART_KEY + userId, redisData);

        // 更新缓存过期时间（如1小时）
        redisTemplate.expire(CART_KEY + userId, 1, TimeUnit.HOURS);
    }


    @Override
    public void saveCart(Long userId) {
        // 获取购物车数据
        Object userCart = redisTemplate.opsForValue().get(CART_KEY + userId.toString());
        // 将购物车数据转换为Map类型，Key值是商品的id，value是商品信息，但不包括价格信息
        Map<String, CartProductItem> redisData = (Map<String, CartProductItem>) userCart;

        if (redisData != null) {
            List<CartProductItem> productItems = redisData.values().stream().toList();
            Cart cart = new Cart(userId, productItems);
            cartDao.saveOrUpdate(cart);
        }
    }
}
