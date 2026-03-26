package com.example.wmall.controller;

import com.example.wmall.common.Result;
import com.example.wmall.dto.SeckillBuyDTO;
import com.example.wmall.dto.SeckillEnqueueVO;
import com.example.wmall.dto.SeckillResultVO;
import com.example.wmall.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @GetMapping("/path/{activityId}")
    public Result<String> createPath(
            @PathVariable Long activityId,
            @RequestHeader(value = "X-User-Name", required = false) String username
    ) {
        return seckillService.createPath(activityId, username);
    }

    @PostMapping("/{activityId}/{path}/buy")
    public Result<SeckillEnqueueVO> buy(
            @PathVariable Long activityId,
            @PathVariable String path,
            @RequestHeader(value = "X-User-Name", required = false) String username,
            @Valid @RequestBody SeckillBuyDTO dto
    ) {
        return seckillService.enqueue(activityId, path, username, dto);
    }

    @GetMapping("/result/{requestId}")
    public Result<SeckillResultVO> result(
            @PathVariable String requestId,
            @RequestHeader(value = "X-User-Name", required = false) String username
    ) {
        return seckillService.getResult(requestId, username);
    }
}
