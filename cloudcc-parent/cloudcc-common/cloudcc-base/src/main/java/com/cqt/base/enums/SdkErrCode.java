package com.cqt.base.enums;

import lombok.Getter;

/**
 * @author Xienx
 * date 2023-07-24 17:16:17:16
 * SDK 错误码定义
 */
@Getter
public enum SdkErrCode {

    /**
     * 0 - 成功
     */
    OK("0", "成功!"),

    START_THREE_WAY_OK("0", "发起三方通话成功!"),

    START_FORCE_CALL_OK("0", "发起强插成功!"),

    /*平台配置异常 1xxx*/
    COMPANY_DISABLE("1000", "企业已禁用"),

    AGENT_DISABLE("1001", "坐席已禁用"),

    GET_REG_ADDR_FAILED("1002", "获取分机注册地址失败"),

    SDK_TOKEN_INVALID("1004", "鉴权不通过!"),

    /*坐席请求异常 4xxx*/
    UUID_NOT_FIND("4000", "uuid不存在!"),

    AGENT_INVALID("4001", "坐席校验不通过!"),

    PARAM_ERROR("4002", "参数错误!"),

    AGENT_NOT_EXIST("4003", "坐席不存在!"),

    AGENT_OFFLINE_PHONE_IN_USED("4003", "坐席离线号码已被使用!"),

    AGENT_INFO_UPDATE_FAIL("4003", "坐席信息更新失败!"),

    TOKEN_VERIFY_FAIL("4003", "token鉴权不通过!"),

    AGENT_NOT_CHECKIN("4003", "鉴权失败, 未签入的坐席!"),

    AGENT_SERVICE_MODE_NOT_CHANGE("4003", "坐席服务模式未变化!"),

    AGENT_PWD_ERROR("4004", "坐席账号或密码错误"),

    AGENT_NOT_BIND_EXT("4005", "坐席无绑定分机"),

    EXT_ID_NOT_EXT("4006", "分机id不存在"),

    EXT_ID_BINDING("4007", "分机id已被其他坐席绑定"),

    COMPANY_NOT_EXIST("4008", "企业不存在"),

    AGENT_NOT_CHECK_IN("4009", "坐席未签入"),

    AGENT_CHECKED_IN("4010", "签入失败, 坐席已经在其他地方签入"),

    EXT_CLIENT_OFFLINE("4011", "签入失败，话机不在线"),

    AGENT_BIND_EXT_ERROR("4012", "坐席绑定的分机与请求分机不一致"),

    FORCED_CHECK_OUT("4013", "系统强制签出"),

    MSG_TYPE_INVALID("4014", "msg_type非法"),

    GET_TOKEN_FAIL("4015", "获取token失败!"),

    AGENT_NOT_IN_CALLING_STATUS("4016", "坐席非通话状态!"),

    REQUEST_ERROR("4043", "请求失败!"),

    TYPE_NOT_SUPPORT("4044", "不支持该类型请求"),

    /*话务异常 5xxx*/
    START_THREE_WAY_FAIL("5100", "发起三方通话失败!"),

    START_FORCE_CALL_FAIL("5101", "发起强插失败!"),

    HANGUP_FAIL("5102", "调用底层服务, 挂断失败!"),

    BASE_REQUEST_ERROR("5103", "底层服务请求失败!"),

    CONCURRENCY_LIMIT_MAX("5000", "呼叫并发达到上限"),

    SYSTEM_EXCEPTION("9999", "系统异常");

    private final String code;

    private final String name;

    SdkErrCode(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
