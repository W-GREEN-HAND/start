package com.example.wmall.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class OrderCreateDTO {

    @NotEmpty(message = "订单商品列表不能为空")
    private List<OrderItemCreateDTO> items;

    @Size(max = 255, message = "订单备注长度不能超过255个字符")
    private String remark;
}
