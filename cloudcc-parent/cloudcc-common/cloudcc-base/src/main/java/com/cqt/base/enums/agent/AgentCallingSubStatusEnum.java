package com.cqt.base.enums.agent;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:43
 * 坐席通话中子状态事件
 */
public enum AgentCallingSubStatusEnum {

    /**
     * 监听中
     */
    EAVESDROP,

    /**
     * 耳语中
     */
    WHISPER,

    /**
     * 转接中
     */
    TRANS,

    /**
     * 三方通话中
     */
    THREE_WAY,

    /**
     * 强插
     */
    FORCE_CALL,

    /**
     * 咨询中
     */
    CONSULT,

    /**
     * 保持中
     */
    HOLD,

    /**
     * 整理
     */
    PLAY_RECORD,

}
