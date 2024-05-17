package com.cqt.base.enums.ext;

/**
 * @author linshiqiang
 * date:  2023-07-04 13:45
 * 分机状态枚举
 */
public enum ExtStatusEnum {

    /**
     * 离线
     */
    OFFLINE("离线"),

    /**
     * 在线
     */
    ONLINE("在线"),

    /**
     * 外呼中
     */
    INVITING("外呼中"),

    /**
     * 振铃中
     */
    RINGING("振铃中"),

    /**
     * 接通中
     */
    CONNECTING("接通中"),

    /**
     * 通话中
     */
    CALLING("通话中");

    private final String desc;

    ExtStatusEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
