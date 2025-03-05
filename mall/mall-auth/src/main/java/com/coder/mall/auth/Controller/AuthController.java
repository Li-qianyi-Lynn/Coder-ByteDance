package com.coder.mall.auth.Controller;


import com.coder.mall.auth.Entity.UserType;
import com.coder.mall.auth.service.AuthService;
import com.coder.mall.auth.util.BaseResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证中心")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "生成访问令牌")
    @PostMapping("/token")
    public BaseResp<String> generateToken(
            @Parameter(required = true, description = "用户ID") @RequestParam("userId") Long userId,
            @Parameter(required = true, description = "用户类型") @RequestParam("userType") UserType userType,
            @Parameter(description = "有效期（秒）") @RequestParam(value = "expiredPeriod", defaultValue = "7200") Long expiredPeriod) throws AuthException, com.coder.mall.auth.exception.AuthException {
        return authService.generateToken(userId, userType, expiredPeriod);
    }

    @Operation(summary = "验证令牌")
    @GetMapping("/verify")
    public BaseResp<AuthService.VerifyResult> verifyToken(
            @Parameter(required = true, description = "访问令牌") @RequestParam("token") String token,
            @Parameter(required = true, description = "请求资源") @RequestParam("resource") String resource) {
        return authService.verifyToken(token, resource);
    }
}