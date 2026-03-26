package com.example.wmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细实体类
 */
@Data
@TableName("t_order_item")
public class OrderItem {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID（关联 t_order.id）
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 商品ID（关联 goods.id）
     */
    @TableField("goods_id")
    private Long goodsId;

    /**
     * 商品名称快照
     */
    @TableField("goods_name")
    private String goodsName;

    /**
     * 商品单价快照
     */
    @TableField("goods_price")
    private BigDecimal goodsPrice;

    /**
     * 购买数量
     */
    @TableField("quantity")
    private Integer quantity;

    /**
     * 小计金额 = 单价 * 数量
     */
    @TableField("subtotal_amount")
    private BigDecimal subtotalAmount;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
