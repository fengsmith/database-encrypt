package com.example.service;

import com.example.dao.manual.mapper.CustomUserMapper;
import com.example.dao.mapper.UserMapper;
import com.example.dao.model.User;
import com.example.dto.UserDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 */
@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private CustomUserMapper customUserMapper;

    public void insert(UserDto userDto) {
        User user = convert(userDto);

        userMapper.insert(user);
    }

    public User query(int id) {
        User user = userMapper.selectByPrimaryKey(id);
        return user;
    }

    public User customQuery(int id) {
        User user = customUserMapper.queryById(id);
        return user;
    }

    public void customInsert(UserDto userDto) {
        User user = convert(userDto);

        customUserMapper.insert(user);
    }

    private User convert(UserDto userDto) {
        User user = new User();
        user.setNickName(userDto.getNickName());
        user.setRealName(userDto.getRealName());
        user.setPhone(userDto.getPhone());
        user.setAge(userDto.getAge());

        user.setCrateDate(new Date());
        user.setUpdateDate(new Date());

        return user;
    }
}
