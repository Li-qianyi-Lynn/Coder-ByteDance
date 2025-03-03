package com.coder.mall.user.service;

import com.coder.mall.user.domain.domain.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coder.mall.user.model.vo.LoginVo;
import com.coder.mall.user.model.vo.RegisterVo;

import java.util.List;

/**
* @author Kevin
* @description 针对表【users(用户表)】的数据库操作Service
* @createDate 2025-02-02 21:11:25
*/
public interface UsersService extends IService<Users> {

    void sendCode(String mobile);

    String login(LoginVo loginVo);

    String register(RegisterVo registerVo);

    String logout();

    List<Users> getBatchUsers(List<Long> userIds);
}
