package com.cqt.base.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-07-07 14:04
 * 外呼坐席之后,
 * 在坐席接通事件需要做的事
 * 外呼并桥接客户
 * xfer
 */
@Getter
public enum OriginateAfterActionEnum {

    /**
     * ivr流程执行
     */
    IVR("ivr"),

    /**
     * 语音通知
     */
    VOICE_NOTICE("voice-notice"),

    /**
     * 预测外呼任务
     */
    PREDICT_TASK("predict"),

    /**
     * 预览外呼任务
     */
    PREVIEW_TASK("preview"),

    /**
     * 话务条播放录音
     */
    PLAY_RECORD("play-record"),

    /**
     * 外呼并桥接客户
     */
    CALL_BRIDGE_CLIENT("call-bridge-client"),

    /**
     * 咨询、转接、三方通话、耳语、监听 xfer
     * 【consult:咨询，trans:转接，three_way:三方通话，whisper:耳语，eavesdrop:监听】
     */
    XFER("xfer"),

    /**
     * 管理员代接
     */
    SUBSTITUTE("substitute"),

    /**
     * 无操作
     */
    NONE("none");

    private final String name;

    OriginateAfterActionEnum(String name) {
        this.name = name;
    }
}
