package com.cqt.model.call.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-01-28 10:25
 */
@Data
public class AlibabaAliqinAxbVendorCallControlResponse {

    @JsonProperty("result")
    private Response result;

    @Data
    public static class Response {

        @JsonProperty("code")
        private String code;

        @JsonProperty("control_resp_dto")
        private ControlRespDto controlRespDto;

        @JsonProperty("message")
        private String message;

        @Data
        public static class ControlRespDto {

            @JsonProperty("call_no_play_code")
            private String callNoPlayCode;

            @JsonProperty("called_no_play_code")
            private String calledNoPlayCode;

            @JsonProperty("control_msg")
            private String controlMsg;

            @JsonProperty("control_operate")
            private String controlOperate;

            @JsonProperty("media_degrade")
            private Boolean mediaDegrade;

            @JsonProperty("product_type")
            private String productType;

            @JsonProperty("subs")
            private Subs subs;

            @Data
            public static class Subs {

                @JsonProperty("call_type")
                private String callType;

                @JsonProperty("called_display_no")
                private String calledDisplayNo;

                @JsonProperty("called_no")
                private String calledNo;

                @JsonProperty("end_call_ivr")
                private EndCallIvr endCallIvr;

                @JsonProperty("fast_record")
                private String fastRecord;

                @JsonProperty("need_realtime_media")
                private Boolean needRealtimeMedia;

                @JsonProperty("need_record")
                private Boolean needRecord;

                @JsonProperty("out_id")
                private String outId;

                @JsonProperty("rec_type")
                private String recType;

                @JsonProperty("record_mode")
                private String recordMode;

                @JsonProperty("rrds_control")
                private Long rrdsControl;

                @JsonProperty("rtp_type")
                private String rtpType;

                @JsonProperty("sequence_calls")
                private SequenceCalls sequenceCalls;

                @JsonProperty("sequence_timeout")
                private Long sequenceTimeout;

                @JsonProperty("sms_channel")
                private String smsChannel;

                @JsonProperty("subs_id")
                private String subsId;

                @JsonProperty("ws_addr")
                private String wsAddr;

                @JsonProperty("ws_addr_called")
                private String wsAddrCalled;

            }

            @Data
            public static class SequenceCalls {

                @JsonProperty("sequence_calls")
                private List<SequenceCall> sequenceCalls;

                @Data
                public static class SequenceCall {

                    @JsonProperty("call_no_play_code")
                    private String callNoPlayCode;
                    @JsonProperty("called_display_no")
                    private String calledDisplayNo;
                    @JsonProperty("called_no")
                    private String calledNo;
                    @JsonProperty("called_no_play_code")
                    private String calledNoPlayCode;

                }
            }

            @Data
            public static class EndCallIvr {

                @JsonProperty("end_call_ivr")
                private String endCallIvr;
                @JsonProperty("max_loop")
                private Long maxLoop;
                @JsonProperty("step1_file")
                private String step1File;
                @JsonProperty("step2_file")
                private String step2File;
                @JsonProperty("valid_key")
                private String validKey;
                @JsonProperty("waiting_dtmf_time")
                private Long waitingDtmfTime;
                @JsonProperty("waiting_end_call")
                private Long waitingEndCall;

            }
        }
    }


}
