package com.coder.mall.user.utils;

import java.util.Random;

//生成随机验证码的工具
public class CodeUtil {
    public static String getRandomCode(Integer length) {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int num = random.nextInt(10);
            builder.append(num);
        }
        return builder.toString();
    }
}
