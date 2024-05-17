package com.cqt.model.cdr.entity;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cqt.base.enums.CallRoleEnum;
import com.cqt.base.enums.CallTypeEnum;
import com.cqt.base.enums.MediaStreamEnum;
import com.cqt.base.enums.OutLineEnum;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;

/**
 * 主话单(主叫为准)(CallCenterMainCdr)表实体类
 *
 * @author linshiqiang
 * @since 2023-08-15 17:21:59
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("cloudcc_main_cdr")
public class CallCenterMainCdr {

    /**
     * 自定义主话单id
     */
    @TableId(type = IdType.INPUT)
    private String callId;

    /**
     * fs服务器标识
     */
    private String serviceId;

    /**
     * 主叫侧uuid
     */
    private String uuid;

    /**
     * 客户侧uuid(外呼被叫, 呼入主叫)
     */
    private String clientUuid;

    /**
     * 企业id
     */
    private String companyCode;

    /**
     * 坐席id
     */
    private String agentId;

    /**
     * 分机id
     */
    private String extId;

    /**
     * 技能id
     */
    private String skillId;

    /**
     * 主叫号码(外呼-分机id, 呼入-客户手机号(分机id))
     */
    private String callerNumber;

    /**
     * 外显号码(外呼-企业主显号, 呼入-客户手机号(企业主显号))
     */
    private String displayNumber;

    /**
     * 被叫号码(外呼-客户手机号(分机id), 呼入-企业号码(分机id))
     */
    private String calleeNumber;

    /**
     * 平台号码
     */
    private String platformNumber;

    /**
     * 计费号码
     */
    private String chargeNumber;

    /**
     * 主叫区号
     */
    private String callerAreaCode;

    /**
     * 被叫区号
     */
    private String calleeAreaCode;

    /**
     * 内外线: 0-内线, 1-外线
     */
    private Integer outLine;

    /**
     * 留言(语音信箱)
     * Voice mail
     * 1-有, 0-没有
     */
    private Integer voiceMailFlag;

    /**
     * 是否有转接满意度. 1-有, 0-没有
     */
    private Integer satisfactionFlag;

    /**
     * 呼入ivr
     */
    @TableField("callin_ivr_flag")
    private Integer callinIvrFlag;

    /**
     * 转接ivr
     */
    @TableField("trans_ivr_flag")
    private Integer transIvrFlag;

    /**
     * 呼叫方向(1-呼入inbound, 0-呼出outbound)
     */
    private Integer direction;

    /**
     * 呼入时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date callinTime;

    /**
     * 外呼时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date inviteTime;

    /**
     * 振铃时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date ringTime;

    /**
     * 接通时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date answerTime;

    /**
     * 桥接时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bridgeTime;

    /**
     * 挂断时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date hangupTime;

    /**
     * 开始通话时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date callStartTime;

    /**
     * 结束通话时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date callEndTime;

    /**
     * 开始排队时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startQueueTime;

    /**
     * 结束排队时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endQueueTime;

    /**
     * 开始ivr时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startIvrTime;

    /**
     * 结束ivr时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endIvrTime;

    /**
     * 开始满意度时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startSatisfactionTime;

    /**
     * 开始语音信箱时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date voiceMailStartTime;

    /**
     * 结束语音信箱时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date voiceMailEndTime;

    /**
     * 呼入时间-时间戳
     */
    private Long callinStamp;

    /**
     * 外呼时间-时间戳
     */
    private Long inviteStamp;

    /**
     * 振铃时间-时间戳
     */
    private Long ringStamp;

    /**
     * 桥接时间-时间戳
     */
    private Long bridgeStamp;

    /**
     * 挂断时间-时间戳
     */
    private Long hangupStamp;

    /**
     * 接通时间-时间戳
     */
    private Long answerStamp;

    /**
     * 接通秒数
     */
    private Long answerSecond;

    /**
     * 开始通话时间-时间戳
     */
    private Long callStartStamp;

    /**
     * 结束通话时间-时间戳
     */
    private Long callEndStamp;

    /**
     * 通话时长(ms)(variable_duration) hangup_time - answer_time
     */
    private Long duration;

    /**
     * 挂机方(0-平台释放, 1-主叫释放, 2-被叫释放)
     */
    private Integer releaseDir;

    /**
     * 结束原因值
     */
    private Integer releaseCode;

    /**
     * 结束原因描述
     */
    private String releaseDesc;

    /**
     * 挂断原因(挂断事件)
     */
    private String hangupCause;

    /**
     * 媒体类型(1-audio, 2-video)
     */
    private Integer mediaType;

    /**
     * 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3
     */
    private Integer audio;

    /**
     * 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0
     */
    private Integer video;

    /**
     * 录音url (主叫侧的录音)
     */
    private String recordUrl;

    /**
     * 留言录制url
     */
    private String voiceMailRecordUrl;

    /**
     * 转化对象
     *
     * @param mainContext uuid上下文
     */
    public void convert(CallUuidContext mainContext, CallStatusEventDTO callStatusEventDTO, String platformNumber) {
        CallUuidRelationDTO current = mainContext.getCurrent();
        CallCdrDTO callCdrDTO = current.getCallCdrDTO();
        setCallId(current.getMainCallId());
        setServiceId(callStatusEventDTO.getServerId());
        setUuid(current.getUuid());
        setClientUuid(getClientUUID(mainContext));
        setCompanyCode(current.getCompanyCode());
        setAgentId(current.getAgentId());
        setExtId(current.getExtId());
        if (CallRoleEnum.CLIENT_CALLER.equals(mainContext.getCallRoleEnum())) {
            setAgentId(mainContext.getCurrent().getCallinTransferAgentId());
        }
        setCallerNumber(current.getCallerNumber());
        setDisplayNumber(current.getDisplayNumber());
        setCalleeNumber(current.getCalleeNumber());
        if (StrUtil.isEmpty(current.getPlatformNumber())) {
            setPlatformNumber(platformNumber);
        } else {
            setPlatformNumber(current.getPlatformNumber());
        }
        setCallerAreaCode(null);
        setCalleeAreaCode(null);
        setOutLine(getOutline(mainContext));
        setSatisfactionFlag(Boolean.TRUE.equals(mainContext.getSatisfaction()) ? 1 : 0);
        setCallinIvrFlag(Boolean.TRUE.equals(mainContext.getCallinIVR()) ? 1 : 0);
        setTransIvrFlag(Boolean.TRUE.equals(mainContext.getTransIVR()) ? 1 : 0);
        setDirection(current.getCallDirectionEnum().getCode());
        initMediaParam(mainContext, callStatusEventDTO);
        // 话机状态时间
        initCdrTime(callCdrDTO);
        // 排队参数
        initQueueParam(mainContext);
        // 结束原因
        initReleaseParam(mainContext, callStatusEventDTO);

        // 留言
        setVoiceMailFlag(0);
        if (Boolean.TRUE.equals(mainContext.getVoiceMailFlag())) {
            setVoiceMailFlag(1);
            setVoiceMailStartTime(mainContext.getVoiceMailStartTime());
            if (Objects.nonNull(mainContext.getVoiceMailEndTime())) {
                setVoiceMailEndTime(mainContext.getVoiceMailEndTime());
            } else {
                if (Objects.nonNull(callCdrDTO.getHangupTimestamp())) {
                    setVoiceMailEndTime(DateUtil.date(callCdrDTO.getHangupTimestamp()));
                }
            }
            setRecordUrl(mainContext.getRecordFileName());
        }
    }

    private void initMediaParam(CallUuidContext mainContext, CallStatusEventDTO callStatusEventDTO) {
        Integer audio = mainContext.getCurrent().getAudio();
        Integer video = mainContext.getCurrent().getVideo();
        setMediaType(1);
        if (MediaStreamEnum.SENDRECV.getCode().equals(video)) {
            setMediaType(2);
        }
        setAudio(audio);
        setVideo(video);
    }

    private void initReleaseParam(CallUuidContext mainContext, CallStatusEventDTO callStatusEventDTO) {
        if (Objects.nonNull(mainContext.getCurrent().getReleaseDir())) {
            setReleaseDir(mainContext.getCurrent().getReleaseDir().getCode());
        }
        setReleaseCode(null);
        setReleaseDesc(null);
        setHangupCause(callStatusEventDTO.getData().getHangupCause());
        String recordFileName = mainContext.getCallCdrDTO().getRecordFileName();
        if (StrUtil.isEmpty(recordFileName)) {
            setRecordUrl(mainContext.getRecordFileName());
            return;
        }
        setRecordUrl(recordFileName);
    }

    private void initQueueParam(CallUuidContext mainContext) {
        UserQueueUpDTO userQueueUpDTO = mainContext.getUserQueueUpDTO();
        if (Objects.nonNull(userQueueUpDTO)) {
            if (Objects.nonNull(userQueueUpDTO.getFirstTimestamp())) {
                setStartQueueTime(DateUtil.date(userQueueUpDTO.getFirstTimestamp()));
                Long successTimestamp = userQueueUpDTO.getSuccessTimestamp();
                if (Objects.nonNull(successTimestamp)) {
                    setEndQueueTime(DateUtil.date(successTimestamp));
                    setEndIvrTime(DateUtil.date(successTimestamp));
                } else {
                    CallCdrDTO callCdrDTO = mainContext.getCallCdrDTO();
                    setEndQueueTime(DateUtil.date(callCdrDTO.getHangupTimestamp()));
                    setEndIvrTime(DateUtil.date(callCdrDTO.getHangupTimestamp()));
                }
            }
            setSkillId(userQueueUpDTO.getSkillId());
        }
        setStartIvrTime(mainContext.getStartIvrTime());
        setStartSatisfactionTime(mainContext.getStartSatisfactionTime());
    }

    private void initCdrTime(CallCdrDTO callCdrDTO) {
        if (Objects.nonNull(callCdrDTO.getCalInTimestamp())) {
            setCallinStamp(callCdrDTO.getCalInTimestamp());
            setCallinTime(DateUtil.date(callCdrDTO.getCalInTimestamp()));
        }
        if (Objects.nonNull(callCdrDTO.getInviteTimestamp())) {
            setInviteStamp(callCdrDTO.getInviteTimestamp());
            setInviteTime(DateUtil.date(callCdrDTO.getInviteTimestamp()));
        }
        if (Objects.nonNull(callCdrDTO.getRingTimestamp())) {
            setRingStamp(callCdrDTO.getRingTimestamp());
            setRingTime(DateUtil.date(callCdrDTO.getRingTimestamp()));
        }
        if (Objects.nonNull(callCdrDTO.getAnswerTimestamp())) {
            setAnswerStamp(callCdrDTO.getAnswerTimestamp());
            setAnswerTime(DateUtil.date(callCdrDTO.getAnswerTimestamp()));
            setCallStartStamp(getAnswerStamp());
            setCallStartTime(getAnswerTime());
            if (Objects.nonNull(callCdrDTO.getRingTimestamp())) {
                setAnswerSecond((callCdrDTO.getAnswerTimestamp() - callCdrDTO.getRingTimestamp()) / 1000);
            } else {
                setAnswerSecond(0L);
            }
        }
        if (Objects.nonNull(callCdrDTO.getBridgeTimestamp())) {
            setBridgeStamp(callCdrDTO.getBridgeTimestamp());
            setBridgeTime(DateUtil.date(callCdrDTO.getBridgeTimestamp()));
        }
        if (Objects.nonNull(callCdrDTO.getHangupTimestamp())) {
            setHangupStamp(callCdrDTO.getHangupTimestamp());
            setHangupTime(DateUtil.date(callCdrDTO.getHangupTimestamp()));
            setCallEndStamp(getHangupStamp());
            setCallEndTime(getHangupTime());
            if (Objects.nonNull(callCdrDTO.getAnswerTimestamp())) {
                setDuration(callCdrDTO.getHangupTimestamp() - callCdrDTO.getAnswerTimestamp());
            }
        }
    }

    private Integer getOutline(CallUuidContext mainContext) {
        CallTypeEnum callTypeEnum = mainContext.getCurrent().getCallTypeEnum();
        return CallTypeEnum.AGENT.equals(callTypeEnum) ? OutLineEnum.IN_LINE.getCode() : OutLineEnum.OUT_LINE.getCode();
    }

    private String getClientUUID(CallUuidContext mainContext) {
        if (CallRoleEnum.CLIENT_CALLER.equals(mainContext.getCurrent().getCallRoleEnum())) {
            return mainContext.getCurrent().getUuid();
        }
        if (Objects.nonNull(mainContext.getBridgeUUID())) {
            return mainContext.getBridgeUUID();
        }
        return "";
    }
}

