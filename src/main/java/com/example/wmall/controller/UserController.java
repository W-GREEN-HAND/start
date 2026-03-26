package com.example.wmall.controller;



import com.example.wmall.dto.LoginDTO;
import com.example.wmall.dto.RegisterDTO;
import com.example.wmall.common.Result;
import com.example.wmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Result<String> login(@Validated @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }
    /**
     * 登出接口
     */
    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Result.fail("token不能为空");
        }
        String token = authorizationHeader.substring(7);
        return userService.logout(token);
    }
    
     /* 测试认证接口（需要token）
     */
    @PostMapping("/info")
    public Result<?> getUserInfo() {
        return Result.success("获取用户信息成功（需要token认证）");
    }
}