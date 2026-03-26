package com.example.wmall.mq;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillOrderMessage {

    private String requestId;

    private Long activityId;

    private Long userId;

    private Long goodsId;

    private BigDecimal seckillPrice;

    private Integer quantity;
}
