package com.cqt.base.enums;

/**
 * @author linshiqiang
 * date:  2023-07-07 14:04
 * xfer操作枚举
 * 【consult:咨询，trans:转接，three_way:三方通话，whisper:耳语，eavesdrop:监听】
 */
public enum XferActionEnum {

    /**
     * 咨询
     */
    CONSULT("consult", "咨询"),

    /**
     * 转接
     */
    TRANS("trans", "转接"),

    /**
     * 三方通话
     */
    THREE_WAY("three_way", "三方通话"),

    /**
     * 强插
     */
    FORCE_CALL("three_way", "强插三方通话"),

    /**
     * 耳语
     */
    WHISPER("whisper", "耳语"),

    /**
     * 监听
     */
    EAVESDROP("eavesdrop", "监听");

    private final String name;

    private final String desc;

    XferActionEnum(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
