package com.cqt.base.enums;

/**
 * @author linshiqiang
 * date:  2023-08-05 15:00
 */
public enum CallInChannelEnum {

    /**
     * 客户呼入
     */
    CALL_IN,

    /**
     * 外呼
     */
    CALL,

    /**
     * 预览外呼
     *
     * @since 7.0.0
     */
    PREVIEW_OUT_CALL,

    /**
     * 预测外呼
     *
     * @since 7.0.0
     */
    PREDICT_OUT_CALL,

    /**
     * 咨询
     */
    CONSULT,

    /**
     * 监听
     */
    EAVESDROP,

    /**
     * 耳语
     */
    WHISPER,

    /**
     * 三方通话
     */
    THREE_WAY,

    /**
     * 转接
     */
    TRANS,

    /**
     * 咨询中转接
     */
    CONSULT_TO_TRANS,

    /**
     * 代接
     */
    SUBSTITUTE
}
