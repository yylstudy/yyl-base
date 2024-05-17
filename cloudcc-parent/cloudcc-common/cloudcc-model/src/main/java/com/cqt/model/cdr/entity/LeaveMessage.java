package com.cqt.model.cdr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName cloudcc_leave_message
 */
@TableName(value = "cloudcc_leave_message")
@Data
public class LeaveMessage implements Serializable {
    private static final long serialVersionUID = 1L;

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

    /*
     * 回呼坐席
     * */
    private String callbackAgent;

    /*
     * 回呼状态
     * */
    private Integer callbackStatus;
}
