package com.coder.mall.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coder.mall.user.custom.constant.RedisConstant;
import com.coder.mall.user.domain.domain.Users;
import com.coder.mall.user.enums.ResultCode;
import com.coder.mall.user.enums.UserStatus;
import com.coder.mall.user.exception.UserException;
import com.coder.mall.user.model.vo.LoginVo;
import com.coder.mall.user.model.vo.RegisterVo;
import com.coder.mall.user.service.SmsService;
import com.coder.mall.user.service.UsersService;
import com.coder.mall.user.domain.mapper.UsersMapper;
import com.coder.mall.user.utils.CodeUtil;
import com.coder.mall.user.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Kevin
 * @description 针对表【users(用户表)】的数据库操作Service实现
 * @createDate 2025-02-02 21:11:25
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

    @Autowired
    private SmsService smsService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UsersMapper usersMapper;

    @Override
    public void sendCode(String mobile) {
        //生成一个6位数的验证码，以短信的形式放到手机上；并且以手机号作为key验证码作为value保存到redis中，以供后续验证验证码是否有效
        String key = RedisConstant.PREFIX + mobile;
        String code = CodeUtil.getRandomCode(6);
        //查看Redis中是否存在这个key
        Boolean hasKey = stringRedisTemplate.hasKey(key);
        if (hasKey) {
            Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);//获取该key对应的TTL（间隔）
            if (ttl > RedisConstant.CODE_TTL_SEC - RedisConstant.CODE_RESEND_TIME_SEC) {
                throw new UserException(ResultCode.SEND_SMS_TOO_OFTEN);
            }
        }
        smsService.sendSms(mobile, code);//把code以短信验证码的形式发送给phone
        stringRedisTemplate.opsForValue().set(key, code, RedisConstant.CODE_TTL_SEC, TimeUnit.SECONDS);
    }

    @Override
    public String login(LoginVo loginVo) {
        //1.验证手机号和验证码是否为空
        if (!StringUtils.hasLength(loginVo.getMobile())) {
            throw new UserException(ResultCode.PHONE_EMPTY);
        }
        if (!StringUtils.hasLength(loginVo.getCode())) {
            throw new UserException(ResultCode.CODE_EMPTY);
        }
        //2.查询Redis中是否存在这个验证码code
        String key = RedisConstant.PREFIX + loginVo.getMobile();
        String code = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasLength(code)) {
            throw new UserException(ResultCode.CODE_EXPIRED);
        }
        //3.验证验证码是否正确
        if (!loginVo.getCode().equals(code)) {
            throw new UserException(ResultCode.CODE_ERROR);
        }
        //4.查询数据库中是否存在这个手机号对应的用户
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getMobile, loginVo.getMobile());
        Users user = usersMapper.selectOne(queryWrapper);
        //5.如果不存在
        if (user == null) {
            throw new UserException(ResultCode.ACCOUNT_NOT_EXIST_ERROR);
        } else {
            //是否禁用
            if (user.getUserStatus() == UserStatus.DISABLE) {
                throw new UserException(ResultCode.ACCOUNT_DISABLED_ERROR);
            }
        }
        //6.如果存在，返回token
        StpUtil.login(user.getId());
        return "登录成功！";
//        return JwtUtil.createToken(user.getId(), user.getMobile());
    }

    @Override
    public String register(RegisterVo registerVo) {
        if (!StringUtils.hasLength(registerVo.getMobile())) {
            throw new UserException(ResultCode.PHONE_EMPTY);
        }
        if (!StringUtils.hasLength(registerVo.getCode())) {
            throw new UserException(ResultCode.CODE_EMPTY);
        }
        String key = RedisConstant.PREFIX + registerVo.getMobile();
        String code = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasLength(code)) {
            throw new UserException(ResultCode.CODE_EXPIRED);
        }
        if (!registerVo.getCode().equals(code)) {
            throw new UserException(ResultCode.CODE_ERROR);
        }
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getMobile, registerVo.getMobile());
        Users user = usersMapper.selectOne(queryWrapper);
        if (user != null) {
            throw new UserException(ResultCode.ACCOUNT_EXIST_ERROR);
        }
        user = new Users();
        user.setMobile(registerVo.getMobile());
        user.setUserType(registerVo.getUserType());
        user.setPassword(MD5Util.encrypt(registerVo.getPassword()));
        user.setNickname("Coder-" + registerVo.getMobile().substring(7));
        user.setUserStatus(UserStatus.ENABLE);
        usersMapper.insert(user);
        return "注册成功！";
    }

    @Override
    public String logout() {
        StpUtil.logout();
        return "欢迎下次光临！";
    }

    @Override
    public List<Users> getBatchUsers(List<Long> userIds) {
        return usersMapper.selectBatchIds(userIds);
    }
}




