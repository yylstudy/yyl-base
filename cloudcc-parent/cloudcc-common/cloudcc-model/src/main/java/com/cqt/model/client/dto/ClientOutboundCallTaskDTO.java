package com.cqt.model.client.dto;

import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:59
 * SDK 外呼任务参数
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientOutboundCallTaskDTO extends ClientRequestBaseDTO implements Serializable {

    private static final long serialVersionUID = 3024190574863329815L;

    /**
     * 任务id
     */
    @JsonProperty("task_id")
    private String taskId;

    /**
     * 外呼类型
     * PREDICT_TASK-预测， IVR-IVR流程， VOICE_NOTICE-语音通知, PREVIEW_TASK-预览
     */
    @JsonProperty("task_type")
    @NotEmpty(message = "[task_type]不能为空!")
    private String taskType;

    /**
     * ivr流程id
     */
    @JsonProperty("ivr_id")
    private String ivrId;

    /**
     * 语音通知文件id
     */
    @JsonProperty("voice_notify_file_id")
    private String voiceNotifyFileId;

    /**
     * 等待音id
     */
    @JsonProperty("waiting_tone")
    private String waitingTone;

    /**
     * 客户号码id
     */
    @JsonProperty("number_id")
    @NotEmpty(message = "[number_id]不能为空!")
    private String numberId;

    /**
     * 被叫号码(客户号码)
     */
    private String clientNumber;

    /**
     * 平台号码
     * <p>
     * ivr外呼 - 分配的外显号码
     * <p>
     * 预测外呼 - 分配的外显号码
     */
    private String platformNumber;

    /**
     * 主叫号码
     * <p>
     * ivr外呼(只呼客户) - 同平台号码
     * <p>
     * 预测外呼(先呼客户, 再桥接坐席)
     * <li> 先呼客户 - 平台号码
     * <li> 再桥接坐席 - 平台号码
     */
    @NotEmpty(message = "[caller_number]不能为空!")
    private String callerNumber;

    /**
     * 外显号码(平台号码)
     * <p>
     * ivr外呼 - 平台号码
     * <p>
     * 预测外呼(先呼客户, 再桥接坐席)
     * <li> 先呼客户 - 平台号码
     * <li> 再桥接坐席 - 客户号码
     */
    @JsonProperty("display_number")
    @NotEmpty(message = "[display_number]不能为空!")
    private String displayNumber;

    /**
     * 被叫号码
     * <p>
     * ivr外呼 - 客户号码
     * <p>
     * 预测外呼(先呼客户, 再桥接坐席)
     * <li> 先呼客户 - 客户号码
     * <li> 再桥接坐席 - 分机id
     */
    @JsonProperty("callee_number")
    @NotEmpty(message = "[callee_number]不能为空!")
    private String calleeNumber;

    /**
     * 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3
     */
    @JsonProperty("audio")
    @NotNull(message = "[audio]不能为空!")
    private Integer audio;

    /**
     * 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0
     */
    @JsonProperty("video")
    @NotNull(message = "[video]不能为空!")
    private Integer video;

    /**
     * 视频分辨率【480， 720， 1280】 默认480(video不为0时可用)
     */
    @JsonProperty("pixels")
    private String pixels;

    /**
     * 录制文件后缀【mp3, wav, mp4】默认mp3
     */
    @JsonProperty("record_suffix")
    private String recordSuffix;

    /**
     * 是否立体声录制，默认否(单声道)
     */
    @JsonProperty("is_stereo")
    private Boolean isStereo;

    /**
     * 最大通话时间，默认8小时
     */
    @JsonProperty("max_call_time")
    private Integer maxCallTime;

    /**
     * 最大外呼时长，超过则结束外呼，默认60s
     */
    @JsonProperty("max_ring_time")
    private Integer maxRingTime;

    /**
     * 是否双向录制， 默认单向
     */
    @JsonProperty("record_contact")
    private Boolean recordContact;

    /**
     * 录制节点【NONE:不录制，RING:响铃录制，ANSWER:接通后录制】默认NONE
     */
    @JsonProperty("record_start")
    private String recordStart;

    /**
     * 当前呼叫次数
     */
    @JsonProperty("current_times")
    private Integer currentTimes;

    private String member;
}
