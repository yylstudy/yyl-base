package com.cqt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 话单结束原因枚举定义
 *
 * @author scott
 * @date 2022年08月10日 10:35
 */
@Getter
@AllArgsConstructor
public enum ReleaseCauseEnum {

    /**
     * 1 - 正常接通
     */
    NORMAL(1, "正常接通"),

    /**
     * 2 - 呼叫遇忙
     */
    CALL_BUSY(2, "呼叫遇忙"),

    /**
     * 3 - 用户不在服务区
     */
    OUT_OF_SERVICE_AREA(3, "用户不在服务区"),

    /**
     * 4 - 用户无应答
     * 空号识别没有到，根据结束时间-振铃时间>50s 判断为无应答
     */
    NO_ANSWER(4, "用户无应答"),

    /**
     * 5 - 用户关机
     */
    USER_SHUTDOWN(5, "用户关机"),

    /**
     * 6 - 空号
     */
    EMPTY_NUMBER(6, "空号"),

    /**
     * 7 - 停机
     */
    STOP_SERVICE(7, "停机"),

    /**
     * 8 - 号码过期
     */
    NUMBER_EXPIRED(8, "号码过期"),

    /**
     * 9 - 主叫应答，被叫应答前挂机(振铃后挂机)  有振铃时间
     */
    CALL_CALLED_HANG_UP(9, "主叫应答，被叫应答前挂机(振铃后挂机)  有振铃时间"),

    /**
     * 91 - 主叫应答，被叫应答前挂机(振铃前挂机) 无振铃时间
     */
    CALL_CALLED_HANG_UP_BEFORE_RINGING(91, "主叫应答，被叫应答前挂机(振铃前挂机) 无振铃时间"),

    /**
     * 10 - 正在通话中
     */
    ON_CALL(10, "正在通话中"),

    /**
     * 11 - 拒接
     * 1).空号识别为(呼叫遇忙或者正在通话中，根据结束时间-开始时间>16s,判断为拒接
     * 2).空号识别没识别到，22<结束时间-振铃时间<50,判断为拒接
     */
    REJECT(11, "拒接"),

    /**
     * 12 - 请不要挂机
     */
    PLEASE_HOLD_THE_LINE(12, "请不要挂机"),

    /**
     * 99 - 其他
     */
    OTHER(99, "其他"),

    /**
     * 20 - 主叫取消呼叫
     */
    CALLER_CANCEL_CALL(20, "主叫取消呼叫"),

    ;
    private final Integer code;
    private final String text;

    /**
     * 枚举缓存
     */
    private static final Map<Integer, String> ENUM_MAPPING;

    static {
        ENUM_MAPPING = new HashMap<>();
        for (ReleaseCauseEnum item : ReleaseCauseEnum.values()) {
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
