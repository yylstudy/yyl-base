package com.cqt.model.client.vo;

import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 呼入 响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientCallInVO extends ClientResponseBaseVO implements Serializable {

    private static final long serialVersionUID = 3113424155698909917L;

    /**
     * 坐席id
     */
    @JsonProperty("agent_id")
    private String agentId;

    /**
     * 分机id
     */
    @JsonProperty("ext_id")
    private String extId;

    /**
     * 呼入uuid-坐席的uuid
     */
    private String uuid;

    @JsonProperty("client_uuid")
    private String clientUuid;

    /**
     * 呼入号码
     */
    @JsonProperty("caller_number")
    private String callerNumber;

    /**
     * 被叫号码(平台号码)
     */
    @JsonProperty("callee_number")
    private String calleeNumber;

    /**
     * 随路参数
     */
    @JsonProperty("flow_data")
    private String flowData;

    /**
     * 呼入通道(转接、咨询、会议、预测外呼空为正常)
     */
    @JsonProperty("callin_channel")
    private String callinChannel;

    /**
     * 是否为视频通话 0否 1是
     */
    @Deprecated
    @JsonProperty("is_video")
    private Integer isVideo;

    /**
     * 否  | 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0
     */
    @JsonProperty("video")
    private Integer video;

    /**
     * 否  | 视频分辨率【480， 720， 1280】 默认480
     */
    @JsonProperty("pixels")
    private Integer pixels;

    /**
     * 否  | 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3
     */
    @JsonProperty("audio")
    private Integer audio;

    /**
     * 构建对象
     */
    public static ClientCallInVO buildClientCallInVO(CallStatusEventDTO callStatusEventDTO,
                                                     CallUuidContext callUuidContext) {
        CallUuidRelationDTO current = callUuidContext.getCurrent();
        ClientCallInVO clientCallInVO = new ClientCallInVO();
        clientCallInVO.setMsgType(MsgTypeEnum.callin.name());
        clientCallInVO.setCompanyCode(callStatusEventDTO.getCompanyCode());
        clientCallInVO.setAgentId(current.getAgentId());
        clientCallInVO.setExtId(current.getExtId());
        clientCallInVO.setUuid(callStatusEventDTO.getUuid());
        clientCallInVO.setClientUuid(callUuidContext.findClientUUID());
        clientCallInVO.setCallerNumber(callStatusEventDTO.getData().getCallerNumber());
        clientCallInVO.setCalleeNumber(callUuidContext.getCalleeNumber());
        clientCallInVO.setCallinChannel(callUuidContext.getCurrent().getCallInChannel().name());
        clientCallInVO.setAudio(callUuidContext.getAudio());
        clientCallInVO.setVideo(callUuidContext.getVideo());
        clientCallInVO.setReqId(callStatusEventDTO.getUuid());
        clientCallInVO.setOs(callUuidContext.getCurrent().getOs());
        return clientCallInVO;
    }

    /**
     * 构建对象
     */
    public static ClientCallInVO buildClientPredictCallInVO(CallStatusEventDTO callStatusEventDTO,
                                                            CallUuidContext callUuidContext) {
        CallUuidRelationDTO current = callUuidContext.getCurrent();
        ClientCallInVO clientCallInVO = new ClientCallInVO();
        clientCallInVO.setMsgType(MsgTypeEnum.callin.name());
        clientCallInVO.setCompanyCode(callStatusEventDTO.getCompanyCode());
        clientCallInVO.setAgentId(current.getAgentId());
        clientCallInVO.setExtId(current.getExtId());
        clientCallInVO.setUuid(callStatusEventDTO.getUuid());
        clientCallInVO.setClientUuid(callUuidContext.findClientUUID());
        clientCallInVO.setCallerNumber(callStatusEventDTO.getData().getCallerNumber());
        clientCallInVO.setCalleeNumber(callUuidContext.getClientOutboundCallTaskDTO().getPlatformNumber());
        clientCallInVO.setCallinChannel(callUuidContext.getCurrent().getCallInChannel().name());
        clientCallInVO.setAudio(callUuidContext.getAudio());
        clientCallInVO.setVideo(callUuidContext.getVideo());
        clientCallInVO.setReqId(callStatusEventDTO.getUuid());
        clientCallInVO.setOs(callUuidContext.getOs());
        clientCallInVO.setCalleeNumber(callUuidContext.getClientOutboundCallTaskDTO().getTaskId());
        clientCallInVO.setTaskId(callUuidContext.getClientOutboundCallTaskDTO().getTaskId());
        return clientCallInVO;
    }
}
