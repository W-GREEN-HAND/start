package com.example.wmall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {

    private Long id;

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    private Integer status;

    private LocalDateTime payTime;

    private String remark;

    private LocalDateTime createTime;

    private List<OrderItemVO> items;
}
