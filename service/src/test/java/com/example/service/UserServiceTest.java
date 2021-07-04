package com.example.service;

import com.example.dao.model.User;
import com.example.dto.UserDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {
    @Resource
    private UserService userService;

    @Test
    public void insert() {
        UserDto userDto = new UserDto();
        userDto.setNickName("shfq1");
        userDto.setRealName("smith1");
        userDto.setPhone("18999999999");
        userDto.setAge(211);
        userService.insert(userDto);
    }

    @Test
    public void query() {
        User user = userService.query(1);
        System.out.println("");
    }

    @Test
    public void customInsert() {
        UserDto userDto = new UserDto();
        userDto.setNickName("lisi");
        userDto.setRealName("李四");
        userDto.setPhone("18977777777");
        userDto.setAge(22);
        userService.insert(userDto);
    }

    @Test
    public void customQuery() {
        User user = userService.query(3);
        System.out.println("");
    }
}