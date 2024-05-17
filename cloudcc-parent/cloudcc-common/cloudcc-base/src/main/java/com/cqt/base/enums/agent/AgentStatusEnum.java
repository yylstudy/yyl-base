package com.cqt.base.enums.agent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:43
 * 坐席状态事件
 */
public enum AgentStatusEnum {

    /**
     * 离线
     */
    OFFLINE("离线"),

    /**
     * 空闲
     */
    FREE("空闲"),

    /**
     * 忙碌
     */
    BUSY("忙碌"),

    /**
     * 小休
     */
    REST("小休"),

    /**
     * 振铃
     */
    RINGING("振铃"),

    /**
     * 通话中
     */
    CALLING("通话中"),

    /**
     * 事后处理
     */
    ARRANGE("事后处理");

    private static final Set<String> SET = new HashSet<>();

    static {
        AgentStatusEnum[] values = AgentStatusEnum.values();
        for (AgentStatusEnum value : values) {
            SET.add(value.name());
        }
    }

    private final String desc;

    AgentStatusEnum(String desc) {
        this.desc = desc;
    }

    public static boolean checkName(String name) {
        return SET.contains(name);
    }

    public String getDesc() {
        return desc;
    }
}
