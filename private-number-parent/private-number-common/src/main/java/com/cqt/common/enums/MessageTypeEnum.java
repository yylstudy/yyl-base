package com.cqt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author linshiqiang
 * @date 2022/5/12 16:42
 */
@Getter
@AllArgsConstructor
public enum MessageTypeEnum {

    /**
     * 短信
     */
    sms,

    /**
     * 邮箱
     */
    email,

    /**
     * 钉钉
     */
    dingding
}
