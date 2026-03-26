package com.example.wmall.feign;

import com.example.wmall.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 用户服务对外暴露的契约接口（仅定义路径、方法和返回值，不关心调用方式）。
 */
@RequestMapping("/api/user")
public interface UserApi {

    /**
     * 获取当前登录用户的信息（仅作示例）。
     */
    @PostMapping("/info")
    Result<?> getUserInfo();

    /**
     * 根据用户名获取用户ID，order-service 通过 Feign 调用这个接口，避免直接访问用户库。
     */
    @GetMapping("/id/{username}")
    Result<Long> getUserIdByUsername(@PathVariable("username") String username);
}
