package com.example.wmall.service;

import com.example.wmall.common.Result;
import com.example.wmall.dto.SeckillBuyDTO;
import com.example.wmall.dto.SeckillEnqueueVO;
import com.example.wmall.dto.SeckillResultVO;

public interface SeckillService {

    Result<String> createPath(Long activityId, String username);

    Result<SeckillEnqueueVO> enqueue(Long activityId, String path, String username, SeckillBuyDTO dto);

    Result<SeckillResultVO> getResult(String requestId, String username);
}
