package com.cqt.common.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * @date 2021/4/19 16:24
 * ack类型
 */
@Getter
public enum AckActionEnum {

    /**
     *  处理成功
     */
    ACCEPT,

    /**
     * 可以重试的错误
     */
    RETRY,

    /**
     * 无需重试的错误
     */
    REJECT,

    ;

}
