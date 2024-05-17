package com.cqt.model.cdr.entity;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cqt.base.enums.*;
import com.cqt.base.enums.cdr.Da2ResultEnum;
import com.cqt.base.enums.cdr.ReleaseDirEnum;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;

/**
 * 子话单(被叫为准)(CallCenterSubCdr)表实体类
 *
 * @author linshiqiang
 * @since 2023-08-15 17:22:00
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
@TableName("cloudcc_sub_cdr")
public class CallCenterSubCdr {

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
     * 客户呼入技能id
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
     * 被叫号码，即用户拨叫的号码
     * 对于主叫话单，填写主叫实际拨打的号码
     * 对于被叫话单，填写被叫计费的16位分机号
     * 对于前转话单，填写前转流程处理前的被叫号码
     */
    @TableField("called_party_number")
    private String calledPartyNumber;

    /**
     * 主叫号码
     * 对于主叫话单，填写主叫计费的16位分机号
     * 对于被叫话单，填写主叫号码，即INVITE的PAI消息头，如果PAI消息头为空，则填写INVITE的From消息头
     * 对于前转话单，填写前转计费的16位分机号
     * 对于被叫话单，填写主叫号码，即INVITE的PAI消息头，如果PAI消息头为空，则填写INVITE的From消息头
     * 对于前转话单，填写前转计费的16位分机号
     */
    @TableField("calling_party_number")
    private String callingPartyNumber;

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
     * 是否有满意度. 1-有, 0-没有
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
     * A路uuid
     */
    @TableField("a_uuid")
    private String aUuid;

    /**
     * A路 话务类型 agent-0 client-1
     */
    @TableField("a_call_type")
    private Integer aCallType;

    /**
     * A路呼入时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("a_callin_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date aCallinTime;

    /**
     * A路外呼时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("a_invite_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date aInviteTime;

    /**
     * A路振铃时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("a_ring_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date aRingTime;

    /**
     * A路接通时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("a_answer_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date aAnswerTime;

    /**
     * A路桥接时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("a_bridge_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date aBridgeTime;

    /**
     * A路挂断时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("a_hangup_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date aHangupTime;

    /**
     * A路开始通话时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("a_call_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date aCallStartTime;

    /**
     * A路结束通话时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("a_call_end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date aCallEndTime;

    /**
     * A路呼入时间-时间戳
     */
    @TableField("a_callin_stamp")
    private Long aCallinStamp;

    /**
     * A路外呼时间-时间戳
     */
    @TableField("a_invite_stamp")
    private Long aInviteStamp;

    /**
     * A路振铃时间-时间戳
     */
    @TableField("a_ring_stamp")
    private Long aRingStamp;

    /**
     * A路接通时间-时间戳
     */
    @TableField("a_answer_stamp")
    private Long aAnswerStamp;

    /**
     * A路桥接时间-时间戳
     */
    @TableField("a_bridge_stamp")
    private Long aBridgeStamp;

    /**
     * A路挂断时间-时间戳
     */
    @TableField("a_hangup_stamp")
    private Long aHangupStamp;

    /**
     * A路接通秒数
     */
    @TableField("a_answer_second")
    private Long aAnswerSecond;

    /**
     * A路开始通话时间-时间戳
     */
    @TableField("a_call_start_stamp")
    private Long aCallStartStamp;

    /**
     * A路结束通话时间-时间戳
     */
    @TableField("a_call_end_stamp")
    private Long aCallEndStamp;

    /**
     * A路通话时长(variable_duration) hangup_time - answer_time
     */
    @TableField("a_duration")
    private Long aDuration;

    /**
     * B路uuid
     */
    @TableField("b_uuid")
    private String bUuid;

    /**
     * B路 话务类型 agent-0 client-1
     */
    @TableField("b_call_type")
    private Integer bCallType;

    /**
     * B路呼入时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("b_callin_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bCallinTime;

    /**
     * B路外呼时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("b_invite_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bInviteTime;

    /**
     * B路振铃时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("b_ring_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bRingTime;

    /**
     * B路接通时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("b_answer_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bAnswerTime;

    /**
     * B路桥接时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("b_bridge_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bBridgeTime;

    /**
     * B路挂断时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("b_hangup_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bHangupTime;

    /**
     * B路开始通话时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("b_call_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bCallStartTime;

    /**
     * B路结束通话时间 (yyyy-MM-dd HH:mm:ss)
     */
    @TableField("b_call_end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bCallEndTime;

    /**
     * B路呼入时间-时间戳
     */
    @TableField("b_callin_stamp")
    private Long bCallinStamp;

    /**
     * B路外呼时间-时间戳
     */
    @TableField("b_invite_stamp")
    private Long bInviteStamp;

    /**
     * B路振铃时间-时间戳
     */
    @TableField("b_ring_stamp")
    private Long bRingStamp;

    /**
     * B路接通时间-时间戳
     */
    @TableField("b_answer_stamp")
    private Long bAnswerStamp;

    /**
     * B路桥接时间-时间戳
     */
    @TableField("b_bridge_stamp")
    private Long bBridgeStamp;

    /**
     * B路挂断时间-时间戳
     */
    @TableField("b_hangup_stamp")
    private Long bHangupStamp;

    /**
     * B路接通秒数
     */
    @TableField("b_answer_second")
    private Long bAnswerSecond;

    /**
     * B路开始通话时间-时间戳
     */
    @TableField("b_call_start_stamp")
    private Long bCallStartStamp;

    /**
     * B路结束通话时间-时间戳
     */
    @TableField("b_call_end_stamp")
    private Long bCallEndStamp;

    /**
     * B路通话时长(variable_duration) hangup_time - answer_time
     */
    @TableField("b_duration")
    private Long bDuration;

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
     * 排队时长(s)
     */
    private Long queueDuration;

    /**
     * 排队次数
     */
    private Integer queueCount;

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
     * 话单类型(0-外呼, 1-呼入, 2-监听, 3-耳语, 4-咨询, 5-转接, 6-代接, 7-三方通话, 8-强插)
     */
    private Integer cdrType;

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

    @TableField("ws_rtsp")
    private String wsRtsp;

    @TableField("record_url_rtsp")
    private String recordUrlRtsp;

    @TableField("absolute_url")
    private String absoluteUrl;

    @TableField("record_url_in")
    private String recordUrlIn;

    /**
     * ivr呼入 未转接坐席挂断 对象转化
     *
     * @param sourceContext      主叫
     * @param callStatusEventDTO 挂断事件
     * @param platformNumber       平台号码
     */
    public void convertCaller(CallUuidContext sourceContext, CallStatusEventDTO callStatusEventDTO, String platformNumber) {
        setUuid(sourceContext.getUUID());
        setServiceId(callStatusEventDTO.getServerId());
        setCallId(sourceContext.getMainCallId());
        setCompanyCode(sourceContext.getCompanyCode());
        setDirection(sourceContext.getCallDirection().getCode());
        setCallerNumber(sourceContext.getCallerNumber());
        setDisplayNumber(sourceContext.getDisplayNumber());
        setCalleeNumber(sourceContext.getCalleeNumber());
        setPlatformNumber(platformNumber);
        setCdrType(sourceContext.getCallRoleEnum().getCode());
        setOutLine(getOutline(sourceContext));

        initSourceParams(sourceContext);

        iniQueueParams(sourceContext, null);
        if (Objects.isNull(getEndIvrTime())) {
            setEndIvrTime(getAHangupTime());
        }

        initMediaParam(sourceContext);
        setReleaseDir(ReleaseDirEnum.CALLER.getCode());
        if (Objects.nonNull(sourceContext.getCurrent().getReleaseDir())) {
            setReleaseDir(sourceContext.getCurrent().getReleaseDir().getCode());
        }
        setReleaseCode(Da2ResultEnum.CALLER_RELEASE.getCode());
        setReleaseDesc(Da2ResultEnum.CALLER_RELEASE.getName());
        setHangupCause(sourceContext.getCallCdrDTO().getHangupCause());
        setRecordUrl(sourceContext.getCallCdrDTO().getRecordFileName());
    }

    /**
     * 对象转化
     *
     * @param sourceContext 主叫
     * @param destContext   被叫
     * @param callStatusEventDTO 挂断事件
     */
    public void convert(CallUuidContext sourceContext,
                        CallUuidContext destContext,
                        CallStatusEventDTO callStatusEventDTO,
                        String platformNumber) {
        setUuid(destContext.getCurrent().getUuid());
        setServiceId(callStatusEventDTO.getServerId());
        setCallId(sourceContext.getMainCallId());
        setCompanyCode(sourceContext.getCompanyCode());
        setDirection(destContext.getCallDirection().getCode());
        setCallerNumber(destContext.getCallerNumber());
        setDisplayNumber(destContext.getDisplayNumber());
        setCalleeNumber(destContext.getCalleeNumber());
        setPlatformNumber(platformNumber);
        setCdrType(destContext.getCallRoleEnum().getCode());
        setOutLine(getOutline(destContext));
        initAgentParams(sourceContext, destContext);

        initSourceParams(sourceContext);

        iniDestParams(destContext);

        iniQueueParams(sourceContext, destContext);

        initMediaParam(destContext);

        initReleaseParam(sourceContext, destContext);
    }

    private void initMediaParam(CallUuidContext callUuidContext) {
        Integer audio = callUuidContext.getAudio();
        Integer video = callUuidContext.getVideo();
        setMediaType(1);
        if (MediaStreamEnum.SENDRECV.getCode().equals(video)) {
            setMediaType(2);
        }
        setAudio(audio);
        setVideo(video);
    }

    private void initAgentParams(CallUuidContext sourceContext, CallUuidContext destContext) {
        CallDirectionEnum callDirection = sourceContext.getCallDirection();
        if (CallDirectionEnum.INBOUND.equals(callDirection)) {
            setAgentId(destContext.getAgentId());
            setExtId(destContext.getExtId());
            if (StrUtil.isEmpty(destContext.getAgentId())) {
                if (CallRoleEnum.CLIENT_CALLER.equals(sourceContext.getCallRoleEnum())) {
                    setAgentId(sourceContext.getCurrent().getCallinTransferAgentId());
                }
            }
        } else {
            setAgentId(sourceContext.getAgentId());
            setExtId(sourceContext.getExtId());
        }

    }

    private void initReleaseParam(CallUuidContext sourceContext, CallUuidContext destContext) {
        CallCdrDTO sourceCallCdrDTO = sourceContext.getCallCdrDTO();
        CallCdrDTO destCallCdrDTO = destContext.getCallCdrDTO();
        if (Objects.nonNull(sourceCallCdrDTO) && Objects.nonNull(destCallCdrDTO)) {
            Long sourceHangupTimestamp = sourceCallCdrDTO.getHangupTimestamp();
            Long destHangupTimestamp = destCallCdrDTO.getHangupTimestamp();
            if (Objects.nonNull(sourceHangupTimestamp) && Objects.nonNull(destHangupTimestamp)) {
                if (sourceHangupTimestamp < destHangupTimestamp) {
                    setReleaseDir(ReleaseDirEnum.CALLER.getCode());
                } else {
                    setReleaseDir(ReleaseDirEnum.CALLED.getCode());
                }
            }
        }
        // 空号检测
        da2Result(destCallCdrDTO, sourceCallCdrDTO);

        setHangupCause(destCallCdrDTO.getHangupCause());
        setRecordUrl(destCallCdrDTO.getRecordFileName());
    }

    private void da2Result(CallCdrDTO destCallCdrDTO, CallCdrDTO sourceCallCdrDTO) {
        if (Objects.nonNull(destCallCdrDTO)) {
            String destDa2Result = destCallCdrDTO.getDa2Result();
            if (StrUtil.isNotEmpty(destDa2Result)) {
                setReleaseCode(Da2ResultEnum.getResultCode(destDa2Result));
                setReleaseDesc(destDa2Result);
                return;
            }
        }
        if (Objects.nonNull(sourceCallCdrDTO)) {
            String sourceDa2Result = sourceCallCdrDTO.getDa2Result();
            if (StrUtil.isNotEmpty(sourceDa2Result)) {
                setReleaseCode(Da2ResultEnum.getResultCode(sourceDa2Result));
                setReleaseDesc(sourceDa2Result);
                return;
            }
        }
        setReleaseCode(Da2ResultEnum.NORMAL.getCode());
        setReleaseDesc(Da2ResultEnum.NORMAL.getName());
    }

    private void initSourceParams(CallUuidContext sourceContext) {
        setAUuid(sourceContext.getCurrent().getUuid());
        setACallType(sourceContext.getCallTypeEnum().getCode());
        CallCdrDTO callCdrDTO = sourceContext.getCallCdrDTO();
        if (Objects.nonNull(callCdrDTO.getCalInTimestamp())) {
            setACallinStamp(callCdrDTO.getCalInTimestamp());
            setACallinTime(DateUtil.date(callCdrDTO.getCalInTimestamp()));
        }
        if (Objects.nonNull(callCdrDTO.getInviteTimestamp())) {
            setAInviteStamp(callCdrDTO.getInviteTimestamp());
            setAInviteTime(DateUtil.date(callCdrDTO.getInviteTimestamp()));
        }
        if (Objects.nonNull(callCdrDTO.getRingTimestamp())) {
            setARingStamp(callCdrDTO.getRingTimestamp());
            setARingTime(DateUtil.date(callCdrDTO.getRingTimestamp()));
        }
        if (Objects.nonNull(callCdrDTO.getAnswerTimestamp())) {
            setAAnswerStamp(callCdrDTO.getAnswerTimestamp());
            setAAnswerTime(DateUtil.date(callCdrDTO.getAnswerTimestamp()));
            setACallStartStamp(getAAnswerStamp());
            setACallStartTime(getAAnswerTime());
            if (Objects.nonNull(callCdrDTO.getRingTimestamp())) {
                setAAnswerSecond((callCdrDTO.getAnswerTimestamp() - callCdrDTO.getRingTimestamp()) / 1000);
            } else {
                setAAnswerSecond(0L);
            }
        }
        if (Objects.nonNull(callCdrDTO.getBridgeTimestamp())) {
            setABridgeStamp(callCdrDTO.getBridgeTimestamp());
            setABridgeTime(DateUtil.date(callCdrDTO.getBridgeTimestamp()));
        }
        if (Objects.nonNull(callCdrDTO.getHangupTimestamp())) {
            setAHangupStamp(callCdrDTO.getHangupTimestamp());
            setAHangupTime(DateUtil.date(callCdrDTO.getHangupTimestamp()));
            setACallEndStamp(getAHangupStamp());
            setACallEndTime(getAHangupTime());
            if (Objects.nonNull(callCdrDTO.getAnswerTimestamp())) {
                setADuration(callCdrDTO.getHangupTimestamp() - callCdrDTO.getAnswerTimestamp());
            }
        }
    }

    private void iniDestParams(CallUuidContext destContext) {
        setBUuid(destContext.getCurrent().getUuid());
        setBCallType(destContext.getCallTypeEnum().getCode());
        CallCdrDTO callCdrDTO = destContext.getCallCdrDTO();
        if (Objects.nonNull(callCdrDTO.getCalInTimestamp())) {
            setBCallinStamp(callCdrDTO.getCalInTimestamp());
            setBCallinTime(DateUtil.date(callCdrDTO.getCalInTimestamp()));
        }
        if (Objects.nonNull(callCdrDTO.getInviteTimestamp())) {
            setBInviteStamp(callCdrDTO.getInviteTimestamp());
            setBInviteTime(DateUtil.date(callCdrDTO.getInviteTimestamp()));
        }
        if (Objects.nonNull(callCdrDTO.getRingTimestamp())) {
            setBRingStamp(callCdrDTO.getRingTimestamp());
            setBRingTime(DateUtil.date(callCdrDTO.getRingTimestamp()));
        }
        if (Objects.nonNull(callCdrDTO.getAnswerTimestamp())) {
            setBAnswerStamp(callCdrDTO.getAnswerTimestamp());
            setBAnswerTime(DateUtil.date(callCdrDTO.getAnswerTimestamp()));
            setBCallStartStamp(getBAnswerStamp());
            setBCallStartTime(getBAnswerTime());
            if (Objects.nonNull(callCdrDTO.getRingTimestamp())) {
                setBAnswerSecond((callCdrDTO.getAnswerTimestamp() - callCdrDTO.getRingTimestamp()) / 1000);
            } else {
                setBAnswerSecond(0L);
            }
        }
        if (Objects.nonNull(callCdrDTO.getBridgeTimestamp())) {
            setBBridgeStamp(callCdrDTO.getBridgeTimestamp());
            setBBridgeTime(DateUtil.date(callCdrDTO.getBridgeTimestamp()));
        }
        if (Objects.nonNull(callCdrDTO.getHangupTimestamp())) {
            setBHangupStamp(callCdrDTO.getHangupTimestamp());
            setBHangupTime(DateUtil.date(callCdrDTO.getHangupTimestamp()));
            setBCallEndStamp(getBHangupStamp());
            setBCallEndTime(getBHangupTime());
            if (Objects.nonNull(callCdrDTO.getAnswerTimestamp())) {
                setBDuration(callCdrDTO.getHangupTimestamp() - callCdrDTO.getAnswerTimestamp());
            }
        }
    }

    private void iniQueueParams(CallUuidContext sourceContext, CallUuidContext destContext) {
        if (Objects.isNull(sourceContext)) {
            return;
        }
        CallDirectionEnum callDirectionEnum = sourceContext.getCallDirection();
        if (CallDirectionEnum.INBOUND.equals(callDirectionEnum)) {
            // ivr参数
            setStartIvrTime(sourceContext.getStartIvrTime());
            // 排队参数
            UserQueueUpDTO userQueueUpDTO = sourceContext.getUserQueueUpDTO();
            if (Objects.nonNull(userQueueUpDTO)) {
                setSkillId(userQueueUpDTO.getSkillId());
                setStartQueueTime(DateUtil.date(userQueueUpDTO.getFirstTimestamp()));
                if (Objects.nonNull(userQueueUpDTO.getSuccessTimestamp())) {
                    DateTime endQueueTime = DateUtil.date(userQueueUpDTO.getSuccessTimestamp());
                    setEndIvrTime(endQueueTime);
                    setEndQueueTime(endQueueTime);
                    setQueueDuration(DateUtil.between(getStartQueueTime(), getEndQueueTime(), DateUnit.SECOND));
                } else {
                    CallCdrDTO callCdrDTO = sourceContext.getCallCdrDTO();
                    DateTime hangupTime = DateUtil.date(callCdrDTO.getHangupTimestamp());
                    setEndIvrTime(hangupTime);
                    setEndQueueTime(hangupTime);
                    setQueueDuration(DateUtil.between(getStartQueueTime(), getEndQueueTime(), DateUnit.SECOND));
                }
                setQueueCount(userQueueUpDTO.getCurrentTimes());
            }
            if (Boolean.TRUE.equals(sourceContext.getCallinIVR())) {
                setCallinIvrFlag(1);
                setClientUuid(sourceContext.getUUID());
            } else {
                setCallinIvrFlag(0);
            }
            setSatisfactionFlag(Boolean.TRUE.equals(sourceContext.getSatisfaction()) ? 1 : 0);
            setStartSatisfactionTime(sourceContext.getStartSatisfactionTime());
            setTransIvrFlag(Boolean.TRUE.equals(sourceContext.getTransIVR()) ? 1 : 0);
            return;
        }
        if (Objects.isNull(destContext)) {
            return;
        }
        setSatisfactionFlag(Boolean.TRUE.equals(destContext.getSatisfaction()) ? 1 : 0);
        setStartSatisfactionTime(destContext.getStartSatisfactionTime());
        setTransIvrFlag(Boolean.TRUE.equals(destContext.getTransIVR()) ? 1 : 0);
        if (CallTypeEnum.CLIENT.equals(destContext.getCurrent().getCallTypeEnum())) {
            setClientUuid(destContext.getCurrent().getUuid());
        }
    }

    private Integer getOutline(CallUuidContext destContext) {
        if (Objects.isNull(destContext)) {
            return null;
        }
        CallTypeEnum callTypeEnum = destContext.getCallTypeEnum();
        return CallTypeEnum.AGENT.equals(callTypeEnum) ? OutLineEnum.IN_LINE.getCode() : OutLineEnum.OUT_LINE.getCode();
    }
}

