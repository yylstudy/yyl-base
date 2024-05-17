package com.cqt.base.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-07-04 9:58
 * 坐席与客户通话的角色枚举
 */
@Getter
public enum CallRoleEnum {

    /**
     * 坐席主叫
     */
    AGENT_CALLER(0, "AGENT_CALLER", "坐席外呼"),

    /**
     * 客户被叫
     */
    CLIENT_CALLEE(0, "CLIENT_CALLEE", "客户被叫"),

    /**
     * 客户主叫
     */
    CLIENT_CALLER(1, "CLIENT_CALLER", "客户呼入"),

    /**
     * 坐席被叫
     */
    AGENT_CALLEE(1, "AGENT_CALLEE", "坐席被叫"),

    /**
     * 客户呼入转给坐席
     */
    CALLIN_TRANSFER_AGENT(1, "CALLIN_TRANSFER_AGENT", "客户呼入转给坐席"),

    /**
     * 客户呼入转给离线坐席手机号
     */
    CALLIN_TRANSFER_OFFLINE_AGENT_PHONE(1, "CALLIN_TRANSFER_OFFLINE_AGENT_PHONE", "客户呼入转给离线坐席手机号"),

    /**
     * 监听坐席
     */
    EAVESDROP_AGENT(2, "EAVESDROP_AGENT", "监听坐席"),

    /**
     * 耳语坐席
     */
    WHISPER_AGENT(3, "WHISPER_AGENT", "耳语坐席"),

    /**
     * 咨询坐席
     */
    CONSULT_AGENT(4, "CONSULT_AGENT", "咨询坐席"),

    /**
     * 咨询外线
     */
    CONSULT_CLIENT(4, "CONSULT_CLIENT", "咨询外线"),

    /**
     * 咨询中转接坐席
     */
    CONSULT_TO_TRANS_AGENT(5, "CONSULT_TO_TRANS_AGENT", "咨询中转接坐席"),

    /**
     * 咨询中转接外线
     */
    CONSULT_TO_TRANS_CLIENT(5, "CONSULT_TO_TRANS_CLIENT", "咨询中转接外线"),

    /**
     * 转接坐席
     */
    TRANS_AGENT(5, "TRANS_AGENT", "转接坐席"),

    /**
     * ivr呼入转接坐席
     */
    CALL_IN_IVR_TRANS_AGENT(5, "CALL_IN_IVR_TRANS_AGENT", "ivr呼入转接坐席"),

    /**
     * 转接外线
     */
    TRANS_CLIENT(5, "TRANS_CLIENT", "转接外线"),

    /**
     * ivr呼入转接外线
     */
    CALL_IN_IVR_TRANS_CLIENT(5, "CALL_IN_IVR_TRANS_CLIENT", "ivr呼入转接外线"),

    /**
     * 代接坐席
     */
    SUBSTITUTE_AGENT(6, "SUBSTITUTE_AGENT", "代接坐席"),

    /**
     * 三方通话坐席
     */
    THREE_WAY_AGENT(7, "THREE_WAY_AGENT", "三方通话坐席"),

    /**
     * 三方通话外线
     */
    THREE_WAY_CLIENT(7, "THREE_WAY_CLIENT", "三方通话外线"),

    /**
     * 强插
     */
    FORCE_CALL_AGENT(8, "FORCE_CALL_AGENT", "强插");

    private final Integer code;

    private final String name;

    private final String desc;

    CallRoleEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }
}
