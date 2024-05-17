package com.cqt.base.enums.cdr;

import com.cqt.base.enums.BaseEnum;
import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-11-08 10:00
 * 呼出B路录制节点
 */
@Getter
public enum CalloutPeerRecorderEnum implements BaseEnum<CalloutPeerRecorderEnum> {

    NONE(0, "不录制"),

    RING(1, "对方/客户振铃开始录制"),

    ANSWER(2, "对方/客户接通开始录制");

    private final Integer code;

    private final String name;

    CalloutPeerRecorderEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static CalloutPeerRecorderEnum parse(Integer code) {
        return BaseEnum.parseByCode(CalloutPeerRecorderEnum.class, code);
    }
}
