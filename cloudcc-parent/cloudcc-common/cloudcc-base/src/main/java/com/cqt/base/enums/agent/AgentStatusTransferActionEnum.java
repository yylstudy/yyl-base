package com.cqt.base.enums.agent;

/**
 * @author linshiqiang
 * date:  2023-07-04 13:46
 * 坐席状态迁移动作枚举
 */
public enum AgentStatusTransferActionEnum {

    /**
     * 签入
     * 离线 -> 在线
     */
    CHECKIN,

    /**
     * 签出
     * 在线 -> 离线
     */
    CHECKOUT,

    /**
     * 外呼
     * 空闲 -> 通话中
     * <p>
     * 1. 外呼或被呼入
     */
    CALLOUT,

    ANSWER,

    BRIDGE,

    HANGUP,

    /**
     * 保持
     */
    HOLD,

    /**
     * 取消保持
     */
    UN_HOLD,

    /**
     * 呼入
     * 空闲 -> 通话中
     * <p>
     * 1. 外呼或被呼入
     */
    CALLIN,

    /**
     * 进入事后处理
     * 通话中 -> 事后处理
     * <p>
     * 1. 外呼或被呼入配置开启事后处理
     */
    ARRANGE,

    /**
     * 呼入 进入振铃
     */
    RING,

    /**
     * 示闲
     * 离线 -> 空闲
     * 忙碌 -> 空闲
     * <p>
     * 1. 坐席签入 坐席设置空闲(根据坐席扩展属性)
     * 2. 页面点击空闲
     */
    MAKE_FREE,

    /**
     * 示忙
     * 离线 -> 忙碌
     * 通话中 -> 忙碌
     * 事后处理 -> 忙碌
     * 振铃 -> 忙碌
     * <p>
     * 1. 坐席签入 坐席设置忙碌(根据坐席扩展属性)
     * 2. 页面点击忙碌
     * 3. 通话中设置忙碌，通话后变为忙碌
     * 4. 事后处理中被设置忙碌超时后转忙碌
     * 5. 开启应答失败自动示忙
     */
    MAKE_BUSY,

    /**
     * 进入小休
     */
    MAKE_REST,

    /**
     * 进入事后处理
     */
    MAKE_ARRANGE,

    /**
     * 恢复通话前的状态
     */
    RECOVER,

    /**
     * 通话结束后自动示忙
     */
    MAKE_BUSY_AFTER_CALL_STOP,

    /**
     * 通话结束后自动示闲
     */
    MAKE_FREE_AFTER_CALL_STOP,

    /**
     * 通话结束后自动进入小休
     */
    MAKE_REST_AFTER_CALL_STOP,

    /**
     * 通话结束后自动进入签出
     */
    CHECKOUT_AFTER_CALL_STOP,

    /**
     * 强复位
     */
    FORCE_RESET,

    /**
     * 强签
     * 任意状态 -> 离线
     */
    FORCE_CHECKOUT,

    /**
     * 强制示闲
     */
    FORCE_MAKE_FREE,

    /**
     * 强制示忙
     */
    FORCE_MAKE_BUSY

}
