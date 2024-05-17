package com.cqt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 定时拨测任务枚举
 *
 * @author Xienx
 * @date 2023年02月23日 14:57
 */
@Getter
@AllArgsConstructor
public enum DialTestTypeEnum {
    /**
     * 1 - 定时隐私号拨测
     */
    DIAL_TEST(1, "定时隐私号拨测"),

    /**
     * 2 - 定时位置更新
     */
    LOCATION(2, "定时位置更新");

    private final Integer code;
    private final String text;
}
