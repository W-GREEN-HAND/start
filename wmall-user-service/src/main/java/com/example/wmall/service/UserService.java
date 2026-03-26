package com.example.wmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wmall.dto.LoginDTO;
import com.example.wmall.dto.RegisterDTO;
import com.example.wmall.common.Result;
import com.example.wmall.entity.User;

public interface UserService extends IService<User> {
    Long getUserIdByUsername(String username);

    Result<?> register(RegisterDTO registerDTO);

    Result<String> login(LoginDTO loginDTO);

    Result<?> logout(String token);
}
