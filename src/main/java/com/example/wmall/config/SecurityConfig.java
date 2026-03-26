package com.example.wmall.config;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.example.wmall.Filter.JwtAuthenticationFilter;
import com.example.wmall.common.Result;
import com.example.wmall.entity.User;
import com.example.wmall.enums.ResultCodeEnum;
import com.example.wmall.mapper.UserMapper;
import com.example.wmall.utils.JwtUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置用户认证
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder);
    }

    /**
     * 配置HTTP安全
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 关闭CSRF
                .csrf().disable()
                // 不创建Session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 统一处理认证/授权异常
                .exceptionHandling()
                // 未登录或token无效（401）
                .authenticationEntryPoint((request, response, ex) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                            JSON.toJSONString(Result.build(ResultCodeEnum.UNAUTHORIZED, "未登录或登录已过期"))
                    );
                })
                // 已登录但权限不足（403）
                .accessDeniedHandler((request, response, ex) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                            JSON.toJSONString(Result.build(ResultCodeEnum.FORBIDDEN, "权限不足"))
                    );
                })
                .and()
                // 配置请求权限
                .authorizeRequests()
                // 放行注册和登录接口
                .antMatchers("/api/user/register", "/api/user/login").permitAll()
                // 商品查询接口允许所有人访问
                .antMatchers(HttpMethod.GET, "/goods/page/list", "/goods/page/search", "/goods/detail/*").permitAll()
                // 商品管理接口需要认证
                .antMatchers("/goods/**").hasRole("ADMIN")
                // 其他接口需要认证
                .anyRequest().authenticated()
                .and()
                 // .anyRequest().permitAll();
                // // 添加JWT过滤器（后续可扩展）//
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * 注入AuthenticationManager
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 自定义UserDetailsService
     */
    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return username -> {
            // 改用QueryWrapper查询用户
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username)
                    .eq("deleted", 0);
            User user = userMapper.selectOne(queryWrapper);

            if (user == null) {
                throw new UsernameNotFoundException("用户不存在");
            }
            // 构建UserDetails（根据用户角色设置权限）
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .authorities("ROLE_" + user.getRole())
                    .build();
        };
    }
}