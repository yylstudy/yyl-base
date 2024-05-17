package com.cqt.push.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author huweizhong
 * date  2023/1/9 17:45
 */
@Getter
@AllArgsConstructor
public enum UrlTypeEnum {

    /**
     * 话单推送
     */
    BILL,

    /**
     * 状态推送
     */
    STATUS,

    /**
     * AXEYB-AYB绑定推送
     */
    AYB,

    /**
     * 解绑事件推送
     */
    UNBIND,
}
