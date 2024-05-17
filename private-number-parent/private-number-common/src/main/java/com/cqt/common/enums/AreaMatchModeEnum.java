package com.cqt.common.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-02-21 17:54
 * 地市编码匹配规则
 * 当whole_area=1时该字段无效
 * 1: 地市池不足, 分配全国号码池;
 * 2: 地市池不足, 不分配全国号码池(默认值)
 */
@Getter
public enum AreaMatchModeEnum {

    /**
     *
     */
    USE_WHOLE_POOl(1, "地市池不足, 分配全国号码池"),

    NOT_USE_WHOLE_POOl(2, "地市池不足, 不分配全国号码池(默认值)");

    private final Integer code;
    private final String message;

    AreaMatchModeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
