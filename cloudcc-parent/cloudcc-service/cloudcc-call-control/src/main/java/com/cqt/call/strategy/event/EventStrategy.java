package com.cqt.call.strategy.event;

import com.cqt.base.enums.EventEnum;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:46
 * 事件策略
 */
public interface EventStrategy {

    /**
     * 获取事件类型枚举
     *
     * @return 事件类型枚举
     */
    EventEnum getEventType();

    /**
     * 处理事件
     *
     * @param message 事件消息
     * @throws Exception 异常
     */
    void deal(String message) throws Exception;

}
