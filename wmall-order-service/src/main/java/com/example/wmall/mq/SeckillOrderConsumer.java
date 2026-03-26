package com.example.wmall.mq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.wmall.common.Result;
import com.example.wmall.config.RabbitMQConfig;
import com.example.wmall.entity.Goods;
import com.example.wmall.entity.Order;
import com.example.wmall.entity.OrderItem;
import com.example.wmall.entity.SeckillOrder;
import com.example.wmall.mapper.GoodsMapper;
import com.example.wmall.mapper.OrderItemMapper;
import com.example.wmall.mapper.OrderMapper;
import com.example.wmall.mapper.SeckillOrderMapper;
import com.example.wmall.mapper.SeckillStockMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SeckillOrderConsumer {

    private static final String KEY_PREFIX_STOCK = "sk:stock:";
    private static final String KEY_PREFIX_USER = "sk:user:";
    private static final String KEY_PREFIX_REQ = "sk:req:";
    private static final String KEY_PREFIX_RESULT = "sk:result:";
    private static final long RESULT_TTL_SECONDS = 60 * 60;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillStockMapper seckillStockMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_ORDER_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handle(String message) throws Exception {
        SeckillOrderMessage msg = objectMapper.readValue(message, SeckillOrderMessage.class);
        String requestId = msg.getRequestId();
        if (requestId == null || requestId.trim().isEmpty()) {
            return;
        }

        String resultKey = KEY_PREFIX_RESULT + requestId;

        SeckillOrder existed = seckillOrderMapper.selectOne(new QueryWrapper<SeckillOrder>().eq("request_id", requestId));
        if (existed != null) {
            if (existed.getOrderId() != null) {
                stringRedisTemplate.opsForValue().set(resultKey, "SUCCESS:" + existed.getOrderId(), RESULT_TTL_SECONDS, TimeUnit.SECONDS);
            }
            return;
        }

        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setRequestId(requestId);
        seckillOrder.setActivityId(msg.getActivityId());
        seckillOrder.setUserId(msg.getUserId());
        seckillOrder.setStatus(0);
        try {
            seckillOrderMapper.insert(seckillOrder);
        } catch (DuplicateKeyException e) {
            SeckillOrder again = seckillOrderMapper.selectOne(new QueryWrapper<SeckillOrder>().eq("request_id", requestId));
            if (again != null && again.getOrderId() != null) {
                stringRedisTemplate.opsForValue().set(resultKey, "SUCCESS:" + again.getOrderId(), RESULT_TTL_SECONDS, TimeUnit.SECONDS);
            }
            return;
        }

        int stockUpdated = seckillStockMapper.decreaseStock(msg.getActivityId(), msg.getQuantity());
        if (stockUpdated <= 0) {
            seckillOrder.setStatus(2);
            seckillOrderMapper.updateById(seckillOrder);
            rollbackPreDeduct(msg.getActivityId(), msg.getUserId());
            stringRedisTemplate.opsForValue().set(resultKey, "FAIL:库存不足", RESULT_TTL_SECONDS, TimeUnit.SECONDS);
            return;
        }

        Goods goods = goodsMapper.selectById(msg.getGoodsId());
        if (goods == null) {
            seckillOrder.setStatus(2);
            seckillOrderMapper.updateById(seckillOrder);
            rollbackPreDeduct(msg.getActivityId(), msg.getUserId());
            stringRedisTemplate.opsForValue().set(resultKey, "FAIL:商品不存在", RESULT_TTL_SECONDS, TimeUnit.SECONDS);
            return;
        }

        BigDecimal price = msg.getSeckillPrice();
        if (price == null) {
            seckillOrder.setStatus(2);
            seckillOrderMapper.updateById(seckillOrder);
            rollbackPreDeduct(msg.getActivityId(), msg.getUserId());
            stringRedisTemplate.opsForValue().set(resultKey, "FAIL:秒杀价格异常", RESULT_TTL_SECONDS, TimeUnit.SECONDS);
            return;
        }

        String orderNo = generateOrderNo();
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(msg.getUserId());
        order.setTotalAmount(price.multiply(new BigDecimal(msg.getQuantity())));
        order.setStatus(0);
        order.setRemark("seckill:" + msg.getActivityId());
        orderMapper.insert(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setGoodsId(goods.getId());
        item.setGoodsName(goods.getName());
        item.setGoodsPrice(price);
        item.setQuantity(msg.getQuantity());
        item.setSubtotalAmount(price.multiply(new BigDecimal(msg.getQuantity())));
        orderItemMapper.insert(item);

        seckillOrder.setOrderId(order.getId());
        seckillOrder.setStatus(1);
        seckillOrderMapper.updateById(seckillOrder);

        stringRedisTemplate.opsForValue().set(resultKey, "SUCCESS:" + order.getId(), RESULT_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private void rollbackPreDeduct(Long activityId, Long userId) {
        String stockKey = KEY_PREFIX_STOCK + activityId;
        String userKey = KEY_PREFIX_USER + activityId + ":" + userId;
        String reqKey = KEY_PREFIX_REQ + activityId + ":" + userId;
        stringRedisTemplate.delete(reqKey);
        stringRedisTemplate.opsForValue().increment(stockKey);
        Long userCnt = stringRedisTemplate.opsForValue().decrement(userKey);
        if (userCnt != null && userCnt < 0) {
            stringRedisTemplate.delete(userKey);
        }
    }

    private String generateOrderNo() {
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(900000) + 100000;
        return timeStr + random;
    }
}
