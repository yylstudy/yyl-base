package com.cqt.common.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * @date 2021/4/19 16:24
 * 号码池类型
 */
@Getter
public enum AxbPoolTypeEnum {

    /**
     * 全部号码池
     */
    ALL,

    /**
     * 主池
     */
    MASTER,

    /**
     * 备池
     */
    SLAVE,

    ;

}
