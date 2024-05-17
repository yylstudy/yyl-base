package com.cqt.base.enums.calltask;

import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-10-27 10:20
 * 外呼任务状态枚举
 */
@Getter
public enum CallTaskStatusEnum {

    DRAFT(1, "草稿"),

    PAUSE(2, "暂停"),

    ENABLE(3, "启用"),

    END(4, "已结束");

    private final Integer code;

    private final String name;

    CallTaskStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
