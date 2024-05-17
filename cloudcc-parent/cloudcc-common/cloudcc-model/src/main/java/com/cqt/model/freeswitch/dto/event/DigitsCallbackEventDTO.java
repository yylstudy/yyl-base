package com.cqt.model.freeswitch.dto.event;

import com.cqt.model.freeswitch.base.FreeswitchEventBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:09
 * 放音收号事件
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DigitsCallbackEventDTO extends FreeswitchEventBase implements Serializable {

    private EventData data;

    @Data
    public static class EventData implements Serializable {

        private static final long serialVersionUID = 2455586361970444945L;

        /**
         * 请求id
         */
        @JsonProperty("req_id")
        private String reqId;

        /**
         * 按键结果
         */
        private String content;

        /**
         * 主叫号码
         */
        @JsonProperty("caller_number")
        private String callerNumber;

        /**
         * 被叫号码
         */
        @JsonProperty("callee_number")
        private String calleeNumber;

        /**
         * 收号结束原因值(注意处理好，在收号时，电话挂断)
         * 【NONE TIMEOUT:收号超时，TERM_KEY:结束键结束】
         */
        @JsonProperty("end_reason")
        private String endReason;
    }
}
