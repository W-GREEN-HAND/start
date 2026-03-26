package com.example.wmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wmall.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细 Mapper 接口
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    // TODO: 根据需要添加自定义订单明细查询方法
}
