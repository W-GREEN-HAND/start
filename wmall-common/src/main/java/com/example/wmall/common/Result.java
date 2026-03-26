package com.example.wmall.common;

import com.example.wmall.enums.ResultCodeEnum;
import lombok.Data;

/**
 * 统一返回结果
 */
@Data
public class Result<T> {

    private Integer code;

    private String msg;

    private T data;

    public static <T> Result<T> success(T data) {
        return build(ResultCodeEnum.SUCCESS, data);
    }

    public static <T> Result<T> success(String msg, T data) {
        Result<T> response = new Result<>();
        response.setCode(ResultCodeEnum.SUCCESS.getCode());
        response.setMsg(msg);
        response.setData(data);
        return response;
    }

    public static <T> Result<T> fail(String msg) {
        return build(ResultCodeEnum.SERVER_ERROR.getCode(), msg, null);
    }

    public static <T> Result<T> build(ResultCodeEnum resultCodeEnum, T data) {
        return build(resultCodeEnum.getCode(), resultCodeEnum.getMessage(), data);
    }

    public static <T> Result<T> build(Integer code, String msg, T data) {
        Result<T> response = new Result<>();
        response.setCode(code);
        response.setMsg(msg);
        response.setData(data);
        return response;
    }

    public boolean isSuccess() {
        return ResultCodeEnum.SUCCESS.getCode().equals(this.code);
    }
}
