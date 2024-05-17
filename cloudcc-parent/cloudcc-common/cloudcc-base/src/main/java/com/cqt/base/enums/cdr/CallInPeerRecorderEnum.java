package com.cqt.base.enums.cdr;

import com.cqt.base.enums.BaseEnum;
import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-11-08 10:00
 * 呼入B路录制节点
 */
@Getter
public enum CallInPeerRecorderEnum implements BaseEnum<CallInPeerRecorderEnum> {

    NONE(0, "不录制"),

    AGENT_RING(2, "坐席振铃开始录制"),

    AGENT_ANSWER(3, "坐席接通开始录制");

    private final Integer code;

    private final String name;

    CallInPeerRecorderEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static CallInPeerRecorderEnum parse(Integer code) {
        return BaseEnum.parseByCode(CallInPeerRecorderEnum.class, code);
    }
}
