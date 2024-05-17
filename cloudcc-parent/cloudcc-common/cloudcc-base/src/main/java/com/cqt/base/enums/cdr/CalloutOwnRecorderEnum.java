package com.cqt.base.enums.cdr;

import com.cqt.base.enums.BaseEnum;
import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-11-08 10:00
 * 呼出A路录制节点
 */
@Getter
public enum CalloutOwnRecorderEnum implements BaseEnum<CalloutOwnRecorderEnum> {

    NONE(0, "不录制"),

    AGENT_INVITE(1, "坐席发起外呼开始录制"),

    AGENT_RING(2, "坐席振铃开始录制"),

    AGENT_ANSWER(3, "坐席接通开始录制"),

    CLIENT_RING(4, "对方/客户振铃开始录制"),

    CLIENT_ANSWER(5, "对方/客户接通开始录制");

    private final Integer code;

    private final String name;

    CalloutOwnRecorderEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static CalloutOwnRecorderEnum parse(Integer code) {
        return BaseEnum.parseByCode(CalloutOwnRecorderEnum.class, code);
    }
}
