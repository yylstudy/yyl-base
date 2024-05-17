package com.cqt.model.freeswitch.dto.api;

import com.cqt.base.enums.MediaStreamEnum;
import com.cqt.base.enums.OutLineEnum;
import com.cqt.base.enums.RecordStartEnum;
import com.cqt.model.client.dto.*;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 9:48
 * 外呼接口入参
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Data
public class OriginateDTO extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = -1111022884024191170L;

    /**
     * 是  | 被叫号码
     */
    @JsonProperty("callee_number")
    private String calleeNumber;

    /**
     * 是  | 主叫号码
     */
    @JsonProperty("caller_number")
    private String callerNumber;

    /**
     * 否  | 外显号码， 不填默认和主叫号码一致
     */
    @JsonProperty("display_number")
    private String displayNumber;

    /**
     * 否  | 是否立体声录制，默认否(单声道)
     */
    @JsonProperty("is_stereo")
    private Boolean isStereo;

    /**
     * 否  | 最大通话时间，默认8小时
     */
    @JsonProperty("max_call_time")
    private Integer maxCallTime;

    /**
     * 否  | 最大外呼时长，超过则结束外呼，默认60s
     */
    @JsonProperty("max_ring_time")
    private Integer maxRingTime;

    /**
     * 否  | 通道变量，可在后续流程中通过cqt_ori_user_data获取该通道变量的值
     */
    @JsonProperty("ori_user_data")
    private String oriUserData;

    /**
     * 是  | 【0:内线， 1:外线，2:owt房间(会议)】， 默认外线
     */
    @JsonProperty("out_line")
    private Integer outLine = 1;

    /**
     * 否  | 视频分辨率【480， 720， 1280】 默认480(video不为0时可用)
     */
    @JsonProperty("pixels")
    private String pixels;

    /**
     * 否  | 是否双向录制， 默认单向
     */
    @JsonProperty("record_contact")
    private Boolean recordContact;

    /**
     * 否  | 录制节点【NONE:不录制，RING:响铃录制，ANSWER:接通后录制】默认NONE
     */
    @JsonProperty("record_start")
    private String recordStart;

    /**
     * 否  | 录制文件后缀【mp3, wav, mp4】默认mp3
     */
    @JsonProperty("record_suffix")
    private String recordSuffix;

    /**
     * 否  | 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0
     */
    @JsonProperty("video")
    private Integer video;

    /**
     * 否  | 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3
     */
    @JsonProperty("audio")
    private Integer audio;

    /**
     * 否  | 指定外呼的uuid，不填默认FS自动生成
     */
    @JsonProperty("ori_uuid")
    private String oriUuid;

    /**
     * 对象转化
     * clientTransDTO -> OriginateDTO
     */
    public static OriginateDTO build(ClientTransDTO clientTransDTO) {
        OriginateDTO originateDTO = new OriginateDTO();
        originateDTO.setReqId(clientTransDTO.getReqId());
        originateDTO.setCompanyCode(clientTransDTO.getCompanyCode());
        return originateDTO;
    }

    /**
     * 构建
     *
     * @param clientCallDTO 外呼对象
     * @return 底层外呼对象
     */
    public static OriginateDTO clientCall2Originate(ClientCallDTO clientCallDTO) {
        OriginateDTO originateDTO = new OriginateDTO();
        // 外线 正常号码. 内线为分机id, 需要查下
        originateDTO.setCallerNumber(clientCallDTO.getCalleeNumber());
        originateDTO.setCalleeNumber(clientCallDTO.getExtId());
        originateDTO.setReqId(clientCallDTO.getReqId());
        originateDTO.setCompanyCode(clientCallDTO.getCompanyCode());
        originateDTO.setAudio(clientCallDTO.getAudio());
        originateDTO.setIsStereo(clientCallDTO.getIsStereo());
        originateDTO.setMaxCallTime(clientCallDTO.getMaxCallTime());
        originateDTO.setMaxRingTime(clientCallDTO.getMaxRingTime());
        originateDTO.setOutLine(clientCallDTO.getOutLine());
        originateDTO.setPixels(clientCallDTO.getPixels());
        originateDTO.setRecordContact(clientCallDTO.getRecordContact());
        originateDTO.setRecordStart(clientCallDTO.getRecordStart());
        originateDTO.setRecordSuffix(clientCallDTO.getRecordSuffix());
        originateDTO.setVideo(clientCallDTO.getVideo());
        return originateDTO;
    }

    /**
     * 构建
     *
     * @param taskDTO 外呼任务对象
     * @return 底层外呼对象
     */
    public static OriginateDTO task2Originate(ClientOutboundCallTaskDTO taskDTO) {
        OriginateDTO originateDTO = new OriginateDTO();
        originateDTO.setReqId(taskDTO.getReqId());
        originateDTO.setCompanyCode(taskDTO.getCompanyCode());
        originateDTO.setCallerNumber(taskDTO.getCallerNumber());
        originateDTO.setDisplayNumber(taskDTO.getDisplayNumber());
        originateDTO.setCalleeNumber(taskDTO.getCalleeNumber());
        originateDTO.setIsStereo(taskDTO.getIsStereo());
        originateDTO.setMaxCallTime(taskDTO.getMaxCallTime());
        originateDTO.setMaxRingTime(taskDTO.getMaxRingTime());
        originateDTO.setOutLine(OutLineEnum.OUT_LINE.getCode());
        originateDTO.setPixels(taskDTO.getPixels());
        originateDTO.setRecordContact(taskDTO.getRecordContact());
        originateDTO.setRecordStart(taskDTO.getRecordStart());
        originateDTO.setRecordSuffix(taskDTO.getRecordSuffix());
        originateDTO.setAudio(taskDTO.getAudio());
        originateDTO.setVideo(taskDTO.getVideo());
        return originateDTO;
    }

    /**
     * 播放录音 外呼坐席
     */
    public static OriginateDTO clientPlayRecord2Originate(ClientPlayRecordDTO clientPlayRecordDTO, String displayNumber) {
        OriginateDTO originateDTO = new OriginateDTO();
        // 外线 正常号码. 内线为分机id, 需要查下
        originateDTO.setCallerNumber(displayNumber);
        originateDTO.setCalleeNumber(clientPlayRecordDTO.getExtId());
        originateDTO.setReqId(clientPlayRecordDTO.getReqId());
        originateDTO.setCompanyCode(clientPlayRecordDTO.getCompanyCode());
        originateDTO.setOutLine(OutLineEnum.IN_LINE.getCode());
        originateDTO.setAudio(MediaStreamEnum.SENDRECV.getCode());
        originateDTO.setVideo(MediaStreamEnum.NONE.getCode());
        originateDTO.setRecordStart(RecordStartEnum.NONE.name());
        return originateDTO;
    }

    /**
     * 构建
     *
     * @param previewOutCallDTO 预览外呼对象
     * @return 底层外呼对象
     */
    public static OriginateDTO clientPreviewOutCall2Originate(ClientPreviewOutCallDTO previewOutCallDTO) {
        OriginateDTO originateDTO = new OriginateDTO();
        originateDTO.setReqId(previewOutCallDTO.getReqId());
        originateDTO.setCompanyCode(previewOutCallDTO.getCompanyCode());
        originateDTO.setCallerNumber(previewOutCallDTO.getClientNumber());
        originateDTO.setCalleeNumber(previewOutCallDTO.getExtId());
        originateDTO.setAudio(previewOutCallDTO.getAudio());
        originateDTO.setVideo(previewOutCallDTO.getVideo());
        originateDTO.setIsStereo(previewOutCallDTO.getIsStereo());
        originateDTO.setMaxCallTime(previewOutCallDTO.getMaxCallTime());
        originateDTO.setMaxRingTime(previewOutCallDTO.getMaxRingTime());
        originateDTO.setOutLine(OutLineEnum.IN_LINE.getCode());
        originateDTO.setPixels(previewOutCallDTO.getPixels());
        originateDTO.setRecordContact(previewOutCallDTO.getRecordContact());
        originateDTO.setRecordStart(previewOutCallDTO.getRecordStart());
        originateDTO.setRecordSuffix(previewOutCallDTO.getRecordSuffix());
        return originateDTO;
    }

    /**
     * 咨询时, 外呼属性设置
     */
    public void initConsultProperties(ClientConsultDTO clientConsultDTO) {
        setAudio(clientConsultDTO.getAudio());
        setIsStereo(clientConsultDTO.getIsStereo());
        setMaxCallTime(clientConsultDTO.getMaxCallTime());
        setMaxRingTime(clientConsultDTO.getMaxRingTime());
        setPixels(clientConsultDTO.getPixels());
        setRecordContact(clientConsultDTO.getRecordContact());
        setRecordStart(clientConsultDTO.getRecordStart());
        setRecordSuffix(clientConsultDTO.getRecordSuffix());
        setVideo(clientConsultDTO.getVideo());
    }

    /**
     * 转接时, 外呼属性设置
     */
    public void initTransProperties(ClientTransDTO transDTO) {
        setAudio(transDTO.getAudio());
        setIsStereo(transDTO.getIsStereo());
        setMaxCallTime(transDTO.getMaxCallTime());
        setMaxRingTime(transDTO.getMaxRingTime());
        setPixels(transDTO.getPixels());
        setRecordContact(transDTO.getRecordContact());
        setRecordStart(transDTO.getRecordStart());
        setRecordSuffix(transDTO.getRecordSuffix());
        setVideo(transDTO.getVideo());
    }

    /**
     * 三方通话时, 外呼属性设置
     */
    public void initThreeWayProperties(ClientThreeWayDTO threeWayDTO) {
        setAudio(threeWayDTO.getAudio());
        setIsStereo(threeWayDTO.getIsStereo());
        setMaxCallTime(threeWayDTO.getMaxCallTime());
        setMaxRingTime(threeWayDTO.getMaxRingTime());
        setPixels(threeWayDTO.getPixels());
        setRecordContact(threeWayDTO.getRecordContact());
        setRecordStart(threeWayDTO.getRecordStart());
        setRecordSuffix(threeWayDTO.getRecordSuffix());
        setVideo(threeWayDTO.getVideo());
    }
}
