package com.coder.mall.auth.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.coder.mall.auth.Entity.User;
import com.coder.mall.auth.Entity.UserType;
import com.coder.mall.auth.exception.AuthException; // 使用自定义异常
import com.coder.mall.auth.repository.UserRepository;
import com.coder.mall.auth.util.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static cn.dev33.satoken.stp.StpUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Long DEFAULT_EXPIRE_SECONDS = 7200L; // 默认有效期2小时
    private final UserRepository userRepository;

    // ======================== Token 生成 ========================

    /**
     * 生成访问令牌
     * @param userId 用户ID
     * @param userType 用户类型
     * @param expiredPeriod 有效期（秒），可选
     * @return 令牌响应
     */
    @Transactional(rollbackFor = Exception.class)
    public BaseResp<String> generateToken(Long userId, UserType userType, Long expiredPeriod)
            throws AuthException {

        // 1. 参数校验
        validateParams(userId, userType, expiredPeriod);

        // 2. 查询并验证用户
        User user = getUserAndValidate(userId, userType);

        // 3. 生成令牌会话
        return BaseResp.success(createTokenSession(user, expiredPeriod));
    }

    // ======================== Token 验证 ========================

    /**
     * 验证令牌有效性
     * @param token 访问令牌
     * @param resource 请求资源路径
     * @return 验证结果
     */
    public BaseResp<VerifyResult> verifyToken(String token, String resource) {
        try {
            // 1. 基础验证
            validateToken(token);

            // 2. 获取会话信息
            // 场景1：获取当前请求的Token-Session（推荐）
            SaSession session = getTokenSession();
            Long userId = session.getLong("userId");
            UserType userType = session.get("userType", UserType.class).newInstance();
            Set<String> permissions = session.get("permissions", Set.class).newInstance();

            // 3. 权限校验
            boolean isValid = checkResourceAccess(userType, permissions, resource);

            return BaseResp.success(new VerifyResult(isValid, userId));
        } catch (NotLoginException e) {
            log.warn("令牌已失效: {}", token);
            return BaseResp.error(401, "令牌已过期");
        } catch (AuthException e) {
            log.error("权限校验失败: {}", e.getMessage());
            return BaseResp.error(403, e.getMessage());
        } catch (Exception e) {
            log.error("系统异常: ", e);
            return BaseResp.error(500, "服务内部错误");
        }
    }

    // ======================== 私有方法 ========================

    /**
     * 参数校验
     */
    private void validateParams(Long userId, UserType userType, Long expiredPeriod)
            throws AuthException {

        if (userId == null || userId <= 0) {
            throw new AuthException("用户ID不合法");
        }
        if (userType == null) {
            throw new AuthException("用户类型不能为空");
        }
        if (expiredPeriod != null && expiredPeriod <= 0) {
            throw new AuthException("有效期必须大于0");
        }
    }

    /**
     * 用户存在性及类型匹配校验
     */
    private User getUserAndValidate(Long userId, UserType expectedType)
            throws AuthException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("用户不存在"));

        if (user.getUserType() != expectedType) {
            throw new AuthException("用户类型不匹配");
        }
        return user;
    }

    /**
     * 创建令牌会话
     */
    private String createTokenSession(User user, Long expiredPeriod) {
        // 清理旧会话
        logout(user.getId());

        // 创建新会话（动态有效期）
        Long effectiveExpire = Optional.ofNullable(expiredPeriod).orElse(DEFAULT_EXPIRE_SECONDS);
        login(user.getId(), effectiveExpire);

        // 存储会话元数据
        SaSession session = getSession();
        session.set("userId", user.getId());
        session.set("userType", user.getUserType());

        return getTokenValue();
    }

    /**
     * 令牌基础验证
     */
    private void validateToken(String token) throws AuthException {
        if (token == null || token.isEmpty()) {
            throw new AuthException("令牌不能为空");
        }
        if (!token.startsWith("satoken_")) {
            throw new AuthException("令牌格式错误");
        }
    }

    /**
     * 资源访问校验
     */
    private boolean checkResourceAccess(UserType userType, Set<String> permissions, String resource) {
        // 示例逻辑：
        // 1. 管理员拥有所有权限
        // 2. 其他用户根据权限列表校验
        return userType == UserType.MERCHANT ||
                permissions.stream().anyMatch(p -> resource.startsWith(p));
    }

    // ======================== 内部类 ========================

    @Data
    @AllArgsConstructor
    public static class VerifyResult {
        private Boolean isValid;
        private Long userId;
    }
}