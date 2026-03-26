package com.example.wmall.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细返回 VO
 */
@Data
public class OrderItemVO {

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品单价
     */
    private BigDecimal goodsPrice;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 小计金额
     */
    private BigDecimal subtotalAmount;
}
