package com.cqt.base.enums;

import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-07-21 9:36
 * 呼入策略枚举
 * 在呼入事件使用
 */
public enum CallInStrategyEnum implements BaseEnum<CallInStrategyEnum> {

    IVR(1, "转IVR"),

    SKILL(2, "转技能(人工服务)"),

    AGENT(3, "转坐席"),

    OUTLINE(4, "转外部"),

    PLAY_MEDIA(5, "播放语音");

    private final Integer code;

    private final String name;

    CallInStrategyEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static Optional<CallInStrategyEnum> of(Integer code) {
        return Optional.ofNullable(BaseEnum.parseByCode(CallInStrategyEnum.class, code));
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
