package com.example.wmall.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应结果枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCodeEnum {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权，请登录"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    SERVER_ERROR(500, "服务器错误"),
    BUSINESS_ERROR(600, "业务异常"),
    DATABASE_ERROR(601, "数据库错误");

    private final Integer code;

    private final String message;
}
