package com.example.wmall.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class GoodsUpdateDTO {

    @NotNull(message = "商品ID不能为空")
    @Min(value = 1, message = "商品ID非法")
    private Long id;

    @NotBlank(message = "商品名称不能为空")
    @Size(min = 2, max = 255, message = "商品名称长度需在2-255位之间")
    private String name;

    @NotBlank(message = "商品描述不能为空")
    @Size(min = 10, max = 1000, message = "商品描述长度需在10-1000位之间")
    private String description;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    @DecimalMax(value = "999999.99", message = "商品价格不能超过999999.99")
    private BigDecimal price;

    @NotNull(message = "商品库存不能为空")
    @Min(value = 0, message = "商品库存不能为负数")
    private Integer stock;

    @NotBlank(message = "商品分类不能为空")
    @Size(max = 100, message = "商品分类长度不能超过100")
    private String category;

    @NotBlank(message = "商品图片不能为空")
    @Size(max = 500, message = "图片URL长度不能超过500")
    private String imageUrl;

    @NotNull(message = "商品状态不能为空")
    @Min(value = 0, message = "商品状态值非法")
    @Max(value = 1, message = "商品状态值非法")
    private Integer status;
}
