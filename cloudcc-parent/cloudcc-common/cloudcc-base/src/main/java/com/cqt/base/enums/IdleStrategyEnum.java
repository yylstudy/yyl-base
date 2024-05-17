package com.cqt.base.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-10-10 9:31
 * 闲时策略枚举
 * <p>
 * 1-当前空闲持续时长最长的优先、
 * <p>
 * 2-当天通话时间最少的优先、
 * <p>
 * 3-当天通话次数最少的优先、
 * <p>
 * 4-随机分配、
 * <p>
 * 5-坐席权值+当前空闲持续时长最长的优先、
 * <p>
 * 6-坐席权值+当天通话时间最少的优先、
 * <p>
 * 7-坐席权值+当天通话次数最少的优先，
 */
@Getter
public enum IdleStrategyEnum implements BaseEnum<IdleStrategyEnum> {

    MAX_FREE_TIME(1, "当前空闲持续时长最长的优先"),

    TODAY_LEAST_CALL_TIME(2, "当天通话时间最少的优先"),

    TODAY_LEAST_CALL_COUNT(3, "当天通话次数最少的优先"),

    RANDOM(4, "随机分配"),

    AGENT_WEIGHT_AND_MAX_FREE_TIME(5, "坐席权值+当前空闲持续时长最长的优先"),

    AGENT_WEIGHT_AND_TODAY_LEAST_CALL_TIME(6, "坐席权值+当天通话时间最少的优先"),

    AGENT_WEIGHT_AND_TODAY_LEAST_CALL_COUNT(7, "坐席权值+当天通话次数最少的优先");

    private final Integer code;

    private final String name;

    IdleStrategyEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static IdleStrategyEnum parse(Integer code) {
        return BaseEnum.parseByCode(IdleStrategyEnum.class, code);
    }
}
