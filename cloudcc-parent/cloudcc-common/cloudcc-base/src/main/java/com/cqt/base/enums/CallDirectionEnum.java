package com.cqt.base.enums;

/**
 * @author linshiqiang
 * date:  2023-07-13 13:56
 * 呼叫方向
 */
public enum CallDirectionEnum implements BaseEnum<CallDirectionEnum> {

    ALL(2, "呼出+呼入"),

    /**
     * 客户呼入
     */
    INBOUND(1, "呼入"),

    /**
     * 坐席外呼
     */
    OUTBOUND(0, "呼出");

    private final Integer code;

    private final String name;

    CallDirectionEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
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
