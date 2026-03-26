package com.example.wmall.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {
    /**
     * 下架/禁用/无效
     */
    DISABLED(0, "下架"),

    /**
     * 上架/启用/有效
     */
    ENABLED(1, "上架");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 枚举对象，如果不存在返回null
     */
    public static StatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StatusEnum statusEnum : StatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

    /**
     * 检查状态码是否有效
     *
     * @param code 状态码
     * @return 是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }

    /**
     * 获取所有的状态码
     *
     * @return 状态码数组
     */
    public static Integer[] getAllCodes() {
        Integer[] codes = new Integer[StatusEnum.values().length];
        StatusEnum[] values = StatusEnum.values();
        for (int i = 0; i < values.length; i++) {
            codes[i] = values[i].getCode();
        }
        return codes;
    }
}
