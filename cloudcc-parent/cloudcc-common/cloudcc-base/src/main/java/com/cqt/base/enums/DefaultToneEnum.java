package com.cqt.base.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-07-21 9:36
 * 默认音
 */
@Getter
public enum DefaultToneEnum {

    MESSAGE("leaveWordTone", "留言提示音"),

    CALL_IN_AGENT_ANSWER_PLAYBACK("inboundAnswerTone", "坐席接收来电应答的提示音"),

    CALL_OUT_PEER_ANSWER_PLAYBACK("outBoundAnswerTone", "坐席呼出对端应答后的提示音");

    private final String code;

    private final String name;

    DefaultToneEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
