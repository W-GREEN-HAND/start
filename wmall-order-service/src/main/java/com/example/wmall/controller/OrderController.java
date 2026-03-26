package com.example.wmall.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wmall.common.Result;
import com.example.wmall.dto.OrderCreateDTO;
import com.example.wmall.dto.OrderQueryDTO;
import com.example.wmall.dto.OrderVO;
import com.example.wmall.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateDTO dto) {
        return orderService.create(dto);
    }

    @GetMapping("/my")
    public Result<List<OrderVO>> getMyOrders(@Valid OrderQueryDTO dto) {
        return orderService.getMyOrders(dto);
    }

    @GetMapping("/{id}")
    public Result<OrderVO> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }
}
