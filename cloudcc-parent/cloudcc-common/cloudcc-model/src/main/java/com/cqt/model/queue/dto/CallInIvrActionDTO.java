package com.cqt.model.queue.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-18 16:11
 * IVR 转技能排队分配坐席
 */
@Data
public class CallInIvrActionDTO implements Serializable {

    /**
     * 技能id
     */
    @JsonProperty("skill_id")
    private String skillId;

    /**
     * 技能等待音文件id
     */
    @JsonProperty("file_id")
    private String fileId;

    /**
     * 技能等待音
     */
    @JsonProperty("skill_wait_tone")
    private String skillWaitTone;

    /**
     * 企业id
     */
    @JsonProperty("company_code")
    private String companyCode;

    /**
     * 来电通话唯一标识
     */
    private String uuid;

    /**
     * 来电号码(主叫号码)
     */
    @JsonProperty("caller_number")
    private String callerNumber;

    /**
     * 被叫号码
     */
    @JsonProperty("callee_number")
    private String calleeNumber;

    /**
     * 超时时间
     * 排队-最长排队时间(s)
     * 转坐席/外线-响铃超时时间
     * 留言-留言超时时间
     */
    @JsonProperty("timeout")
    private Integer timeout;

    /**
     * 留言开始提示音
     */
    @JsonProperty("message_start_record_tone")
    private String messageStartRecordTone;

    /**
     * 最大重试次数
     */
    @JsonProperty("max_retry")
    private Integer maxRetry;

    /**
     * 当前排队次数
     * 第几次排队
     */
    @JsonProperty("current_times")
    private Integer currentTimes;

    /**
     * 当前排队时间戳
     */
    private Long timestamp;

    /**
     * 第一次排队时间戳
     */
    @JsonProperty("first_timestamp")
    private Long firstTimestamp;

    /**
     * 是否外呼
     */
    @JsonProperty("call_bridge")
    private Boolean callBridge = true;

    /**
     * 【NONE:无语音流， SENDONLY:只发送语音流不接收， RECVONLY:只接收语音流不发送， SENDRECV:发送并接收语音流】
     *
     * @see com.cqt.base.enums.MediaStreamEnum
     */
    @JsonProperty("audio")
    private Integer audio;

    /**
     * 【NONE:无视频流， SENDONLY:只发送视频流不接收， RECVONLY:只接收视频流不发送， SENDRECV:发送并接收视频流】
     *
     * @see com.cqt.base.enums.MediaStreamEnum
     */
    @JsonProperty("video")
    private Integer video;

    /**
     * 类型: 1-排队ivr, 2-转接坐席, 3-转接外线, 4-留言, 5-转技能
     */
    private Integer type;

    /**
     * 转接坐席id
     */
    @JsonProperty("agent_id")
    private String agentId;

    /**
     * 转接外线号码
     */
    @JsonProperty("outline_number")
    private String outlineNumber;

    /**
     * 留言结束按钮
     */
    private String dtmf;

}
