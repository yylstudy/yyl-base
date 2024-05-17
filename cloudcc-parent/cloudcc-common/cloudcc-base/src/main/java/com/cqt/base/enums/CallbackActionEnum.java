package com.cqt.base.enums;

/**
 * @author linshiqiang
 * @since 2022/1/24 17:25
 * 结果回调通知
 */
public enum CallbackActionEnum {

    /**
     * ivr执行
     */
    IVR,

    /**
     * 话务条播放录音
     */
    PLAY_RECORD,

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
     * 转接
     */
    TRANS,

    /**
     * 咨询中转接
     */
    CONSULT_TO_TRANS,

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
     * 代接
     */
    SUBSTITUTE,

    /**
     * 强插(三方)
     */
    FORCE_CALL,

    /**
     * 强拆
     */
    INTERRUPT_CALL,

    /**
     * 音视频切换
     */
    CHANGE_MEDIA,

    /**
     * 分机注销
     */
    EXT_UN_REG
}
