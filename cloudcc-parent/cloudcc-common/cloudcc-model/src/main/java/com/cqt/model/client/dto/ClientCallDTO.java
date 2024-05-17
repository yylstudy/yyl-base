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
 * SDK 外呼参数
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientCallDTO extends ClientRequestBaseDTO implements Serializable {

    private static final long serialVersionUID = 3024190574863329815L;

    /**
     * 【0:内线， 1:外线，2:owt房间(会议)】， 默认外线
     */
    @JsonProperty("out_line")
    @NotNull(message = "[out_line]不能为空!")
    private Integer outLine;

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
     * 被叫号码
     */
    @JsonProperty("callee_number")
    @NotEmpty(message = "[callee_number]不能为空!")
    private String calleeNumber;

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
     * 最大外呼时长，超过则结束外呼，默认60s
     */
    @JsonProperty("max_call_time")
    private Integer maxCallTime;

    /**
     * 最大通话时间，默认8小时
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
     * 工单id
     */
    @JsonProperty("work_order_id")
    private String workOrderId;
}
