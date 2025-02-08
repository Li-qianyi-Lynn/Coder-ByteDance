package com.coder.mall.user.controller;

import com.coder.common.response.Response;
import com.coder.mall.user.model.vo.LoginVo;
import com.coder.mall.user.model.vo.RegisterVo;
import com.coder.mall.user.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UsersService usersService;

    @GetMapping("/login/getCode")
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

    @GetMapping("register")
    @Operation(summary = "注册")
    public Response<String> register(RegisterVo registerVo) {
        String result = usersService.register(registerVo);
        return Response.success(result);
    }

}
