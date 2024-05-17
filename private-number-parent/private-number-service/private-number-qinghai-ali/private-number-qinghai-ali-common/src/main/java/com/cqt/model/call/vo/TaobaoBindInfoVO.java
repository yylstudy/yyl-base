package com.cqt.model.call.vo;

import com.cqt.model.bind.vo.BindInfoVO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-01-28 13:34
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaobaoBindInfoVO extends BindInfoVO implements Serializable {

    private static final long serialVersionUID = 4127128793144996291L;

    /**
     * 顺振参数数组
     */
    @JsonProperty("sequence_calls")
    @ApiModelProperty("顺振参数数组")
    private List<SequenceCall> sequenceCalls;

    @ApiModelProperty("短信通道方式SMS_INTERCEPT(拦截推送阿里)，SMS_NORMAL_SEND(正常现网下发)，SMS_DROP(拦截丢弃)")
    @JsonProperty("sms_channel")
    private String smsChannel;

    /**
     * 呼叫类型MASTER(A->X->B), CALLED(B->X->A), SMS_SENDER, SMS_RECEIVER
     */
    @JsonProperty("call_type")
    @ApiModelProperty("呼叫类型MASTER(A->X->B), CALLED(B->X->A), SMS_SENDER, SMS_RECEIVER")
    private String callType;

    /**
     * 是否媒体资源降级,放弃录音放音功能；接入方无此相关功能，可忽略;  0：否、1：是
     */
    @JsonProperty("media_degrade")
    @ApiModelProperty("是否媒体资源降级,放弃录音放音功能；接入方无此相关功能，可忽略;  0：否、1：是")
    private Integer mediaDegrade;

    /**
     * 顺振超时时间
     */
    @JsonProperty("sequence_timeout")
    @ApiModelProperty(value = "顺振超时时间", example = "15")
    private Long sequenceTimeout;

    /**
     * 录音内容模式，1：仅录制通话录音、2：放音录音+通话录音
     */
    @JsonProperty("record_content_mode")
    @ApiModelProperty(value = "录音内容模式，1：仅录制通话录音、2：放音录音+通话录音", example = "1")
    private Integer recordContentMode;

    /**
     * 是否需要优先下载录音，0：否、1：是
     */
    @JsonProperty("fast_record")
    @ApiModelProperty(value = "是否需要优先下载录音，0：否、1：是", example = "1")
    private Integer fastRecord;

    /**
     * 是否开启铃音检测 0：不开启 1：开启
     */
    @JsonProperty("rrds_control")
    @ApiModelProperty(value = "是否开启铃音检测 0：不开启 1：开启", example = "1")
    private Long rrdsControl;

    /**
     * 主叫媒体流推送地址
     */
    @JsonProperty("ws_addr")
    @ApiModelProperty(value = "主叫媒体流推送地址")
    private String wsAddr;

    /**
     * 被叫媒体流推送地址
     */
    @JsonProperty("ws_addr_called")
    @ApiModelProperty(value = "被叫媒体流推送地址")
    private String wsAddrCalled;

    /**
     * 外部回传字段
     */
    @JsonProperty("out_id")
    @ApiModelProperty(value = "外部回传字段")
    private String outId;

    /**
     * 是否实时媒体; 0：否、1：是
     */
    @JsonProperty("need_realtime_media")
    @ApiModelProperty(value = "是否实时媒体; 0：否、1：是")
    private Integer needRealtimeMedia;

    /**
     * 实时媒体类型1 彩铃 2 通话 3 彩铃和通话
     */
    @JsonProperty("rtp_type")
    @ApiModelProperty(value = "实时媒体类型1 彩铃 2 通话 3 彩铃和通话")
    private Integer rtpType;

    /**
     * 挂机IVR参数
     */
    @JsonProperty("end_call_ivr")
    @ApiModelProperty(value = "挂机IVR参数")
    private EndCallIvr endCallIvr;

    /**
     * 顺振参数
     */
    @Data
    public static class SequenceCall {

        /**
         * 主叫放音
         */
        @JsonProperty("call_no_play_code")
        @ApiModelProperty(value = "顺振-主叫放音")
        private String callNoPlayCode;

        /**
         * 被叫放音
         */
        @JsonProperty("called_display_no")
        @ApiModelProperty(value = "顺振-被叫放音")
        private String calledDisplayNo;

        /**
         * 被叫号码
         */
        @JsonProperty("called_no")
        @ApiModelProperty(value = "顺振-被叫号码")
        private String calledNo;

        /**
         * 被叫号显
         */
        @JsonProperty("called_no_play_code")
        @ApiModelProperty(value = "顺振-被叫号显")
        private String calledNoPlayCode;

    }

    /**
     * 挂机IVR参数
     */
    @Data
    public static class EndCallIvr {

        /**
         * 挂机ivr开关 enable
         */
        @JsonProperty("end_call_ivr")
        @ApiModelProperty(value = "挂机ivr开关 enable")
        private String endCallIvr;

        /**
         * 循环次数
         */
        @JsonProperty("max_loop")
        @ApiModelProperty(value = "循环次数")
        private Long maxLoop;

        /**
         * 第一步放音文件
         */
        @JsonProperty("step1_file")
        @ApiModelProperty(value = "第一步放音文件")
        private String step1File;

        /**
         * 第二步放音文件
         */
        @JsonProperty("step2_file")
        @ApiModelProperty(value = "第二步放音文件")
        private String step2File;

        /**
         * 有效按键
         */
        @JsonProperty("valid_key")
        @ApiModelProperty(value = "有效按键")
        private String validKey;

        /**
         * 最大等待时长，单位秒
         */
        @JsonProperty("waiting_dtmf_time")
        @ApiModelProperty(value = "最大等待时长，单位秒")
        private Long waitingDtmfTime;

        /**
         * 挂机等待时长
         */
        @JsonProperty("waiting_end_call")
        @ApiModelProperty(value = "挂机等待时长")
        private Long waitingEndCall;

    }

    public TaobaoBindInfoVO(Integer code, String message, String callerIvr) {
        super(code, message, callerIvr);
    }

    public TaobaoBindInfoVO(Integer code, String message, String callerIvr, String numType) {
        super(code, message, callerIvr, numType);
    }

    public static TaobaoBindInfoVO fail(Integer code, String message) {
        return new TaobaoBindInfoVO(code, message, null);
    }

    public static TaobaoBindInfoVO fail(Integer code, String message, String callerIvr) {
        return new TaobaoBindInfoVO(code, message, callerIvr);
    }

    public static TaobaoBindInfoVO okExt(Integer code, String message, String callerIvr, String numType) {
        return new TaobaoBindInfoVO(code, message, callerIvr, numType);
    }
}
