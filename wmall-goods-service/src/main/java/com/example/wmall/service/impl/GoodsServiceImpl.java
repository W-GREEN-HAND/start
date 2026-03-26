package com.example.wmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wmall.dto.GoodsAddDTO;
import com.example.wmall.dto.GoodsUpdateDTO;
import com.example.wmall.dto.GoodsUpdateStatusDTO;
import com.example.wmall.entity.Goods;
import com.example.wmall.enums.ResultCodeEnum;
import com.example.wmall.exception.BusinessException;
import com.example.wmall.mapper.GoodsMapper;
import com.example.wmall.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public IPage<Goods> pageSearch(Integer pageNum, Integer pageSize, String name, String category, Double minprice, Double maxprice) {
        IPage<Goods> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        if (category != null && !category.isEmpty()) {
            queryWrapper.like("category", category);
        }
        if (minprice != null) {
            queryWrapper.ge("price", minprice);
        }
        if (maxprice != null) {
            queryWrapper.le("price", maxprice);
        }
        return this.page(page, queryWrapper);
    }

    @Override
    public IPage<Goods> pageList(Integer pageNum, Integer pageSize) {
        Page<Goods> page = new Page<>(pageNum, pageSize);
        return this.page(page);
    }

    @Override
    public String add(GoodsAddDTO goodsAddDTO) {
        log.info("开始添加商品，商品名称：{}，分类：{}，价格：{}",
                goodsAddDTO.getName(), goodsAddDTO.getCategory(), goodsAddDTO.getPrice());

        Goods goods = new Goods();
        BeanUtils.copyProperties(goodsAddDTO, goods);

        boolean result = this.save(goods);

        if (result) {
            log.info("商品添加成功，商品ID：{}，商品名称：{}", goods.getId(), goods.getName());
            return ResultCodeEnum.SUCCESS.getMessage();
        } else {
            log.error("商品添加失败，商品名称：{}", goodsAddDTO.getName());
            throw new BusinessException(ResultCodeEnum.BUSINESS_ERROR.getMessage());
        }
    }

    @Override
    public String delete(Long id) {
        log.info("开始删除商品，商品ID：{}", id);

        Goods goods = this.getById(id);
        if (goods == null) {
            log.warn("删除失败，商品不存在，商品ID：{}", id);
            throw new BusinessException(ResultCodeEnum.NOT_FOUND.getMessage());
        }

        boolean result = this.removeById(id);

        if (result) {
            log.info("商品删除成功，商品ID：{}，商品名称：{}", id, goods.getName());
            return ResultCodeEnum.SUCCESS.getMessage();
        } else {
            log.error("商品删除失败，商品ID：{}", id);
            throw new BusinessException(ResultCodeEnum.BUSINESS_ERROR.getMessage());
        }
    }

    @Override
    public String update(GoodsUpdateDTO goodsUpdateDTO) {
        log.info("开始更新商品，商品ID：{}，商品名称：{}",
                goodsUpdateDTO.getId(), goodsUpdateDTO.getName());

        Goods goods = this.getById(goodsUpdateDTO.getId());
        if (goods == null) {
            log.warn("更新失败，商品不存在，商品ID：{}", goodsUpdateDTO.getId());
            throw new BusinessException(ResultCodeEnum.NOT_FOUND.getMessage());
        }

        Goods updateGoods = new Goods();
        BeanUtils.copyProperties(goodsUpdateDTO, updateGoods);

        boolean result = this.updateById(updateGoods);

        if (result) {
            log.info("商品更新成功，商品ID：{}，商品名称：{}", goodsUpdateDTO.getId(), goodsUpdateDTO.getName());
            return ResultCodeEnum.SUCCESS.getMessage();
        } else {
            log.error("商品更新失败，商品ID：{}", goodsUpdateDTO.getId());
            throw new BusinessException(ResultCodeEnum.BUSINESS_ERROR.getMessage());
        }
    }

    @Override
    public String updateStatus(GoodsUpdateStatusDTO goodsUpdateStatusDTO) {
        log.info("开始更新商品状态，商品ID：{}，目标状态：{}",
                goodsUpdateStatusDTO.getId(), goodsUpdateStatusDTO.getStatus());

        Goods goods = this.getById(goodsUpdateStatusDTO.getId());
        if (goods == null) {
            log.warn("更新状态失败，商品不存在，商品ID：{}", goodsUpdateStatusDTO.getId());
            throw new BusinessException(ResultCodeEnum.NOT_FOUND.getMessage());
        }

        Goods updateGoods = new Goods();
        updateGoods.setId(goodsUpdateStatusDTO.getId());
        updateGoods.setStatus(goodsUpdateStatusDTO.getStatus());

        boolean result = this.updateById(updateGoods);

        if (result) {
            log.info("商品状态更新成功，商品ID：{}，商品名称：{}，原状态：{}，新状态：{}",
                    goods.getId(), goods.getName(), goods.getStatus(), goodsUpdateStatusDTO.getStatus());
            return ResultCodeEnum.SUCCESS.getMessage();
        } else {
            log.error("商品状态更新失败，商品ID：{}", goodsUpdateStatusDTO.getId());
            throw new BusinessException(ResultCodeEnum.BUSINESS_ERROR.getMessage());
        }
    }
}
