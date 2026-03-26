package com.example.wmall.mq;

import com.example.wmall.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单消息生产者：负责发送订单创建成功的消息
 */
@Slf4j
@Component
public class OrderMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送订单创建成功的消息
     * @param orderId 订单ID
     */
    public void sendOrderCreatedMessage(Long orderId) {
        try {
            String message = "订单创建成功，订单ID：" + orderId;
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE, 
                RabbitMQConfig.ORDER_ROUTING_KEY, 
                message
            );
            log.info("发送订单消息成功：{}", message);
        } catch (Exception e) {
            log.error("发送订单消息失败，订单ID：{}", orderId, e);
        }
    }
}
