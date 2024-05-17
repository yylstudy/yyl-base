package com.cqt.model.freeswitch.dto.api;

import cn.hutool.core.util.IdUtil;
import com.cqt.base.enums.OutLineEnum;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.cqt.model.client.dto.ClientCallDTO;
import com.cqt.model.client.dto.ClientOutboundCallTaskDTO;
import com.cqt.model.client.dto.ClientPreviewOutCallDTO;
import com.cqt.model.client.dto.ClientTransDTO;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.cqt.model.queue.dto.CallInIvrActionDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:12
 * 外呼并桥接入参
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Data
public class CallBridgeDTO extends FreeswitchApiBase implements Serializable {

    /**
     * 否  | 指定外呼的uuid，不填默认FS自动生成
     */
    @JsonProperty("ori_uuid")
    private String oriUuid;

    /**
     * 是  | 源通话ID
     */
    @JsonProperty("s_uuid")
    private String sUuid;

    /**
     * 否  | 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3
     */
    @JsonProperty("audio")
    private Integer audio;

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

    @JsonProperty("ori_user_data")
    private String oriUserData;

    /**
     * 是  | 【0:内线， 1:外线，2:owt房间】， 默认外线
     */
    @JsonProperty("out_line")
    private Integer outLine;

    /**
     * 否  | 视频分辨率【480， 720， 1280】 默认480
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
     * 坐席接通之后, 给坐席放音, 与客户桥接之后自动结束放音.
     * 不填底层默认 嘟嘟嘟嘟
     * 呼出坐席等待音 callOutRinging
     *
     * @see com.cqt.model.company.entity.CompanyInfo#callOutRinging
     */
    @JsonProperty("ring_back")
    private String ringBack;

    /**
     * 转化对象
     * callInIvrActionDTO -> CallBridgeDTO
     */
    public static CallBridgeDTO build(CallUuidContext callUuidContext,
                                      CallInIvrActionDTO callInIvrActionDTO,
                                      String oriUuid,
                                      String calleeNumber,
                                      OutLineEnum outLineEnum) {
        CallBridgeDTO callBridgeDTO = new CallBridgeDTO();
        callBridgeDTO.setReqId(IdUtil.fastUUID());
        callBridgeDTO.setCompanyCode(callInIvrActionDTO.getCompanyCode());
        callBridgeDTO.setSUuid(callInIvrActionDTO.getUuid());
        callBridgeDTO.setOriUuid(oriUuid);
        callBridgeDTO.setCallerNumber(callInIvrActionDTO.getCallerNumber());
        callBridgeDTO.setCalleeNumber(calleeNumber);
        callBridgeDTO.setOutLine(outLineEnum.getCode());
        callBridgeDTO.setAudio(callUuidContext.getAudio());
        callBridgeDTO.setVideo(callUuidContext.getVideo());
        callBridgeDTO.setMaxRingTime(callInIvrActionDTO.getTimeout());
        return callBridgeDTO;
    }

    /**
     * 转化对象-必填字段
     * clientRequestBaseDTO -> CallBridgeDTO
     */
    public static CallBridgeDTO build(ClientRequestBaseDTO clientRequestBaseDTO) {
        CallBridgeDTO callBridgeDTO = new CallBridgeDTO();
        callBridgeDTO.setReqId(clientRequestBaseDTO.getReqId());
        callBridgeDTO.setCompanyCode(clientRequestBaseDTO.getCompanyCode());
        return callBridgeDTO;
    }

    /**
     * 转化对象-盲转
     * clientTransDTO -> CallBridgeDTO
     */
    public static CallBridgeDTO build(ClientTransDTO clientTransDTO, String oriUuid) {
        CallBridgeDTO callBridgeDTO = new CallBridgeDTO();
        callBridgeDTO.setReqId(clientTransDTO.getReqId());
        callBridgeDTO.setCompanyCode(clientTransDTO.getCompanyCode());
        callBridgeDTO.setSUuid(clientTransDTO.getUuid());
        callBridgeDTO.setOriUuid(oriUuid);
        callBridgeDTO.setCallerNumber(clientTransDTO.getAgentId());
        callBridgeDTO.setCalleeNumber(clientTransDTO.getTransNumber());
        return callBridgeDTO;
    }

    /**
     * 预测外呼-桥接参数
     *
     * @param callStatusEventDTO 当前事件消息
     * @param clientContext      当前uuid上下文-客户
     */
    public static CallBridgeDTO buildCallBridgeDTO(CallStatusEventDTO callStatusEventDTO,
                                                   CallUuidContext clientContext,
                                                   String agentUuid,
                                                   String extId) {
        ClientOutboundCallTaskDTO callTaskDTO = clientContext.getClientOutboundCallTaskDTO();
        CallBridgeDTO callBridgeDTO = new CallBridgeDTO();
        callBridgeDTO.setReqId(callTaskDTO.getReqId());
        callBridgeDTO.setCompanyCode(callStatusEventDTO.getCompanyCode());
        callBridgeDTO.setSUuid(clientContext.getUUID());
        callBridgeDTO.setOriUuid(agentUuid);
        callBridgeDTO.setServerId(callStatusEventDTO.getServerId());

        callBridgeDTO.setCallerNumber(callTaskDTO.getClientNumber());
        callBridgeDTO.setDisplayNumber(callTaskDTO.getClientNumber());
        callBridgeDTO.setCalleeNumber(extId);
        callBridgeDTO.setOutLine(OutLineEnum.IN_LINE.getCode());
        callBridgeDTO.setAudio(clientContext.getAudio());
        callBridgeDTO.setVideo(clientContext.getVideo());
        callBridgeDTO.setPixels(callTaskDTO.getPixels());
        callBridgeDTO.setMaxRingTime(callTaskDTO.getMaxRingTime());
        callBridgeDTO.setMaxCallTime(callTaskDTO.getMaxCallTime());
        callBridgeDTO.setRecordStart(callTaskDTO.getRecordStart());
        callBridgeDTO.setRecordContact(callTaskDTO.getRecordContact());
        callBridgeDTO.setIsStereo(callTaskDTO.getIsStereo());
        callBridgeDTO.setRecordSuffix(callTaskDTO.getRecordSuffix());
        return callBridgeDTO;
    }

    public void setClientCallParams(ClientCallDTO clientCallDTO) {
        setVideo(clientCallDTO.getVideo());
        setAudio(clientCallDTO.getAudio());
        setPixels(clientCallDTO.getPixels());
        setMaxRingTime(clientCallDTO.getMaxRingTime());
        setMaxCallTime(clientCallDTO.getMaxCallTime());
        setRecordStart(clientCallDTO.getRecordStart());
        setRecordContact(clientCallDTO.getRecordContact());
        setIsStereo(clientCallDTO.getIsStereo());
        setRecordSuffix(clientCallDTO.getRecordSuffix());
    }

    public void setClientCallParams(ClientPreviewOutCallDTO previewOutCallDTO) {
        setVideo(previewOutCallDTO.getVideo());
        setAudio(previewOutCallDTO.getAudio());
        setPixels(previewOutCallDTO.getPixels());
        setMaxRingTime(previewOutCallDTO.getMaxRingTime());
        setMaxCallTime(previewOutCallDTO.getMaxCallTime());
        setRecordStart(previewOutCallDTO.getRecordStart());
        setRecordContact(previewOutCallDTO.getRecordContact());
        setIsStereo(previewOutCallDTO.getIsStereo());
        setRecordSuffix(previewOutCallDTO.getRecordSuffix());
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
}
