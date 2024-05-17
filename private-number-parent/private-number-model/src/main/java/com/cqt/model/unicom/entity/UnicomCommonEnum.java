package com.cqt.model.unicom.entity;

import lombok.Getter;

/**
 * 常量枚举
 *
 * @author zhengsuhao
 * @date 2022/12/20
 */
@Getter
public enum UnicomCommonEnum {
    /**
     *
     */
    MP3("mp3"),
    WAV("wav"),
    SCUESS("success"),
    FAIL("fail"),
    ZERO("0"),
    ONE("1"),
    TWO("2");

    private final String value;

    UnicomCommonEnum(String value) {
        this.value = value;
    }


}
