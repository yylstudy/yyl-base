package com.cqt.model.freeswitch.dto.event;

import com.cqt.model.freeswitch.base.FreeswitchEventBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:16
 * 放音状态事件
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PlayStatusEventDTO extends FreeswitchEventBase implements Serializable {

    private static final long serialVersionUID = 6550039884903937091L;

    private EventData data;

    @Data
    public static class EventData implements Serializable {

        private static final long serialVersionUID = -2631284033453962919L;

        @JsonProperty("req_id")
        private String reqId;

        /**
         * 被叫号码
         */
        @JsonProperty("callee_number")
        private String calleeNumber;

        /**
         * 主叫号码
         */
        @JsonProperty("caller_number")
        private String callerNumber;

        /**
         * 【start:放音开始，end:放音结束】
         */
        private String status;

    }
}
