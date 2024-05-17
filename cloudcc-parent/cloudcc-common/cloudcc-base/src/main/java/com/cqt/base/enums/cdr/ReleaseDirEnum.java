package com.cqt.base.enums.cdr;

import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-07-11 16:05
 * 挂机方向枚举
 */
@Getter
public enum ReleaseDirEnum {

    PLATFORM(0, "平台释放"),

    CALLER(1, "主叫释放"),

    CALLED(2, "被叫释放");

    private final Integer code;

    private final String name;

    ReleaseDirEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
