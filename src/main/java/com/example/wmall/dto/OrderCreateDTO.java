package com.example.wmall.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 创建订单的请求参数 DTO（下单使用）
 * 方案B：一个订单可以包含多个商品明细
 */
@Data
public class OrderCreateDTO {

    /**
     * 订单商品明细列表
     */
    @NotEmpty(message = "订单商品列表不能为空")
    private List<OrderItemCreateDTO> items;

    /**
     * 订单备注
     */
    @Size(max = 255, message = "订单备注长度不能超过255个字符")
    private String remark;
}
