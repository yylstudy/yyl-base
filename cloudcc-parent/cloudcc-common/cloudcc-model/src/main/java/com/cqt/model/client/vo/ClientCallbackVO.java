package com.cqt.model.client.vo;

import cn.hutool.core.util.IdUtil;
import com.cqt.base.enums.CallbackActionEnum;
import com.cqt.base.enums.CallbackPhaseEnum;
import com.cqt.base.enums.MediaStreamEnum;
import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.ext.dto.ExtStatusTransferDTO;
import com.cqt.model.freeswitch.dto.api.CallBridgeDTO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 回调通知 响应
 * <pre>
 * {
 *     "req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",
 *     "company_code":"90001",
 *     "msg_type":" callback",
 *     "agent_id":"090008_6000",
 *     "ext_id":"090008_5000" ,
 *     "action":"CONSULT" ,
 *     "success":true,
 *     "msg": "咨询成功, 通话建立!",
 *     "os": "Windows"
 * }
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientCallbackVO extends ClientResponseBaseVO implements Serializable {

    private static final long serialVersionUID = -2372939590647360393L;

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
     * 操作类型:
     * <p>
     * CALL 坐席外呼<p>
     * CONSULT 坐席发起咨询<p>
     * TRANS 坐席发起转接<p>
     * THREE_WAY 坐席发起三方通话<p>
     * EAVESDROP 管理员坐席发起监听<p>
     * WHISPER 管理员坐席发耳语<p>
     * SUBSTITUTE 代接<p>
     * FORCE_CALL 强插(三方)<p>
     * INTERRUPT_CALL 强拆<p>
     * <p>
     * EXT_UN_REG 分机注销
     */
    private String action;

    /**
     * action进行阶段: <p>
     * 发起action INITIATE <p>
     * 开始通话 START_CALL <p>
     * 结束通话 END_CALL <p>
     */
    private String phase;

    /**
     * 当前时间
     */
    @JsonProperty("curren_time")
    private Date currentTime;

    /**
     * 与坐席正在一起通话的号码列表
     */
    @JsonProperty("in_call_numbers")
    private Set<String> inCallNumbers;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 【INACTIVE:无语音流，
     * SENDONLY:只发送语音流不接收，
     * RECVONLY:只接收语音流不发送，
     * SENDRECV:发送并接收语音流】
     * 接通，音视频切换时携带
     */
    @JsonProperty("audio")
    private Integer audio;

    /**
     * 【INACTIVE:无语音流，
     * SENDONLY:只发送语音流不接收，
     * RECVONLY:只接收语音流不发送，
     * SENDRECV:发送并接收语音流】
     * 上一次的状态，接通，音视频切换时携带
     */
    @JsonProperty("last_audio")
    private Integer lastAudio;

    /**
     * 【INACTIVE:无视频流，
     * SENDONLY:只发送视频流不接收，
     * RECVONLY:只接收视频流不发送，
     * SENDRECV:发送并接收视频流】
     * 接通，音视频切换时携带
     */
    @JsonProperty("video")
    private Integer video;

    /**
     * 【INACTIVE:无视频流，
     * SENDONLY:只发送视频流不接收，
     * RECVONLY:只接收视频流不发送，
     * SENDRECV:发送并接收视频流】
     * 上一次的状态，接通，音视频切换时携带
     */
    @JsonProperty("last_video")
    private Integer lastVideo;

    /**
     * 音视频切换标志
     * true: 当前坐席发起音视频切换
     * false: 对方接通情况
     */
    @JsonProperty("change_media_flag")
    private Integer changeMediaFlag;

    /**
     * 工单id
     *
     * @since 7.0.0
     */
    @JsonProperty("work_order_id")
    private String workOrderId;

    /**
     * 工单回呼-CALL时, 对方接通, 返回对方通话uuid
     *
     * @since 7.0.0
     */
    @JsonProperty("peer_uuid")
    private String peerUuid;

    @JsonProperty("display_number")
    private String displayNumber;

    /**
     * 分机注销通知
     */
    public static ClientCallbackVO build(ExtStatusTransferDTO transferDTO) {
        ClientCallbackVO callbackVO = new ClientCallbackVO();
        callbackVO.setReqId(transferDTO.getUuid());
        callbackVO.setCompanyCode(transferDTO.getCompanyCode());
        callbackVO.setMsgType(MsgTypeEnum.callback.name());
        callbackVO.setAgentId(transferDTO.getAgentId());
        callbackVO.setExtId(transferDTO.getExtId());
        callbackVO.setAction(CallbackActionEnum.EXT_UN_REG.name());
        callbackVO.setOs(transferDTO.getOs());
        callbackVO.setSuccess(true);
        callbackVO.setMsg("分机已注销");
        return callbackVO;
    }

    /**
     * 外呼结果通知
     */
    public static ClientCallbackVO build(CallBridgeDTO callBridgeDTO, String agentId, String extId, Boolean success,
                                         String msg, String os) {
        ClientCallbackVO callbackVO = new ClientCallbackVO();
        callbackVO.setReqId(callBridgeDTO.getReqId());
        callbackVO.setCompanyCode(callBridgeDTO.getCompanyCode());
        callbackVO.setMsgType(MsgTypeEnum.callback.name());
        callbackVO.setAgentId(agentId);
        callbackVO.setExtId(extId);
        callbackVO.setAction(CallbackActionEnum.CALL.name());
        callbackVO.setSuccess(success);
        callbackVO.setOs(os);
        callbackVO.setMsg(msg);
        return callbackVO;
    }

    /**
     * xfer结果通知
     */
    public static ClientCallbackVO build(String companyCode,
                                         String agentId,
                                         String extId,
                                         String action,
                                         Boolean success,
                                         String msg,
                                         String os) {
        ClientCallbackVO callbackVO = new ClientCallbackVO();
        callbackVO.setReqId(IdUtil.fastUUID());
        callbackVO.setCompanyCode(companyCode);
        callbackVO.setMsgType(MsgTypeEnum.callback.name());
        callbackVO.setAgentId(agentId);
        callbackVO.setExtId(extId);
        callbackVO.setAction(action);
        callbackVO.setSuccess(success);
        callbackVO.setOs(os);
        callbackVO.setMsg(msg);
        return callbackVO;
    }

    /**
     * 呼入回调
     */
    public static ClientCallbackVO callbackNoAnswer(CallUuidContext callUuidContext) {
        ClientCallbackVO callbackVO = new ClientCallbackVO();
        callbackVO.setReqId(IdUtil.fastUUID());
        callbackVO.setCompanyCode(callUuidContext.getCompanyCode());
        callbackVO.setMsgType(MsgTypeEnum.callback.name());
        callbackVO.setAgentId(callUuidContext.getCurrent().getAgentId());
        callbackVO.setExtId(callUuidContext.getCurrent().getExtId());
        callbackVO.setAction(callUuidContext.getCurrent().getCallInChannel().name());
        callbackVO.setOs(callUuidContext.getCurrent().getOs());
        callbackVO.setPhase(CallbackPhaseEnum.END_CALL.name());
        callbackVO.setSuccess(false);
        callbackVO.setMsg("对方未接通!");
        return callbackVO;
    }

    /**
     * 呼入回调
     */
    public static ClientCallbackVO callbackNormalHangup(CallUuidContext callUuidContext) {
        ClientCallbackVO callbackVO = new ClientCallbackVO();
        callbackVO.setReqId(IdUtil.fastUUID());
        callbackVO.setCompanyCode(callUuidContext.getCompanyCode());
        callbackVO.setMsgType(MsgTypeEnum.callback.name());
        callbackVO.setAgentId(callUuidContext.getCurrent().getAgentId());
        callbackVO.setExtId(callUuidContext.getCurrent().getExtId());
        callbackVO.setAction(callUuidContext.getCurrent().getCallInChannel().name());
        callbackVO.setOs(callUuidContext.getCurrent().getOs());
        callbackVO.setPhase(CallbackPhaseEnum.END_CALL.name());
        callbackVO.setSuccess(true);
        callbackVO.setMsg("对方已挂断!");
        return callbackVO;
    }

    /**
     * 构建对象-初始化
     */
    public static ClientCallbackVO buildInit(CallUuidContext callUuidContext,
                                             CallbackActionEnum callbackActionEnum,
                                             Boolean success,
                                             String msg) {
        ClientCallbackVO callbackVO = new ClientCallbackVO();
        callbackVO.setReqId(IdUtil.fastUUID());
        callbackVO.setCompanyCode(callUuidContext.getCompanyCode());
        callbackVO.setMsgType(MsgTypeEnum.callback.name());
        callbackVO.setAgentId(callUuidContext.getAgentId());
        callbackVO.setExtId(callUuidContext.getExtId());
        callbackVO.setAction(callbackActionEnum.name());
        callbackVO.setOs(callUuidContext.getCurrent().getOs());
        callbackVO.setPhase(CallbackPhaseEnum.INITIATE.name());
        callbackVO.setSuccess(success);
        callbackVO.setMsg(msg);
        return callbackVO;
    }

    /**
     * 构建对象-开始通话
     */
    public static ClientCallbackVO buildStart(CallUuidContext callUuidContext, CallbackActionEnum callbackActionEnum) {
        ClientCallbackVO callbackVO = new ClientCallbackVO();
        callbackVO.setReqId(IdUtil.fastUUID());
        callbackVO.setCompanyCode(callUuidContext.getCompanyCode());
        callbackVO.setMsgType(MsgTypeEnum.callback.name());
        callbackVO.setAgentId(callUuidContext.getAgentId());
        callbackVO.setExtId(callUuidContext.getExtId());
        callbackVO.setAction(callbackActionEnum.name());
        callbackVO.setOs(callUuidContext.getCurrent().getOs());
        callbackVO.setPhase(CallbackPhaseEnum.START_CALL.name());
        callbackVO.setSuccess(true);
        callbackVO.setMsg("对方已接通!");
        return callbackVO;
    }

    /**
     * 构建对象-开始通话
     */
    public static ClientCallbackVO buildStart(CallUuidContext callUuidContext, CallbackActionEnum callbackActionEnum, String msg) {
        ClientCallbackVO callbackVO = new ClientCallbackVO();
        callbackVO.setReqId(IdUtil.fastUUID());
        callbackVO.setCompanyCode(callUuidContext.getCompanyCode());
        callbackVO.setMsgType(MsgTypeEnum.callback.name());
        callbackVO.setAgentId(callUuidContext.getAgentId());
        callbackVO.setExtId(callUuidContext.getExtId());
        callbackVO.setAction(callbackActionEnum.name());
        callbackVO.setOs(callUuidContext.getCurrent().getOs());
        callbackVO.setPhase(CallbackPhaseEnum.START_CALL.name());
        callbackVO.setSuccess(true);
        callbackVO.setMsg(msg);
        return callbackVO;
    }

    /**
     * 构建对象-结束通话
     */
    public static ClientCallbackVO buildEnd(CallUuidContext callUuidContext,
                                            CallbackActionEnum callbackActionEnum,
                                            Boolean isNoAnswer) {
        ClientCallbackVO callbackVO = new ClientCallbackVO();
        callbackVO.setReqId(IdUtil.fastUUID());
        callbackVO.setCompanyCode(callUuidContext.getCompanyCode());
        callbackVO.setMsgType(MsgTypeEnum.callback.name());
        callbackVO.setAgentId(callUuidContext.getAgentId());
        callbackVO.setExtId(callUuidContext.getExtId());
        callbackVO.setAction(callbackActionEnum.name());
        callbackVO.setOs(callUuidContext.getCurrent().getOs());
        callbackVO.setPhase(CallbackPhaseEnum.END_CALL.name());
        callbackVO.setSuccess(true);
        if (Boolean.TRUE.equals(isNoAnswer)) {
            callbackVO.setMsg("对方未接通!");
        } else {
            callbackVO.setMsg("对方已挂断!");
        }
        return callbackVO;
    }

    /**
     * 构建对象-结束通话
     */
    public static ClientCallbackVO buildEnd(CallUuidContext callUuidContext,
                                            CallbackActionEnum callbackActionEnum,
                                            Set<String> inCallNumbers) {
        ClientCallbackVO callbackVO = new ClientCallbackVO();
        callbackVO.setReqId(IdUtil.fastUUID());
        callbackVO.setCompanyCode(callUuidContext.getCompanyCode());
        callbackVO.setMsgType(MsgTypeEnum.callback.name());
        callbackVO.setAgentId(callUuidContext.getAgentId());
        callbackVO.setExtId(callUuidContext.getExtId());
        callbackVO.setAction(callbackActionEnum.name());
        callbackVO.setOs(callUuidContext.getCurrent().getOs());
        callbackVO.setPhase(CallbackPhaseEnum.END_CALL.name());
        callbackVO.setInCallNumbers(inCallNumbers);
        callbackVO.setSuccess(true);
        callbackVO.setMsg("三方通话已挂断一方!");
        return callbackVO;
    }

    /**
     * 构建对象-被操作方
     */
    public static ClientCallbackVO buildPassivity(CallUuidContext callUuidContext,
                                                  CallbackActionEnum callbackActionEnum,
                                                  Boolean isNoAnswer) {
        ClientCallbackVO callbackVO = new ClientCallbackVO();
        callbackVO.setReqId(IdUtil.fastUUID());
        callbackVO.setCompanyCode(callUuidContext.getCompanyCode());
        callbackVO.setMsgType(MsgTypeEnum.callback.name());
        callbackVO.setAgentId(callUuidContext.getAgentId());
        callbackVO.setExtId(callUuidContext.getExtId());
        callbackVO.setAction(callbackActionEnum.name());
        callbackVO.setOs(callUuidContext.getCurrent().getOs());
        callbackVO.setPhase(CallbackPhaseEnum.END_CALL.name());
        callbackVO.setSuccess(true);
        if (Boolean.TRUE.equals(isNoAnswer)) {
            callbackVO.setMsg("对方未接通!");
        } else {
            callbackVO.setMsg("对方已挂断!");
        }
        return callbackVO;
    }

    /**
     * 构建对象-音视频切换通知
     */
    public static ClientCallbackVO buildChangeMedia(CallUuidContext callUuidContext,
                                                    CallStatusEventDTO callStatusEventDTO,
                                                    Integer changeMediaFlag,
                                                    String msg) {
        ClientCallbackVO callbackVO = new ClientCallbackVO();
        callbackVO.setReqId(IdUtil.fastUUID());
        callbackVO.setCompanyCode(callUuidContext.getCompanyCode());
        callbackVO.setMsgType(MsgTypeEnum.callback.name());
        callbackVO.setAgentId(callUuidContext.getAgentId());
        callbackVO.setExtId(callUuidContext.getExtId());
        callbackVO.setAction(CallbackActionEnum.CHANGE_MEDIA.name());
        callbackVO.setOs(callUuidContext.getCurrent().getOs());
        callbackVO.setSuccess(true);
        CallStatusEventDTO.EventData eventData = callStatusEventDTO.getData();
        callbackVO.setMsg(msg);
        callbackVO.setAudio(MediaStreamEnum.valueOf(eventData.getAudio()).getCode());
        callbackVO.setLastAudio(MediaStreamEnum.valueOf(eventData.getLastAudio()).getCode());
        callbackVO.setVideo(MediaStreamEnum.valueOf(eventData.getVideo()).getCode());
        callbackVO.setLastVideo(MediaStreamEnum.valueOf(eventData.getLastVideo()).getCode());
        callbackVO.setChangeMediaFlag(changeMediaFlag);
        return callbackVO;
    }
}
