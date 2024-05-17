package com.cqt.common.enums;

import lombok.Getter;

/**
 * 错误码枚举
 *
 * @author hlx
 * @date 2021-09-09
 */
@Getter
public enum ErrorCodeEnum {

    /**
     * 错误码枚举类
     */
    OK(0, "成功"),

    OTHER_ERROR(-1, "其他错误"),

    REQUEST_BODY_ERROR(-1, "请求体格式错误"),

    TEL_TOO_MUCH(-1, "tel_b个数达到上限"),

    AUTH_FAIL(11, "验签失败"),

    SERVICE_DOWN(12, "服务暂时不可用"),

    POOL_LACK(101, "号池资源不足"),

    EXT_NUM_ALREADY_USED(101, "分机号已被使用, 或分机号不足"),

    TEL_B_POOL_LACK(101, "B号池资源不足"),

    FORMAT_ERROR(102, "号码格式不正确"),

    NOT_BIND(103, "绑定关系不存在"),

    OVER_BIND_TIME(104, "超出绑定时长"),

    EXIST_VALID_BIND(105, "已存在有效的绑定关系"),

    TEL_X_IS_USED(106, "X号码已被使用"),

    TEL_BIND_NOT_EXIST(107, "原绑定号码不存在"),

    TEL_X_NOT_EXIST(108, "X号码不存在平台号码池内"),

    DUPLICATE_A_B(109, "AB号码重复"),

    NUM_TYPE_NOT_EXIST(110, "号码类型不存在"),

    EXT_NUM_NOT_VALID(111, "分机号不正确"),

    CALLER_IN_BLACKLIST(112, "来电号码在黑名单内"),

    SYSTEM_ERROR(12, "系统异常"),

    /**
     * 400
     */
    CALL_REJECT(403, "通话拦截"),

    SMS_DROP(403, "短信拦截丢弃"),

    IMPORT_ERROR(520, "号码池添加错误"),

    NONE_NUMBER(521, "号码不存在"),

    PARAM_ERROR(522, "参数错误"),

    VCC_ID_NOT_EXIST(523, "企业不存在!");


    private final Integer code;
    private final String message;

    ErrorCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
