package com.cqt.base.enums;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:43
 * 通话状态事件
 */
public enum CallStatusEventEnum {

    /**
     * 外呼
     */
    INVITE,

    /**
     * 媒体建立
     */
    MEDIA,

    /**
     * 振铃
     */
    RING,

    /**
     * 接通
     */
    ANSWER,

    /**
     * 桥接
     */
    BRIDGE,

    /**
     * 挂断
     */
    HANGUP,

    /**
     * 音视频切换
     */
    MEDIARENEG
}
