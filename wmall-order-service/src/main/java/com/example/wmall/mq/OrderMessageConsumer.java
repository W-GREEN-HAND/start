package com.example.wmall.mq;

import com.example.wmall.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单消息消费者：监听订单队列，处理订单创建成功的消息
 */
@Slf4j
@Component
public class OrderMessageConsumer {

    /**
     * 监听订单创建队列
     * @param message 消息内容
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handleOrderCreatedMessage(String message) {
        log.info("========== 收到订单消息 ==========");
        log.info("消息内容：{}", message);
        log.info("可以在这里做：发送通知、写操作日志、更新缓存等异步操作");
        log.info("==================================");
    }
}
