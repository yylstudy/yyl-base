package com.cqt.common.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * @date 2021/10/27 9:46
 * ax 模型
 * 非tel号码呼叫 tel_x时，被叫tel看见的来电显示号码为如下取值；
 * 1：系统随机分配虚号Y ; 默认值
 * 2: tel_x
 */
@Getter
public enum ModelEnum {

    /**
     *
     */
    TEL_Y(1, "系统随机分配虚号Y"),

    TEL_X(2, "tel_x, 默认值"),

    REAL_TEL(3, "真实号码");

    private final Integer code;
    private final String message;

    ModelEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
