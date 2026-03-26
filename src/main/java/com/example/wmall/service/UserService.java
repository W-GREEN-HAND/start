package com.example.wmall.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wmall.dto.LoginDTO;
import com.example.wmall.dto.RegisterDTO;
import com.example.wmall.common.Result;
import com.example.wmall.entity.User;

public interface UserService extends IService<User> {
    // 调用Mapper的查询方法，返回用户ID
    Long getUserIdByUsername(String username);
    /**
     * 用户注册
     */
    Result<?> register(RegisterDTO registerDTO);

    /**
     * 用户登录（生成JWT token）
     */
    Result<String> login(LoginDTO loginDTO);

    Result<?> logout(String token);

}