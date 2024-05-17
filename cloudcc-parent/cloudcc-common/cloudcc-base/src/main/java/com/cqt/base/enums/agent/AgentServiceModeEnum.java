package com.cqt.base.enums.agent;

import com.cqt.base.enums.BaseEnum;
import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:43
 * 坐席模式
 * 客服型
 * 外呼型
 */
@Getter
public enum AgentServiceModeEnum implements BaseEnum<AgentServiceModeEnum> {

    /**
     * 客服型
     */
    CUSTOMER(1, "客服型"),

    /**
     * 外呼型
     */
    OUTBOUND(2, "外呼型");

    private final Integer code;

    private final String name;

    AgentServiceModeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static AgentServiceModeEnum parse(Integer code) {
        return BaseEnum.parseByCode(AgentServiceModeEnum.class, code);
    }
}
