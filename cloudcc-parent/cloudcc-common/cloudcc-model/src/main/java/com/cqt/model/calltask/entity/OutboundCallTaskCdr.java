package com.cqt.model.calltask.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 外呼任务话单 (OutboundCallTaskCdr)表实体类
 *
 * @author linshiqiang
 * @since 2023-10-24 14:05:31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("cloudcc_outbound_call_task_cdr")
public class OutboundCallTaskCdr {

    /**
     * 话单id
     */
    @TableId(type = IdType.INPUT)
    private Long callId;

    /**
     * 企业id
     */
    private String companyCode;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 任务类型
     *
     * @see com.cqt.base.enums.calltask.CallTaskEnum
     */
    private Integer taskType;

    /**
     * 关联主话单
     */
    private String mainCallId;

    /**
     * 子话单id
     */
    private String subUuid;

    /**
     * 号码id
     */
    private String numberId;

    /**
     * 主叫号码
     */
    private String callerNumber;

    /**
     * 外显号码(平台号码)
     */
    private String displayNumber;

    /**
     * 被叫号码(客户号码)
     */
    private String clientNumber;

    /**
     * 通话开始时间
     */
    private Date callStartTime;

    /**
     * 客户接通时间
     */
    private Date clientAnswerTime;

    /**
     * 坐席接通时间
     */
    private Date agentAnswerTime;

    /**
     * 挂断时间
     */
    private Date hangupTime;

    /**
     * 通话时长
     */
    private Long duration;

    /**
     * 坐席id
     */
    private String agentId;

    /**
     * 当前呼叫次数
     */
    private Integer currentTimes;

    /**
     * 接通状态 1-已接通 0 -未接通
     */
    private Integer answerStatus;

    /**
     * 失败原因
     */
    private String failCause;

    /**
     * 录音地址
     */
    private String recordUrl;

    private String serviceId;
}

