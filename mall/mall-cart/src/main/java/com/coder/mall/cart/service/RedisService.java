package com.coder.mall.cart.service;

public interface RedisService {

    void saveData(String key, Object value);

    Object getData(String key);

    void deleteData(String key);
}
