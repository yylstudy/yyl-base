package com.cqt.wechat.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huweizhong
 * date  2023/2/22 10:15
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MsgInfo {
    /**
     * 短信基础信息
     */
    @JsonProperty("base_info")
    @ApiModelProperty(value = "短信基础信息")
    private MsgBaseInfo baseInfo;

    /**
     * 短信基础信息
     */
    @Data
    public static class MsgBaseInfo {

        /**
         * 本次短信唯⼀标识
         */
        @JsonProperty("sms_id")
        @ApiModelProperty(value = "本次短信唯⼀标识")
        private String smsId;
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
         * 发送者真实号码
         */
        @JsonProperty("sender")
        @ApiModelProperty(value = "发送者真实号码")
        private String sender;
        /**
         * 接收者真实号码
         */
        @JsonProperty("receiver")
        @ApiModelProperty(value = "发送者真实号码")
        private String receiver;
        /**
         * 发送者分配号码
         */
        @JsonProperty("sender_show")
        @ApiModelProperty(value = "发送者分配号码")
        private String senderShow;
        /**
         * 接收者分配号码
         */
        @JsonProperty("receiver_show")
        @ApiModelProperty(value = "接收者分配号码")
        private String receiverShow;
        /**
         * 短信发送时刻(秒)
         */
        @JsonProperty("transfer_time")
        @ApiModelProperty(value = "短信发送时刻(秒)")
        private Integer transferTime;
        /**
         * 短信内容
         */
        @JsonProperty("sms_content")
        @ApiModelProperty(value = "短信内容")
        private String smsContent;
        /**
         * 短信状态(MsgStatus)
         */
        @JsonProperty("sms_result")
        @ApiModelProperty(value = "短信状态(MsgStatus)")
        private Integer smsResult;
    }
}
