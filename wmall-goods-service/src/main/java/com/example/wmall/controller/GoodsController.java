package com.example.wmall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.wmall.common.Result;
import com.example.wmall.dto.GoodsAddDTO;
import com.example.wmall.dto.GoodsUpdateDTO;
import com.example.wmall.dto.GoodsUpdateStatusDTO;
import com.example.wmall.entity.Goods;
import com.example.wmall.service.GoodsService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Api;

@Api(tags = "商品管理")
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @ApiOperation("模糊搜索")
    @GetMapping("/page/search")
    public Result<IPage<Goods>> pageSearch(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minprice,
            @RequestParam(required = false) Double maxprice
    ) {
        IPage<Goods> pageList = goodsService.pageSearch(pageNum, pageSize, name, category, minprice, maxprice);
        return Result.success(pageList);
    }

    @ApiOperation("查询商品列表")
    @GetMapping("/page/list")
    public Result<IPage<Goods>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        IPage<Goods> pageList = goodsService.pageList(pageNum, pageSize);
        return Result.success(pageList);
    }

    @ApiOperation("添加商品")
    @PostMapping("/add")
    public Result<String> add(@Validated @RequestBody GoodsAddDTO goodsAddDTO) {
        String msg = goodsService.add(goodsAddDTO);
        return Result.success(msg);
    }

    @ApiOperation("删除商品")
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        String msg = goodsService.delete(id);
        return Result.success(msg);
    }

    @ApiOperation("更新商品")
    @PutMapping("/update")
    public Result<String> update(@Validated @RequestBody GoodsUpdateDTO goodsUpdateDTO) {
        String msg = goodsService.update(goodsUpdateDTO);
        return Result.success(msg);
    }

    @ApiOperation("更新商品状态")
    @PutMapping("/updateStatus")
    public Result<String> updateStatus(@Validated @RequestBody GoodsUpdateStatusDTO goodsUpdateStatusDTO) {
        String msg = goodsService.updateStatus(goodsUpdateStatusDTO);
        return Result.success(msg);
    }

    @ApiOperation("查询商品详情")
    @GetMapping("/detail/{id}")
    public Result<Goods> detail(@PathVariable Long id) {
        Goods goods = goodsService.getById(id);
        if (goods == null) {
            return Result.fail("商品不存在");
        }
        return Result.success(goods);
    }
}
