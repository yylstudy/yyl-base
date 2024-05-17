package com.cqt.common.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * @date 2021/10/27 9:46
 * 双声道录音模式，取值范围如下：
 * 0：主叫录音到左声道，被叫录音到右声道。
 * 1：被叫录音到左声道，主叫录音到右声道。
 * 录音模式为双声道时有效，而且是必选
 * 默认主叫录音到左声道，被叫录音到右声道
 */
@Getter
public enum DualRecordModeEnum {

    /**
     *
     */
    CALLER_LEFT(0, "主叫录音到左声道，被叫录音到右声道。"),

    CALLED_LEFT(1, "被叫录音到左声道，主叫录音到右声道。");

    private final Integer code;
    private final String message;

    DualRecordModeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
