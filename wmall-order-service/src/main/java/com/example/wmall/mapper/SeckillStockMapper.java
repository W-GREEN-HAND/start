package com.example.wmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wmall.entity.SeckillStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillStockMapper extends BaseMapper<SeckillStock> {

    @Update("UPDATE seckill_stock SET available_stock = available_stock - #{num} " +
            "WHERE activity_id = #{activityId} AND available_stock >= #{num}")
    int decreaseStock(@Param("activityId") Long activityId, @Param("num") Integer num);
}
