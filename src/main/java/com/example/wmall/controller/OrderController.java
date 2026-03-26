package com.example.wmall.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wmall.common.Result;
import com.example.wmall.dto.OrderCreateDTO;
import com.example.wmall.dto.OrderItemCreateDTO;
import com.example.wmall.dto.OrderQueryDTO;
import com.example.wmall.dto.OrderVO;
import com.example.wmall.service.OrderService;


/**
 * 订单控制器（暂不包含具体接口实现）
 */
@RestController
@RequestMapping("/orders")
public class OrderController {
    // TODO: 在此定义订单相关的接口（下单、查询订单等）
    @Autowired
    private OrderService orderService;

    @PostMapping("/create") // 创建订单
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateDTO dto) {
        // TODO: 调用服务层创建订单的逻辑
     
        return orderService.create(dto);
    }

    @GetMapping("/my") // 获取我的订单
    public Result<List<OrderVO>> getMyOrders(@Valid OrderQueryDTO dto) {
        // TODO: 调用服务层获取我的订单的逻辑
        return orderService.getMyOrders(dto);
    }
}
