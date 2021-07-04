package com.example.dao.manual.mapper;

import com.example.dao.model.User;

/**
 */
public interface CustomUserMapper {
    void insert(User user);

    User queryById(int id);

}
