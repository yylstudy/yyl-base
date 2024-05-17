package com.cqt.model.freeswitch.dto.event;

import com.cqt.base.enums.MediaStreamEnum;
import com.cqt.model.freeswitch.base.FreeswitchEventBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:09
 * 呼入事件
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CallInEventDTO extends FreeswitchEventBase implements Serializable {

    private EventData data;

    @JsonIgnore
    public Integer getAudioCode() {
        return MediaStreamEnum.valueOf(this.data.getAudio()).getCode();
    }

    @JsonIgnore
    public Integer getVideoCode() {
        return MediaStreamEnum.valueOf(this.data.getVideo()).getCode();
    }

    @Data
    public static class EventData implements Serializable {

        private static final long serialVersionUID = 2455586361970444945L;

        /**
         * 【NONE:无语音流， SENDONLY:只发送语音流不接收， RECVONLY:只接收语音流不发送， SENDRECV:发送并接收语音流】
         */
        @JsonProperty("audio")
        private String audio;

        /**
         * 【NONE:无视频流， SENDONLY:只发送视频流不接收， RECVONLY:只接收视频流不发送， SENDRECV:发送并接收视频流】
         */
        @JsonProperty("video")
        private String video;

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

    }
}
