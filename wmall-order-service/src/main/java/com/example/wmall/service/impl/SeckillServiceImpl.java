package com.example.wmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.wmall.client.UserClient;
import com.example.wmall.common.Result;
import com.example.wmall.dto.SeckillBuyDTO;
import com.example.wmall.dto.SeckillEnqueueVO;
import com.example.wmall.dto.SeckillResultVO;
import com.example.wmall.entity.Goods;
import com.example.wmall.entity.SeckillActivity;
import com.example.wmall.entity.SeckillStock;
import com.example.wmall.mapper.GoodsMapper;
import com.example.wmall.mapper.SeckillActivityMapper;
import com.example.wmall.mapper.SeckillStockMapper;
import com.example.wmall.config.RabbitMQConfig;
import com.example.wmall.mq.SeckillOrderMessage;
import com.example.wmall.service.SeckillService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    private static final String KEY_PREFIX_ACTIVITY = "sk:act:";
    private static final String KEY_PREFIX_STOCK = "sk:stock:";
    private static final String KEY_PREFIX_USER = "sk:user:";
    private static final String KEY_PREFIX_REQ = "sk:req:";
    private static final String KEY_PREFIX_PATH = "sk:path:";
    private static final String KEY_PREFIX_RESULT = "sk:result:";

    private static final long PATH_TTL_SECONDS = 60;
    private static final long REQUEST_GUARD_TTL_MILLIS = 5 * 60 * 1000L;
    private static final long RESULT_TTL_SECONDS = 60 * 60;

    private static final DefaultRedisScript<Long> PRE_DEDUCT_SCRIPT;

    static {
        String lua = ""
                + "local stock = tonumber(redis.call('GET', KEYS[1]) or '-1');"
                + "if stock <= 0 then return -2 end;"
                + "local userCnt = tonumber(redis.call('GET', KEYS[2]) or '0');"
                + "local limit = tonumber(ARGV[1]);"
                + "if (userCnt + 1) > limit then return -3 end;"
                + "if redis.call('EXISTS', KEYS[3]) == 1 then return -4 end;"
                + "redis.call('DECR', KEYS[1]);"
                + "redis.call('INCR', KEYS[2]);"
                + "redis.call('SET', KEYS[3], ARGV[2], 'PX', ARGV[3]);"
                + "return 1;";
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(lua);
        script.setResultType(Long.class);
        PRE_DEDUCT_SCRIPT = script;
    }

    @Autowired
    private SeckillActivityMapper seckillActivityMapper;

    @Autowired
    private SeckillStockMapper seckillStockMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserClient userClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Result<String> createPath(Long activityId, String username) {
        Long userId = getUserId(username);
        SeckillActivity activity = getActivity(activityId);
        if (!isActivityOnline(activity)) {
            return Result.fail("秒杀活动未开始或已结束");
        }

        String raw = UUID.randomUUID().toString().replace("-", "");
        String path = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));

        String key = KEY_PREFIX_PATH + activityId + ":" + userId;
        stringRedisTemplate.opsForValue().set(key, path, PATH_TTL_SECONDS, TimeUnit.SECONDS);
        return Result.success(path);
    }

    @Override
    public Result<SeckillEnqueueVO> enqueue(Long activityId, String path, String username, SeckillBuyDTO dto) {
        Long userId = getUserId(username);
        SeckillActivity activity = getActivity(activityId);
        if (!isActivityOnline(activity)) {
            return Result.fail("秒杀活动未开始或已结束");
        }

        String pathKey = KEY_PREFIX_PATH + activityId + ":" + userId;
        String storedPath = stringRedisTemplate.opsForValue().get(pathKey);
        if (storedPath == null || !storedPath.equals(path)) {
            return Result.fail("秒杀路径无效或已过期");
        }

        ensureStockKey(activityId);

        int limit = activity.getLimitPerUser() == null ? 1 : activity.getLimitPerUser();
        if (limit <= 0) {
            limit = 1;
        }

        String requestId = UUID.randomUUID().toString().replace("-", "");
        String stockKey = KEY_PREFIX_STOCK + activityId;
        String userKey = KEY_PREFIX_USER + activityId + ":" + userId;
        String reqKey = KEY_PREFIX_REQ + activityId + ":" + userId;

        List<String> keys = Arrays.asList(stockKey, userKey, reqKey);
        Long code = stringRedisTemplate.execute(
                PRE_DEDUCT_SCRIPT,
                keys,
                String.valueOf(limit),
                requestId,
                String.valueOf(REQUEST_GUARD_TTL_MILLIS)
        );

        if (code == null) {
            return Result.fail("系统繁忙，请稍后重试");
        }
        if (code == -2L) {
            return Result.fail("库存不足");
        }
        if (code == -3L) {
            return Result.fail("超出限购数量");
        }
        if (code == -4L) {
            return Result.fail("请求已受理，请勿重复提交");
        }
        if (code != 1L) {
            return Result.fail("秒杀失败");
        }

        Goods goods = goodsMapper.selectById(activity.getGoodsId());
        if (goods == null) {
            rollbackPreDeduct(activityId, userId);
            return Result.fail("商品不存在");
        }

        BigDecimal price = activity.getSeckillPrice();
        if (price == null) {
            rollbackPreDeduct(activityId, userId);
            return Result.fail("秒杀价格未配置");
        }

        String resultKey = KEY_PREFIX_RESULT + requestId;
        stringRedisTemplate.opsForValue().set(resultKey, "PROCESSING", RESULT_TTL_SECONDS, TimeUnit.SECONDS);

        SeckillOrderMessage msg = new SeckillOrderMessage();
        msg.setRequestId(requestId);
        msg.setActivityId(activityId);
        msg.setUserId(userId);
        msg.setGoodsId(activity.getGoodsId());
        msg.setSeckillPrice(price);
        msg.setQuantity(dto.getQuantity());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SECKILL_EXCHANGE,
                    RabbitMQConfig.SECKILL_ORDER_ROUTING_KEY,
                    objectMapper.writeValueAsString(msg)
            );
        } catch (Exception e) {
            log.error("秒杀消息投递失败，requestId={}", requestId, e);
            rollbackPreDeduct(activityId, userId);
            stringRedisTemplate.opsForValue().set(resultKey, "FAIL:消息投递失败", RESULT_TTL_SECONDS, TimeUnit.SECONDS);
            return Result.fail("系统繁忙，请稍后重试");
        }

        SeckillEnqueueVO vo = new SeckillEnqueueVO();
        vo.setRequestId(requestId);
        return Result.success(vo);
    }

    @Override
    public Result<SeckillResultVO> getResult(String requestId, String username) {
        getUserId(username);
        String resultKey = KEY_PREFIX_RESULT + requestId;
        String raw = stringRedisTemplate.opsForValue().get(resultKey);
        if (raw == null) {
            SeckillResultVO vo = new SeckillResultVO();
            vo.setStatus("NOT_FOUND");
            vo.setMessage("结果不存在或已过期");
            return Result.success(vo);
        }
        if ("PROCESSING".equals(raw)) {
            SeckillResultVO vo = new SeckillResultVO();
            vo.setStatus("PROCESSING");
            vo.setMessage("排队中");
            return Result.success(vo);
        }
        if (raw.startsWith("SUCCESS:")) {
            String idStr = raw.substring("SUCCESS:".length());
            SeckillResultVO vo = new SeckillResultVO();
            vo.setStatus("SUCCESS");
            vo.setMessage("下单成功");
            try {
                vo.setOrderId(Long.parseLong(idStr));
            } catch (Exception ignored) {
            }
            return Result.success(vo);
        }
        if (raw.startsWith("FAIL:")) {
            SeckillResultVO vo = new SeckillResultVO();
            vo.setStatus("FAIL");
            vo.setMessage(raw.substring("FAIL:".length()));
            return Result.success(vo);
        }

        SeckillResultVO vo = new SeckillResultVO();
        vo.setStatus("UNKNOWN");
        vo.setMessage(raw);
        return Result.success(vo);
    }

    private SeckillActivity getActivity(Long activityId) {
        String cacheKey = KEY_PREFIX_ACTIVITY + activityId;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, SeckillActivity.class);
            } catch (Exception ignored) {
            }
        }

        SeckillActivity activity = seckillActivityMapper.selectById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("秒杀活动不存在");
        }

        Duration ttl = ttlForActivity(activity);
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(activity), ttl.getSeconds(), TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
        return activity;
    }

    private Duration ttlForActivity(SeckillActivity activity) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = activity.getEndTime() == null ? now.plusMinutes(10) : activity.getEndTime();
        long seconds = Duration.between(now, end).getSeconds();
        if (seconds < 10) {
            seconds = 10;
        }
        if (seconds > 24 * 60 * 60) {
            seconds = 24 * 60 * 60;
        }
        return Duration.ofSeconds(seconds);
    }

    private boolean isActivityOnline(SeckillActivity activity) {
        if (activity == null) {
            return false;
        }
        if (activity.getStatus() == null || activity.getStatus() != 1) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime() != null && now.isBefore(activity.getStartTime())) {
            return false;
        }
        if (activity.getEndTime() != null && now.isAfter(activity.getEndTime())) {
            return false;
        }
        return true;
    }

    private void ensureStockKey(Long activityId) {
        String key = KEY_PREFIX_STOCK + activityId;
        Boolean exists = stringRedisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            return;
        }
        SeckillStock stock = seckillStockMapper.selectOne(new QueryWrapper<SeckillStock>().eq("activity_id", activityId));
        if (stock == null || stock.getAvailableStock() == null) {
            stringRedisTemplate.opsForValue().set(key, "0", 10, TimeUnit.SECONDS);
            return;
        }
        stringRedisTemplate.opsForValue().set(key, String.valueOf(stock.getAvailableStock()), 24, TimeUnit.HOURS);
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

    private Long getUserId(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("未登录或缺少用户信息");
        }
        Result<Long> result = userClient.getUserIdByUsername(username);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new IllegalArgumentException("用户不存在或获取用户信息失败");
        }
        return result.getData();
    }
}
