package com.example.wmall.client;

import com.example.wmall.feign.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * order-service 调用 user-service 的 Feign Client。
 * 具体的接口定义在 common 模块的 UserApi 中，这里只负责绑定服务名。
 */
@FeignClient(name = "wmall-user-service")
public interface UserClient extends UserApi {
}
