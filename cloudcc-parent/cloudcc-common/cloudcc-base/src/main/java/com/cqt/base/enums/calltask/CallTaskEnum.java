package com.cqt.base.enums.calltask;

import com.cqt.base.enums.BaseEnum;
import lombok.Getter;

import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:31
 * 外呼任务枚举
 */
@Getter
public enum CallTaskEnum implements BaseEnum<CallTaskEnum> {

    /**
     * IVR流程
     */
    IVR(1, "IVR"),

    /**
     * 语音通知
     */
    VOICE_NOTICE(2, "语音通知"),

    /**
     * 预测
     */
    PREDICT_TASK(3, "预测外呼"),

    /**
     * 预览
     */
    PREVIEW(4, "预览外呼");

    private final Integer code;

    private final String name;

    CallTaskEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static Optional<CallTaskEnum> of(Integer code) {
        return Optional.ofNullable(BaseEnum.parseByCode(CallTaskEnum.class, code));
    }

    public static CallTaskEnum parse(Integer code) {
        return BaseEnum.parseByCode(CallTaskEnum.class, code);
    }
}
