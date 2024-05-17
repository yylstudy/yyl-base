package com.cqt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 位置更新状态码
 *
 * @author Xienx
 * @date 2020年05月24日 13:55
 */
@Getter
@AllArgsConstructor
public enum MapLocationUpdatingStatusEnum {

    /**
     * 位置更新状态码 <p/>
     * 0 - success
     */
    SUCCESS(0, "success", "更新成功"),

    /**
     * 位置更新状态码 <p/>
     * 3 - Stop Service - 暂停服务
     */
    STOP_SERVICE(3, "Stop Service", "暂停服务"),

    /**
     * 位置更新状态码 <p/>
     * 6 - IMSI ERROR - Imsi错误
     */
    IMSI_ERROR(6, "IMSI ERROR", "Imsi错误"),

    /**
     * 位置更新状态码 <p/>
     * 8 - Outgoing Call Barred - 去话暂停呼叫
     */
    OUTGOING_CALL_BARRED(8, "Outgoing Call Barred", "去话暂停呼叫"),

    /**
     * 位置更新状态码 <p/>
     * 9 - Incoming Call Barred - 禁止接听来电
     */
    INCOMING_CALL_BARRED(9, "Incoming Call Barred", "禁止接听来电"),

    /**
     * 位置更新状态码 <p/>
     * 20 - unknown subscriber - 未知用户
     */
    UNKNOWN_SUBSCRIBER(20, "unknown subscriber", "未知用户"),

    /**
     * 位置更新状态码 <p/>
     * 23 - roaming not allowed - 不允许漫游
     */
    ROAMING_NOT_ALLOWED(23, "roaming not allowed", "不允许漫游"),

    /**
     * 位置更新状态码 <p/>
     * 30 - X number and IMSI not match - msidn与发送的不符
     */
    X_NUMBER_AND_IMSI_NOT_MATCH(30, "X number and IMSI not match", "msidn与发送的不符"),

    /**
     * 位置更新状态码 <p/>
     * 36 - unexpectedDataValue - 异常数据
     */
    UNEXPECTED_DATA_VALUE(36, "unexpectedDataValue", "异常数据"),

    /**
     * 位置更新状态码 <p/>
     * 255 -  - 未知异常
     */
    UNKNOWN_EXCEPTION(255, "", "未知异常");

    /**
     * 位置更新状态码
     */
    private final Integer code;

    /**
     * 位置更新失败原因
     */
    private final String reason;

    /**
     * 位置更新状态描述
     */
    private final String description;

    /**
     * 枚举缓存
     */
    private static final Map<Integer, String> ENUM_MAPPING;

    static {
        ENUM_MAPPING = new HashMap<>();
        for (MapLocationUpdatingStatusEnum item : MapLocationUpdatingStatusEnum.values()) {
            if (ENUM_MAPPING.put(item.getCode(), item.getReason()) != null) {
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
