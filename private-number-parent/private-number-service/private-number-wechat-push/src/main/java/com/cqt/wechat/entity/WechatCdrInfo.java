package com.cqt.wechat.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huweizhong
 * date  2023/2/22 9:49
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WechatCdrInfo {
    /**
     * 话单基础信息
     */
    @JsonProperty("base_info")
    @ApiModelProperty(value = "话单基础信息")
    private CallBaseInfo baseInfo;
    /**
     * 话单状态信息
     */
    @JsonProperty("status")
    @ApiModelProperty(value = "话单状态信息")
    private CallStatus status;
    /**
     * 话单录⾳信息
     */
    @JsonProperty("record")
    @ApiModelProperty(value = "话单录⾳信息")
    private CallRecord record;

    /**
     * 话单基础信息
     */
    @Data
    public static class CallBaseInfo {

        /**
         * 本次话单唯⼀标识
         */
        @JsonProperty("call_id")
        @ApiModelProperty(value = "本次话单唯⼀标识")
        private String callId;
        /**
         * 区号
         */
        @JsonProperty("area_code")
        @ApiModelProperty(value = "区号")
        private String areaCode;
        /**
         * 绑定ID
         */
        @JsonProperty("bind_id")
        @ApiModelProperty(value = "绑定ID")
        private String bindId;
        /**
         * 主叫真实号码
         */
        @JsonProperty("caller")
        @ApiModelProperty(value = "主叫真实号码")
        private String caller;
        /**
         * 被叫真实号码
         */
        @JsonProperty("called")
        @ApiModelProperty(value = "被叫真实号码")
        private String called;
        /**
         * 分配虚号
         */
        @JsonProperty("tel_x")
        @ApiModelProperty(value = "分配虚号")
        private String telX;
        /**
         * 分配分机号
         */
        @JsonProperty("tel_x_ext")
        @ApiModelProperty(value = "分配分机号")
        private String telXext;
    }

    /**
     * 话单状态信息
     */
    @Data
    public static class CallStatus {

        /**
         * 主叫拨打时间(毫秒)
         */
        @JsonProperty("begin_time")
        @ApiModelProperty(value = "主叫拨打时间(毫秒)")
        private Integer beginTime;
        /**
         * 被叫接通时间(毫秒)
         */
        @JsonProperty("connect_time")
        @ApiModelProperty(value = "被叫接通时间(毫秒)")
        private Integer connectTime;
        /**
         * 被叫振铃时间(毫秒)
         */
        @JsonProperty("alerting_time")
        @ApiModelProperty(value = "被叫振铃时间(毫秒)")
        private Integer alertTime;
        /**
         * 通话结束时间(毫秒)
         */
        @JsonProperty("release_time")
        @ApiModelProperty(value = "通话结束时间(毫秒)")
        private Integer releaseTime;
        /**
         * 通话时⻓(秒)
         */
        @JsonProperty("call_duration")
        @ApiModelProperty(value = "通话时⻓(秒)")
        private Integer callDuration;
        /**
         * 通话结束原因码(CallResult)
         */
        @JsonProperty("call_result")
        @ApiModelProperty(value = "通话结束原因码(CallResult)")
        private Integer callResult;

    }

    /**
     * 话单录⾳信息
     */
    @Data
    public static class CallRecord {

        /**
         * 0:补录⾳; 1:录⾳
         */
        @JsonProperty("record_flag")
        @ApiModelProperty(value = "0:补录⾳; 1:录⾳")
        private Integer resultFlag;
        /**
         * 录⾳开始时间(毫秒)
         */
        @JsonProperty("record_start_time")
        @ApiModelProperty(value = "录⾳开始时间(毫秒)")
        private Integer recordStartTime;
        /**
         * 通话录⾳url
         */
        @JsonProperty("record_file_url")
        @ApiModelProperty(value = "通话录⾳url")
        private String recordFileUrl;


    }
}
