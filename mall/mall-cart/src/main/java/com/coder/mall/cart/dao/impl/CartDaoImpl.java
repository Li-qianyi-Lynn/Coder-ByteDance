package com.coder.mall.cart.dao.impl;

import com.coder.mall.cart.dao.CartDao;
import com.coder.mall.cart.model.entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import static com.coder.mall.cart.constant.CartConstants.CART_ID;

@Service
public class CartDaoImpl implements CartDao {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveOrUpdate(Cart cart) {
        if (null == cart) {
            return;
        }
        Query query = new Query(Criteria.where(CART_ID).is(cart.getUserId()));
        Update update = new Update()
                .set(CART_ID, cart.getUserId())
                .set("productItems", cart.getProductItems());

        mongoTemplate.upsert(query, update, Cart.class);
    }

    @Override
    public void deleteByUserId(Long userId) {
        if (null == userId) {
            return;
        }
        Query query = Query.query(Criteria.where(CART_ID).is(userId));
        mongoTemplate.findAndRemove(query, Cart.class);
    }
}
