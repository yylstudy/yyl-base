package com.cqt.base.enums.cdr;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author linshiqiang
 * date:  2023-09-12 13:44
 * <p>
 * ID  状态        别名                描述
 * 2    关机        power off            关机
 * 3    空号        does not exist        空号
 * 4    停机        out of service        停机
 * 5    正在通话中    hold on                正在通话中
 * 6    用户拒接    not convenient        用户拒接
 * 7    无法接通    is not reachable    无法接通
 * 8    暂停服务    not in service        暂停服务
 * 9    用户正忙    busy now            用户正忙
 * 10    拨号方式不正确    not a local number    拨号方式不正确
 * 11    呼入限制    barring of incoming    呼入限制
 * 12    来电提醒    call reminder        各类秘书服务
 * 13    呼叫转移失败    forwarded        呼叫转移失败
 * 14    网络忙        line is busy        网络忙
 * 15    无人接听    not answer            无人接听
 * 16    欠费        defaulting            欠费
 * 17    无法接听    cannot be connected    无法接听
 * 18    改号        number change        改号
 * 19    线路故障    line fault            线路不能呼出，比如SIM卡欠费
 * 20    稍后再拨    redial later        各种稍后再拨提示
 * </p>
 */
@Getter
public enum Da2ResultEnum {

    NORMAL(0, "normal", "正常"),

    CALLER_RELEASE(1, "caller release", "主叫主动挂断"),

    POWER_OFF(2, "power off", "关机"),

    DOES_NOT_EXIST(3, "does not exist", "空号"),

    OUT_OF_SERVICE(4, "out of service", "停机"),

    HOLD_ON(5, "hold on", "正在通话中"),

    NOT_CONVENIENT(6, "not convenient", "用户拒接"),

    IS_NOT_REACHABLE(7, "is not reachable", "无法接通"),

    NOT_IN_SERVICE(8, "not in service", "暂停服务"),

    BUSY_NOW(9, "busy now", "用户正忙"),

    NOT_A_LOCAL_NUMBER(10, "not a local number", "拨号方式不正确"),

    BARRING_OF_INCOMING(11, "barring of incoming", "呼入限制"),

    CALL_REMINDER(12, "call reminder", "来电提醒(各类秘书服务)"),

    LINE_IS_BUSY(13, "line is busy", "网络忙"),

    NOT_ANSWER(15, "not answer", "无人接听"),

    DEFAULTING(16, "defaulting", "欠费"),

    CANNOT_BE_CONNECTED(17, "cannot be connected", "无法接听"),

    NUMBER_CHANGE(18, "number change", "改号"),

    LINE_FAULT(19, "line fault", "线路故障(线路不能呼出，比如SIM卡欠费)"),

    REDIAL_LATER(20, "redial later", "稍后再拨(各种稍后再拨提示)");

    private static final Map<String, Integer> CACHE = new HashMap<>(16);

    static {
        Da2ResultEnum[] resultEnums = Da2ResultEnum.class.getEnumConstants();
        for (Da2ResultEnum resultEnum : resultEnums) {
            CACHE.put(resultEnum.getName(), resultEnum.getCode());
        }

    }

    private final Integer code;

    private final String name;

    private final String desc;

    Da2ResultEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static Integer getResultCode(String name) {
        return CACHE.get(name);
    }
}
