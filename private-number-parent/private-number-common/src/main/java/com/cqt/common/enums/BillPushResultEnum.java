package com.cqt.common.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * @date 2021/10/27 9:46
 * 话单推送客户接口结果
 */
@Getter
public enum BillPushResultEnum {

    /**
     *
     */
    REACH_MAX_RETRY(0, "重推超过次数"),

    ERROR_RESULT_CODE(1, "接口返回结果码不正确");

    private final Integer code;
    private final String message;

    BillPushResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
