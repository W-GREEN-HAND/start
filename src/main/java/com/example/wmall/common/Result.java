package com.example.wmall.common;

import com.example.wmall.enums.ResultCodeEnum;
import lombok.Data;

/**
 * 统一返回结果
 */
@Data
public class Result<T> {
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 数据
     */
    private T data;

    /**
     * 成功响应（使用SUCCESS状态码）
     */
    public static <T> Result<T> success(T data) {
        return build(ResultCodeEnum.SUCCESS, data);
    }

    /**
     * 成功响应（使用自定义消息）
     */
    public static <T> Result<T> success(String msg, T data) {
        Result<T> response = new Result<>();
        response.setCode(ResultCodeEnum.SUCCESS.getCode());
        response.setMsg(msg);
        response.setData(data);
        return response;
    }

    /**
     * 失败响应（使用SERVER_ERROR状态码）
     */
    public static <T> Result<T> fail(String msg) {
        return build(ResultCodeEnum.SERVER_ERROR.getCode(), msg, null);
    }

    /**
     * 使用ResultCodeEnum构建响应
     */
    public static <T> Result<T> build(ResultCodeEnum resultCodeEnum, T data) {
        return build(resultCodeEnum.getCode(), resultCodeEnum.getMessage(), data);
    }

    /**
     * 使用状态码和消息构建响应
     */
    public static <T> Result<T> build(Integer code, String msg, T data) {
        Result<T> response = new Result<>();
        response.setCode(code);
        response.setMsg(msg);
        response.setData(data);
        return response;
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResultCodeEnum.SUCCESS.getCode().equals(this.code);
    }
}