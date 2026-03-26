package com.example.wmall.dto;

import lombok.Data;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

/**
 * 订单查询条件 DTO（分页/条件查询订单列表时使用）
 */
@Data
public class OrderQueryDTO {

    /**
     * 用户ID（查询我的订单时使用）
     */
    @NotNull
    private Long userId;

    /**
     * 订单状态：0-待支付，1-已支付，2-已取消（可选）
     */
    private Integer status;

    /**
     * 下单开始时间（可选）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 下单结束时间（可选）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
