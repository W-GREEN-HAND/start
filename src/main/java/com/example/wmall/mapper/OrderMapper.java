package com.example.wmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wmall.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper 接口（暂不包含自定义方法）
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    // TODO: 根据需要添加自定义订单查询方法
}
