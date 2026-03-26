package com.example.wmall.feign;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(name = "goods-service")
public interface GoodsClient {
    
}
