package com.example.wmall.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 编辑商品DTO
 */
@Data
public class GoodsUpdateDTO {
    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    @Min(value = 1, message = "商品ID非法")
    private Long id;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    @Size(min = 2, max = 255, message = "商品名称长度需在2-255位之间")
    private String name;

    /**
     * 商品描述
     */
    @NotBlank(message = "商品描述不能为空")
    @Size(min = 10, max = 1000, message = "商品描述长度需在10-1000位之间")
    private String description;

    /**
     * 商品价格
     */
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    @DecimalMax(value = "999999.99", message = "商品价格不能超过999999.99")
    private BigDecimal price;

    /**
     * 商品库存
     */
    @NotNull(message = "商品库存不能为空")
    @Min(value = 0, message = "商品库存不能为负数")
    private Integer stock;

    /**
     * 商品分类
     */
    @NotBlank(message = "商品分类不能为空")
    @Size(max = 100, message = "商品分类长度不能超过100")
    private String category;

    /**
     * 商品图片URL
     */
    @NotBlank(message = "商品图片不能为空")
    @Size(max = 500, message = "图片URL长度不能超过500")
    private String imageUrl;

    /**
     * 商品状态（0-下架，1-上架）
     */
    @NotNull(message = "商品状态不能为空")
    @Min(value = 0, message = "商品状态值非法")
    @Max(value = 1, message = "商品状态值非法")
    private Integer status;
}
