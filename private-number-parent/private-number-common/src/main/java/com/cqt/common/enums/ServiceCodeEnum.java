package com.cqt.common.enums;

import lombok.Getter;

/**
 * ServiceCode
 *
 * @author hlx
 * @date 2021-09-09
 */
@Getter
public enum ServiceCodeEnum {

    /**
     *
     */
    AXB(10, "AXB"),

    AXBN(11, "AXBN"),

    AXEYB_AXE(20, "AXEYB_AXE"),

    AXEYB_AYB(21, "AXEYB_AYB"),

    AXE(22, "AXE"),

    AX(23, "AX"),

    AXEBN(24, "AXEBN"),

    AXYB(25, "AXYB"),

    AXG(26, "AXG"),

    ;

    private final Integer code;
    private final String desc;

    ServiceCodeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
