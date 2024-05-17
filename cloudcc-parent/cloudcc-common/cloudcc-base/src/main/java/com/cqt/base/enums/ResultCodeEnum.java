package com.cqt.base.enums;

import java.util.Optional;

/**
 * @author linshiqiang
 * date  2022-09-30 9:22
 * 响应码枚举
 */
public enum ResultCodeEnum {

    /**
     * 错误码枚举类
     */
    OK(0, "成功"),

    OTHER_ERROR(-1, "其他错误");

    private final Integer code;

    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 转化
     */
    public static Optional<ResultCodeEnum> of(Integer code) {
        Class<ResultCodeEnum> clazz = ResultCodeEnum.class;
        ResultCodeEnum[] enums = clazz.getEnumConstants();
        for (ResultCodeEnum e : enums) {
            if (e.getCode().equals(code)) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

}
