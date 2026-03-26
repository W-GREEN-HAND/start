package com.example.wmall.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class OrderUpdateStatusDTO {

    @NotNull(message = "订单ID不能为空")
    @Min(value = 1, message = "订单ID非法")
    private Long id;

    @NotNull(message = "订单状态不能为空")
    @Min(value = 0, message = "订单状态值非法")
    @Max(value = 2, message = "订单状态值非法")
    private Integer status;

    @Size(max = 255, message = "状态变更原因长度不能超过255个字符")
    private String reason;
}
