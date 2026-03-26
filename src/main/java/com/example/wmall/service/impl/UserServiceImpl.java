package com.example.wmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wmall.dto.LoginDTO;
import com.example.wmall.dto.RegisterDTO;
import com.example.wmall.common.Result;
import com.example.wmall.entity.User;
import com.example.wmall.exception.BusinessException;
import com.example.wmall.mapper.UserMapper;
import com.example.wmall.service.UserService;

import com.example.wmall.utils.JwtUtil;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 用户注册
     */
    @Override
    public Result<?> register(RegisterDTO registerDTO) {
        // 1. 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", registerDTO.getUsername()) // 匹配用户名
                .eq("deleted", 0); // 排除已逻辑删除的用户
        User existUser = userMapper.selectOne(queryWrapper);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 密码加密
        String encodePassword = passwordEncoder.encode(registerDTO.getPassword());

        // 3. 保存用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(encodePassword);
        user.setEmail(registerDTO.getEmail());
        user.setRole("USER"); // 默认注册为普通用户
        int insert = userMapper.insert(user);
        if (insert <= 0) {
            log.error("用户注册失败，用户名：{}", registerDTO.getUsername());
            throw new BusinessException("注册失败");
        }

        log.info("用户注册成功，用户名：{}，邮箱：{}", registerDTO.getUsername(), registerDTO.getEmail());
        return Result.success("注册成功");
    }

    /**
     * 用户登录
     */
    @Override
    public Result<String> login(LoginDTO loginDTO) {
        try {
            // 1. 认证用户名密码
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
            );
        } catch (Exception e) {
            log.error("用户登录失败，用户名：{}，错误信息：{}", loginDTO.getUsername(), e.getMessage());
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 生成JWT token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getUsername());
        final String token = jwtUtil.generateToken(userDetails);

        // 将 token 存入 Redis，过期时间与 JWT 一致
        stringRedisTemplate.opsForValue().set(
                "login_token:" + token,
                userDetails.getUsername(),
                jwtUtil.getExpiration(),
                java.util.concurrent.TimeUnit.MILLISECONDS
        );

        log.info("用户登录成功，用户名：{}", loginDTO.getUsername());
        return Result.success(token);
    }

    @Override
    public Result<?> logout(String token) {
        if (StringUtils.isBlank(token)){
            throw new BusinessException("token不能为空");
        }
        try{
            Boolean isInBlacklist = stringRedisTemplate.opsForSet().isMember("blacklist_token" + token) != null;
            if(Boolean.TRUE.equals(isInBlacklist)){
                throw new BusinessException("token已失效");

            }
            stringRedisTemplate.opsForSet().add("blacklist_token",token);
            stringRedisTemplate.expire("blacklist_token",jwtUtil.getExpiration(), TimeUnit.MILLISECONDS);

        stringRedisTemplate.delete("login_token:" + token);
        
        log.info("用户登出成功，token：{}", token.substring(0, Math.min(20, token.length())) + "...");
        return Result.success("登出成功");
        }catch (Exception e){
            log.error("用户登出失败，错误信息：{}", e.getMessage(), e);
            throw new BusinessException("登出失败");
        }
    }

    @Override
    public Long getUserIdByUsername(String username) {
        // 调用Mapper的查询方法，返回用户ID
        return userMapper.getUserIdByUsername(username);
    }
}