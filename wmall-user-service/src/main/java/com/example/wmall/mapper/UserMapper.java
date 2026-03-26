package com.example.wmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wmall.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT id FROM sys_user WHERE username = #{username} LIMIT 1")
    Long getUserIdByUsername(@Param("username") String username);
}
