package com.cqt.base.enums;

/**
 * @author linshiqiang
 * date:  2023-08-02 13:50
 * 发送媒体流枚举
 * 语音流
 * 视频流
 */
public enum MediaStreamEnum implements BaseEnum<MediaStreamEnum> {

    INACTIVE(0, "NONE", "无语音/视频流"),

    NONE(0, "NONE", "无语音/视频流"),

    SENDONLY(1, "SENDONLY", "只发送语音/视频流不接收"),

    RECVONLY(2, "RECVONLY", "只接收语音/视频流不发送"),

    SENDRECV(3, "SENDRECV", "发送并接收语音/视频流");

    private final Integer code;

    private final String name;

    private final String desc;

    MediaStreamEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
