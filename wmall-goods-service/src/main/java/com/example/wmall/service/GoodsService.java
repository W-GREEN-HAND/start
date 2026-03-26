package com.example.wmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wmall.dto.GoodsAddDTO;
import com.example.wmall.dto.GoodsUpdateDTO;
import com.example.wmall.dto.GoodsUpdateStatusDTO;
import com.example.wmall.entity.Goods;

public interface GoodsService extends IService<Goods> {

    IPage<Goods> pageList(Integer pageNum, Integer pageSize);

    String add(GoodsAddDTO goodsAddDTO);

    String delete(Long id);

    String update(GoodsUpdateDTO goodsUpdateDTO);

    String updateStatus(GoodsUpdateStatusDTO goodsUpdateStatusDTO);

    IPage<Goods> pageSearch(Integer pageNum, Integer pageSize, String name, String category, Double minprice, Double maxprice);
}
