package com.example.wmall.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class GoodsUpdateStatusDTO {

    @NotNull(message = "商品ID不能为空")
    @Min(value = 1, message = "商品ID非法")
    private Long id;
    @NotNull(message = "商品状态值不能为空")
    @Min(value = 0, message = "商品状态值非法")
    @Max(value = 1, message = "商品状态值非法")
    private Integer status;
}
