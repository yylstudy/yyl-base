package com.cqt.common.enums;

import lombok.Getter;

/**
 * ServiceCode
 *
 * @author dingsh
 * @date 2022-08-01
 */
@Getter
public enum CdrTypeCodeEnum {

    /**
     *
     */
    inside("0", "内部"),
    supplier("1", "第三方"),




    ;

    private final String code;
    private final String desc;

    CdrTypeCodeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
