package com.cqt.base.enums;

/**
 * @author linshiqiang
 * date:  2023-07-11 16:05
 * 内外线枚举
 */
public enum OutLineEnum {

    IN_LINE(0),

    OUT_LINE(1),

    OWT(2);

    private final Integer code;

    OutLineEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
