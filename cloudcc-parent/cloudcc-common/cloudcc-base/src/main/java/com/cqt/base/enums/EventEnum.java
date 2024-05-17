package com.cqt.base.enums;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:31
 * 底层事件类型枚举
 */
public enum EventEnum {

    /**
     * 呼入
     */
    call_in,

    /**
     * 通话状态
     */
    call_status,

    /**
     * 按键收号
     */
    dtmf,

    /**
     * 放音收号事件
     */
    digits_callback,

    /**
     * 放音状态
     */
    play_status,

    /**
     * 分机状态
     */
    ext_status
}
