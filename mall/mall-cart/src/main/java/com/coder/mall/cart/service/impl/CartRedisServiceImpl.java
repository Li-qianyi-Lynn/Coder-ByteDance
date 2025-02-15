package com.coder.mall.cart.service.impl;

import com.coder.mall.cart.model.entity.Cart;
import com.coder.mall.cart.service.ICartRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class CartRedisServiceImpl implements ICartRedisService {

    @Autowired
    private RedisTemplate<String, Object> template;

    public HashOperations<String, Object, Object> getOpsHash() {
        return template.opsForHash();
    }

//    @Override
//    public Integer addCart(Cart cart) {
//        Cart existingCart = (Cart) getOpsHash().get(cart.getUsersId() + "", cart.getGoodsId() + "");
//        if (existingCart != null) {
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

    @Override
    public Integer addCart(Cart cart) {
        return 0;
    }

    @Override
    public List<Cart> listCart(Integer uid) {
        return List.of();
    }

    @Override
    public Boolean delCart(Cart cart) {
        return null;
    }

    @Override
    public void updateCart(Cart cart, String type) {
        return;
    }

    @Override
    public void delAll(Cart cart) {

    }

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
