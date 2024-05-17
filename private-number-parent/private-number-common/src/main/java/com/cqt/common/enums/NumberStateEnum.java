package com.cqt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 号码状态枚举定义
 *
 * @author Xienx
 * @date 2022年05月17日 14:02
 */
@Getter
@AllArgsConstructor
public enum NumberStateEnum {
    /**
     * 号码状态 - 0 正常
     */
    NORMAL(0, "正常"),
    /**
     * 号码状态 - 1 下线
     */
    OFFLINE(1, "下线"),
    ;

    private final Integer code;
    private final String text;

    /**
     * 枚举缓存
     */
    private static final Map<Integer, String> ENUM_MAPPING;

    static {
        ENUM_MAPPING = new HashMap<>();
        for (NumberStateEnum item : NumberStateEnum.values()) {
            if (ENUM_MAPPING.put(item.getCode(), item.getText()) != null) {
                throw new IllegalArgumentException("duplicate code:" + item.getCode());
            }
        }
    }

    /**
     * 根据code获取枚举值
     *
     * @param code 枚举code
     * @return 枚举描述
     */
    public static String getByCode(Integer code) {
        return ENUM_MAPPING.get(code);
    }
}
