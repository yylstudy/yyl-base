package com.cqt.base.enums.ext;

import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-07-11 16:05
 * 分机通话模式
 */
@Getter
public enum ExtCallModeEnum {

    NORMAL(1),

    LONG_CALL(2);

    private final Integer code;

    ExtCallModeEnum(Integer code) {
        this.code = code;
    }

}
