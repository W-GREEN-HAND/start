package com.example.wmall.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wmall.common.Result;
import com.example.wmall.dto.OrderCreateDTO;
import com.example.wmall.dto.OrderQueryDTO;
import com.example.wmall.dto.OrderVO;
import com.example.wmall.entity.Order;

public interface OrderService extends IService<Order> {

    Result<OrderVO> create(OrderCreateDTO dto);

    Result<List<OrderVO>> getMyOrders(OrderQueryDTO dto);

    Result<OrderVO> getOrderById(Long id);
}
