package com.coder.mall.user;

import com.coder.mall.user.service.UsersService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestCRUD {

    @Autowired
    private UsersService usersService;

    @Test
    public void test() {
        System.out.println("users: " + usersService.list());
    }
}
