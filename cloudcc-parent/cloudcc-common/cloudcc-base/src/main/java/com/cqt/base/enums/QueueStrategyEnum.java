package com.cqt.base.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-10-10 9:31
 * 排队策略(字典配置): (1 TIME-排队时长,  2 COMBINE-组合策略)
 */
@Getter
public enum QueueStrategyEnum {

    TIME(1, "排队时长"),

    COMBINE(2, "组合策略");

    private final Integer code;

    private final String name;

    QueueStrategyEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
