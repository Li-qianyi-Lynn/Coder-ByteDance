package com.coder.mall.user.controller;

import com.coder.framework.biz.context.holder.LoginUserContextHolder;
import com.coder.framework.common.response.Response;
import com.coder.mall.user.domain.domain.Users;
import com.coder.mall.user.model.vo.LoginVo;
import com.coder.mall.user.model.vo.RegisterVo;
import com.coder.mall.user.service.UsersService;
import com.coder.mall.user.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UsersService usersService;

    @GetMapping("getCode")
    @Operation(summary = "获取短信验证码")
    public Response getCode(@RequestParam String mobile) {
        usersService.sendCode(mobile);
        return Response.success();
    }

    @PostMapping("login")
    @Operation(summary = "登录")
    public Response<String> login(LoginVo loginVo) {
        String token = usersService.login(loginVo);
        return Response.success(token);
    }

    @PostMapping("register")
    @Operation(summary = "注册")
    public Response<String> register(RegisterVo registerVo) {
        String result = usersService.register(registerVo);
        return Response.success(result);
    }

//    @GetMapping("userInfo")
//    @Operation(summary = "获取登录用户用户信息")
//    public Response userInfo(@RequestHeader("token") String token) {
//        Claims claims = JwtUtil.parseToken(token);
//        Long userId = Long.parseLong(claims.getId());
//        Users user = usersService.getById(userId);
//        return Response.success(user);
//    }

    @GetMapping("userInfo")
    @Operation(summary = "获取登录用户用户信息")
    public Response userInfo() {
        Long userId = LoginUserContextHolder.getUserId();
        Users user = usersService.getById(userId);
        return Response.success(user);
    }

    @GetMapping("getBatchUsers")
    @Operation(summary = "获取用户列表")
    public Response getBatchUsers(List<Long> userIds) {
        List<Users> users = usersService.getBatchUsers(userIds);
        return Response.success(users);
    }

    @GetMapping("logout")
    @Operation(summary = "退出登录")
    public Response logout(@RequestHeader("token") String token) {
        Claims claims = JwtUtil.parseToken(token);
        Long userId = Long.parseLong(claims.getId());
        usersService.logout(userId);
        return Response.success();
    }
}
