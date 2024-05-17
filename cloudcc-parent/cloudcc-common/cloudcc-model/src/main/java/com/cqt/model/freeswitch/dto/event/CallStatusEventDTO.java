package com.cqt.model.freeswitch.dto.event;

import com.cqt.model.freeswitch.base.FreeswitchEventBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:12
 * 呼叫状态事件
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CallStatusEventDTO extends FreeswitchEventBase implements Serializable {

    private static final long serialVersionUID = 6369958003892654498L;

    private EventData data;

    @JsonProperty("channel_data")
    private Map<String, Object> channelData;

    @Data
    public static class EventData implements Serializable {

        private static final long serialVersionUID = 2455586361970444945L;

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
         * 断原因值
         */
        @JsonProperty("hangup_cause")
        private String hangupCause;

        /**
         * 【INACTIVE:无语音流，
         * SENDONLY:只发送语音流不接收，
         * RECVONLY:只接收语音流不发送，
         * SENDRECV:发送并接收语音流】
         * 接通，音视频切换时携带
         */
        @JsonProperty("audio")
        private String audio;

        /**
         * 【INACTIVE:无语音流，
         * SENDONLY:只发送语音流不接收，
         * RECVONLY:只接收语音流不发送，
         * SENDRECV:发送并接收语音流】
         * 上一次的状态，接通，音视频切换时携带
         */
        @JsonProperty("last_audio")
        private String lastAudio;

        /**
         * 【INACTIVE:无视频流，
         * SENDONLY:只发送视频流不接收，
         * RECVONLY:只接收视频流不发送，
         * SENDRECV:发送并接收视频流】
         * 接通，音视频切换时携带
         */
        @JsonProperty("video")
        private String video;

        /**
         * 【INACTIVE:无视频流，
         * SENDONLY:只发送视频流不接收，
         * RECVONLY:只接收视频流不发送，
         * SENDRECV:发送并接收视频流】
         * 上一次的状态，接通，音视频切换时携带
         */
        @JsonProperty("last_video")
        private String lastVideo;

        /**
         * 【false:音视频切换应答， true:音视频切换请求】
         */
        @JsonProperty("media_reneg_request")
        private Boolean mediaRenegRequest;

        /**
         * 发起切换请求id，media_reneg_request为false时为空
         */
        @JsonProperty("req_id")
        private String reqId;

        /**
         * 录制文件名(外呼时有录制，挂断的时候返回)
         */
        @JsonProperty("record_file_name")
        private String recordFileName;

        /**
         * 通话状态【INVITE:外呼, RING:振铃, ANSWER:接通, BRIDGE:桥接, HANGUP:挂断, MEDIARENEG:音视频切换】
         */
        @JsonProperty("status")
        private String status;

        @JsonProperty("da2_result")
        private String da2Result;
    }
}
