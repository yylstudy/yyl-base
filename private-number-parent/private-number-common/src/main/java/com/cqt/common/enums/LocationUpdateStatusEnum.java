package com.cqt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 位置更新状态枚举
 *
 * @author Xienx
 * @date 2022年05月17日 14:02
 */
@Getter
@AllArgsConstructor
public enum LocationUpdateStatusEnum {

    /**
     * 目前已不再使用
     * 位置更新状态 0 - 待更新
     */
    @Deprecated
    WAIT_UPDATE(0, "待更新"),

    /**
     * 位置更新状态 1 - 更新成功
     */
    SUCCESS(1, "更新成功"),

    /**
     * 位置更新状态 2 - 更新中
     */
    UPDATING(2, "更新中"),

    /**
     * 位置更新状态 3 - 临时下线
     */
    TEMPORARY_OFFLINE(3, "临时下线"),

    /**
     * 位置更新状态 4 - 被抢占
     */
    PREEMPTED(4, "被抢占"),

    /**
     * 位置更新状态 5 - 更新失败
     */
    FAILED(5, "更新失败"),

    /**
     * 位置更新状态 6 - 下线
     */
    OFFLINE(6, "下线"),

    ;

    private final Integer code;
    private final String text;

    /**
     * 枚举缓存
     */
    private static final Map<Integer, String> ENUM_MAPPING;

    static {
        ENUM_MAPPING = new HashMap<>();
        for (LocationUpdateStatusEnum item : LocationUpdateStatusEnum.values()) {
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
