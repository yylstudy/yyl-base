package com.cqt.base.enums.ext;

/**
 * @author linshiqiang
 * date:  2023-07-04 13:46
 * 分机状态迁移动作枚举
 */
public enum ExtStatusTransferActionEnum {

    /**
     * 分机注册
     * 离线 -> 在线
     */
    REGISTER,

    /**
     * 分机注销
     * 任意状态 -> 离线
     */
    LOGOUT,

    /**
     * 外呼事件
     * 在线 -> 外呼中
     */
    INVITE,

    /**
     * 振铃事件
     * 外呼中 -> 振铃中
     */
    RING,

    /**
     * 接通事件
     * 振铃中 -> 接通中
     */
    ANSWER,

    /**
     * 桥接事件
     * 接通中 -> 通话中
     */
    BRIDGE,

    /**
     * 挂断事件
     * 通话中 -> 在线
     */
    HANGUP,

    /**
     * 复位分机
     */
    RESET
}
