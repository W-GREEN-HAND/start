package com.example.wmall.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wmall.common.Result;
import com.example.wmall.dto.OrderCreateDTO;
import com.example.wmall.dto.OrderItemCreateDTO;
import com.example.wmall.dto.OrderVO;
import com.example.wmall.entity.Order;

/**
 * 订单服务接口（暂不包含具体方法定义）
 */
public interface OrderService extends IService<Order> {
    // TODO: 根据业务需要补充订单相关服务方法
    Result<OrderVO> create(OrderCreateDTO dto);
}
