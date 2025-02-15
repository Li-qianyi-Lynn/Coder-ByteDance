package com.coder.mall.gateway.auth;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [Sa-Token 权限认证] 配置类
 * @author click33
 */
@Configuration
public class SaTokenConfigure {
    // 注册 Sa-Token全局过滤器
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 拦截地址
                .addInclude("/**")    /* 拦截全部path */
                // 鉴权方法：每次访问进入
                .setAuth(obj -> {
                    // 登录校验
                    SaRouter.notMatch("/**") // 拦截所有路由
                            .notMatch("/auth/user/login") // 排除登录接口
                            .notMatch("/auth/verification/code/send") // 排除验证码发送接口
                            .check(r -> StpUtil.checkLogin()) // 校验是否登录
                            // TODO 排除掉登录所需的接口
                    ;

                    // 权限认证 -- 不同模块, 校验不同权限
                    // TODO 调用这里的 url 时，首先会执行 StpInterfaceImpl 中的获取权限列表，检验此用户有无权限
//                     SaRouter.match("/haha/**", r -> StpUtil.checkPermission("test"));
                    // TODO 同理，这个会执行获取 Role 列表，看用户有无担任角色
//                     SaRouter.match("/admin/**", r -> StpUtil.checkRole("admin"));
                    // SaRouter.match("/goods/**", r -> StpUtil.checkPermission("goods"));
                    // SaRouter.match("/orders/**", r -> StpUtil.checkPermission("orders"));

                    // 更多匹配 ...  */
                })
                // 异常处理方法：每次setAuth函数出现异常时进入
                .setError(e -> {
                    // return SaResult.error(e.getMessage());
                    // 手动抛出异常，抛给全局异常处理器
                    if (e instanceof NotLoginException) { // 未登录异常
                        throw new NotLoginException(e.getMessage(), null, null);
                    } else if (e instanceof NotPermissionException || e instanceof NotRoleException) { // 权限不足，或不具备角色，统一抛出权限不足异常
                        throw new NotPermissionException(e.getMessage());
                    } else { // 其他异常，则抛出一个运行时异常
                        throw new RuntimeException(e.getMessage());
                    }
                })
                ;
    }
}


