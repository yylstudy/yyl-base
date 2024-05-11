package com.linkcircle.mq.common;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/1/12 14:34
 */

public enum RocketmqDelayLevel {
    ONE(1,"1s"),
    TWO(2,"5s"),
    THREE(3,"10s"),
    FOUR(4,"30s"),
    FIVE(5,"1m"),
    SIX(6,"2m"),
    SEVEN(7,"3m"),
    EIGHT(8,"4m"),
    NINE(9,"5m"),
    TEN(10,"6m"),
    ELEVEN(11,"7m"),
    TWELVE(12,"8m"),
    THIRTEEN(13,"9m"),
    FOURTEEN(14,"10m"),
    FIFTEEN(15,"20m"),
    SIXTEEN(16,"30m"),
    SEVENTEEN(17,"1h"),
    EIGHTEEN (18,"2h");
    private final int code;
    private final String desc;

    RocketmqDelayLevel(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
