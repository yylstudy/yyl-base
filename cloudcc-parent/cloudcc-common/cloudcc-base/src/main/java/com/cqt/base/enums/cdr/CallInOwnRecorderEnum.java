package com.cqt.base.enums.cdr;

import com.cqt.base.enums.BaseEnum;
import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-11-08 10:00
 * 呼入A路录制节点
 */
@Getter
public enum CallInOwnRecorderEnum implements BaseEnum<CallInOwnRecorderEnum> {

    NONE(0, "不录制"),

    CALL_IN(1, "呼入平台开始录制");

    private final Integer code;

    private final String name;

    CallInOwnRecorderEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static CallInOwnRecorderEnum parse(Integer code) {
        return BaseEnum.parseByCode(CallInOwnRecorderEnum.class, code);
    }
}
