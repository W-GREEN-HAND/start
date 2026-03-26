package com.example.wmall.utils;

import com.alibaba.fastjson.JSON;
import com.example.wmall.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class MessageNotifyUtil {
    @Value("${message.notify.url}")
    private String notifyUrl;
    @Value("${message.notify.app-key}")
    private String appKey;
    @Value("${message.notify.secret}")
    private String secret;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 推送内容发布通知
     */
    public void publishContentNotify(Long contentId, Long userId, String title) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("app-key", appKey);
            headers.add("secret", secret);

            Map<String, Object> params = new HashMap<>();
            params.put("contentId", contentId);
            params.put("userId", userId);
            params.put("title", title);
            params.put("type", "CONTENT_PUBLISH");
            params.put("createTime", System.currentTimeMillis());

            HttpEntity<String> request = new HttpEntity<>(JSON.toJSONString(params), headers);
            restTemplate.postForObject(notifyUrl, request, String.class);
        } catch (Exception e) {
            // 消息推送失败不影响主流程，仅打印日志
            System.err.println("消息通知推送失败：" + e.getMessage());
        }
    }
}