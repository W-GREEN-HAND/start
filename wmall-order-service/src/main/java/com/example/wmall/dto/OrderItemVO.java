package com.example.wmall.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {

    private Long goodsId;

    private String goodsName;

    private BigDecimal goodsPrice;

    private Integer quantity;

    private BigDecimal subtotalAmount;
}
