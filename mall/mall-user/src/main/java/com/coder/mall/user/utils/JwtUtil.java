package com.coder.mall.user.utils;

import com.coder.mall.user.enums.ResultCode;
import com.coder.mall.user.exception.UserException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    private static SecretKey secretKey = Keys.hmacShaKeyFor("Coder-Coder-Coder-Coder-Coder-Coder".getBytes());//jwtUtils自带了Keys方法用来创建签名秘钥

    //创建token
    public static String createToken(Long userId, String mobile) {
        return Jwts.builder()//Jwts是为了创建待签名的jwt(构造器模式），并且jwt的header字段不需要管
                .setSubject("LOGIN_USER")//官方定义的字段：主体
                .setExpiration(new Date(System.currentTimeMillis() + 3600000 * 24 * 365L))//官方定义的字段：过期时间（这里设置的是过期的时间点，不是过期的时间长度）
                .claim("id", userId)//自定义字段：用户id
                .claim("mobile", mobile)//自定义字段：密码
                .signWith(secretKey, SignatureAlgorithm.HS256)//官方定义的字段：签名
                .compact();//把上面的字段整合起来，变成jwt
    }

    //解析token
    public static Claims parseToken(String token) {
        if (token == null) {
            throw new UserException(ResultCode.ACCOUNT_NOT_LOGIN_ERROR);
        }
        try {
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();
            Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);//解析token的payload字段
            return claimsJws.getBody();
        } catch (ExpiredJwtException e) {//token过期
            throw new UserException(ResultCode.TOKEN_EXPIRED);
        } catch (JwtException e) {//token非法
            throw new UserException(ResultCode.TOKEN_INVALID);
        }
    }

    public static void main(String[] args) {
        System.out.println(createToken(1L, "15288845875"));
    }


}
