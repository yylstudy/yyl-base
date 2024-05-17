package com.cqt.common.enums;

/**
 * freeswich通话事件
 *
 * @author hlx
 * @date 2022-01-04
 */
public enum CallEventEnum {
    /**
     * 呼入平台
     */
    callin,
    /**
     * 找到绑定关系呼出
     */
    callout,
    /**
     * 振铃
     */
    ringing,
    /**
     * 接通
     */
    answer,
    /**
     * 挂断
     */
    hangup
}
