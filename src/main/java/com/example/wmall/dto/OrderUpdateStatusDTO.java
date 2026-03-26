package com.example.wmall.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 更新订单状态的请求参数 DTO
 * 用于支付成功、取消订单、发货等状态变更。
 */
@Data
public class OrderUpdateStatusDTO {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    @Min(value = 1, message = "订单ID非法")
    private Long id;

    /**
     * 订单状态：0-待支付，1-已支付，2-已取消
     */
    @NotNull(message = "订单状态不能为空")
    @Min(value = 0, message = "订单状态值非法")
    @Max(value = 2, message = "订单状态值非法")
    private Integer status;

    /**
     * 状态变更原因（可选）
     */
    @Size(max = 255, message = "状态变更原因长度不能超过255个字符")
    private String reason;
}
