package com.cqt.base.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-08-21 9:23
 * 呼入IVR 之后的操作
 */
@Getter
public enum CallInIvrActionEnum {

    IVR_QUEUE_UP(1, "queue", "呼入IVR-排队"),

    IVR_TRANS_AGENT(2, "agent", "呼入IVR-转接坐席"),

    IVR_TRANS_OUTLINE(3, "outline", "呼入IVR-转接外线"),

    IVR_MESSAGE(4, "message", "呼入IVR-留言"),

    TRANS_SKILl(5, "skill", "转技能");

    private final Integer code;

    private final String name;

    private final String desc;

    CallInIvrActionEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }
}
