package com.example.wmall.Filter;

import com.example.wmall.common.Result;
import com.example.wmall.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 不需要鉴权的白名单路径
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/user/login",
            "/api/user/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单直接放行
        if (isWhitePath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("请求未携带有效的Authorization头，path={}", path);
            return unauthorized(exchange, "未登录或登录已过期");
        }

        String token = authHeader.substring(7);

        try {
            // 1. 黑名单校验（登出后 token 会加入黑名单）
            Boolean inBlacklist = stringRedisTemplate.opsForSet().isMember("blacklist_token", token);
            if (Boolean.TRUE.equals(inBlacklist)) {
                log.warn("token 在黑名单中，拒绝访问");
                return unauthorized(exchange, "登录状态已失效，请重新登录");
            }

            // 2. 登录态校验：必须在 login_token:<token> 中有记录
            String redisKey = "login_token:" + token;
            String usernameInRedis = stringRedisTemplate.opsForValue().get(redisKey);
            if (usernameInRedis == null) {
                log.warn("Redis 中未找到登录态，token 可能已过期或未登录");
                return unauthorized(exchange, "未登录或登录已过期");
            }

            // 3. JWT 基本校验：能否解析 + 是否过期
            String username = jwtUtil.extractUsername(token);
            Date expiration = jwtUtil.extractExpiration(token);
            if (expiration.before(new Date())) {
                log.warn("JWT 已过期，username={}", username);
                return unauthorized(exchange, "登录已过期，请重新登录");
            }

            // 4. 透传用户信息到下游服务
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Name", username)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            log.error("JWT 解析或校验失败: {}", e.getMessage(), e);
            return unauthorized(exchange, "无效的登录状态");
        }
    }

    private boolean isWhitePath(String path) {
        for (String white : WHITE_LIST) {
            if (path.startsWith(white)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");

        Result<?> result = Result.fail(message);
        byte[] bytes;
        try {
            bytes = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(result)
                    .getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            String fallback = "{\"code\":401,\"message\":\"" + message + "\"}";
            bytes = fallback.getBytes(StandardCharsets.UTF_8);
        }

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        // 数值越小优先级越高，尽量在链路前面做认证
        return -100;
    }
}
