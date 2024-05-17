package com.cqt.base.enums.cdr;

import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-08-07 9:40
 * 挂断原因
 */
@Getter
public enum HangupCauseEnum {

    PREDICT_CALL_ASSIGNMENT_AGENT_FAIL("PREDICT_CALL_ASSIGNMENT_AGENT_FAIL", "Failure to predict an outbound agent assignment"),

    VIDEO_CONCURRENCY_LIMIT_MAX("VIDEO_CONCURRENCY_LIMIT_MAX", "视频呼叫并发达到上限"),

    AUDIO_CONCURRENCY_LIMIT_MAX("AUDIO_CONCURRENCY_LIMIT_MAX", "音频呼叫并发达到上限"),

    NUMBER_INVALID("NUMBER_INVALID", "平台挂断-号码无效"),

    NUMBER_SERVICE_INVALID("NUMBER_SERVICE_INVALID", "平台挂断-号码ivr服务或人工服务未配置"),

    NUMBER_UN_ENABLE("NUMBER_UN_ENABLE", "平台挂断-号码被禁用"),

    FORCE_CALL("FORCE_CALL", "强插"),

    INTERRUPT_CALL("INTERRUPT_CALL", "强拆"),

    SUBSTITUTE("SUBSTITUTE", "代接"),

    AGENT_REJECT("AGENT_REJECT", "坐席拒接"),

    AGENT_NO_ANSWER("AGENT_NO_ANSWER", "坐席未接听"),

    CALLER_RELEASE("CALLER_RELEASE", "主叫挂断"),

    CALLEE_RELEASE("CALLEE_RELEASE", "被叫挂断"),

    PLATFORM_RELEASE("PLATFORM_RELEASE", "平台挂断"),

    FORCE_CHECKOUT("FORCE_CHECKOUT", "管理员强签"),

    CHECKOUT("CHECKOUT", "坐席签出"),

    RESET("RESET", "复位"),

    AGENT_HANGUP("AGENT_HANGUP", "坐席主动发起挂断"),

    AGENT_HANGUP_AND_HANGUP_OTHER("AGENT_HANGUP_AND_HANGUP_OTHER", "坐席主动发起挂断并挂断所有关联通话"),

    BLIND_TRANS_HANGUP("BLIND_TRANS_HANGUP", "盲转挂断原坐席"),

    CANCEL_CONSULT_TRANS("CANCEL_CONSULT_TRANS", "取消咨询转"),

    CONSULT_TO_TRANS("CONSULT_TO_TRANS", "坐席咨询中转接"),

    TRANS_SKILL("TRANS_SKILL", "转接技能"),

    TRANS_SKILL_TIMEOUT("TRANS_SKILL_TIMEOUT", "转接技能, 分配坐席超时, 挂断来电"),

    TRANS_IVR("TRANS_IVR", "转接IVR, 挂断坐席"),

    TRANS_SATISFACTION("TRANS_SATISFACTION", "转接满意度, 挂断坐席"),

    IVR_MESSAGE_STOP("IVR_MESSAGE_STOP", "IVR留言主动结束"),

    IVR_MESSAGE_TIMEOUT("IVR_MESSAGE_TIMEOUT", "IVR留言超时挂断"),

    CANCEL_CONSULT_HANGUP("CANCEL_CONSULT_HANGUP", "坐席主动取消咨询, 挂断咨询方"),

    CANCEL_CONSULT_TRANS_AGENT_HANGUP("CANCEL_CONSULT_TRANS_AGENT_HANGUP", "咨询转成功, 挂断发起咨询转坐席"),

    CALL_IN_NOT_FIND_NUMBER_INFO("CALL_IN_NOT_FIND_NUMBER_INFO", "客户呼入, 未找到被叫号码信息, 挂断客户"),

    CALL_IN_NUMBER_INVALID("CALL_IN_NUMBER_INVALID", "呼入号码无效, 挂断客户"),

    CALL_IN_NUMBER_IN_BLACK_LIST("CALL_IN_NUMBER_IN_BLACK_LIST", "呼入号码在黑名单内"),

    CALL_BRIDGE_FAIL("CALL_BRIDGE_FAIL", "坐席外呼后, 发起外呼并桥接失败, 挂断当前坐席通话"),

    PREVIEW_OUT_CALL_BRIDGE_FAIL("PREVIEW_OUT_CALL_BRIDGE_FAIL", "预览外呼 - 坐席外呼后, 发起外呼并桥接失败, 挂断当前坐席通话"),

    XFER_FAIL("XFER_FAIL", "调用底层xfer失败"),

    PLAY_RECORD_END_HANGUP("PLAY_RECORD_END_HANGUP", "录音播放结束, 挂断坐席"),

    HANGUP_ALL("HANGUP_ALL", "挂断所有关联通话");

    private final String name;

    private final String desc;

    HangupCauseEnum(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

}
