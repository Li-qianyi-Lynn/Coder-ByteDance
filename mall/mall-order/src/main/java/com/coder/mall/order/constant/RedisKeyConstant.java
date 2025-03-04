package com.coder.mall.order.constant;

public class RedisKeyConstant {
    private static final String APP_PREFIX = "mall";     // 应用前缀
    private static final String MODULE = "order";        // 模块名
    
    // 订单相关
    public static final String ORDER_SEQUENCE = APP_PREFIX + ":" + MODULE + ":seq";     // 序列号
    public static final String ORDER_CACHE = APP_PREFIX + ":" + MODULE + ":cache";      // 订单缓存
    
    // 购物车相关
    public static final String CART = APP_PREFIX + ":cart";                          
    
    // 缓存时间
    public static final long ORDER_CACHE_HOURS = 24;
    public static final long CART_CACHE_HOURS = 72;
    
    private RedisKeyConstant() {}
} 