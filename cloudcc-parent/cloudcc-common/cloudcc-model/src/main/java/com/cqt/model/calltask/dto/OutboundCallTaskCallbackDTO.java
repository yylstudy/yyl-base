package com.cqt.model.calltask.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author linshiqiang
 * date:  2023-10-20 15:36
 * 外呼任务执行结果 回调通知
 */
@Data
public class OutboundCallTaskCallbackDTO {

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 企业id
     */
    private String companyCode;

    /**
     * 关联主话单
     */
    private String mainId;

    /**
     * 子话单id
     */
    private String subUuid;

    /**
     * 通话id
     */
    private String uuid;

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
     * 挂机时间
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
     * 状态
     * 客户接通为成功，客户未接通为失败，其余都显示未开始；
     */
    private Integer state;

    /**
     * 失败原因
     * 客户未接、系统原因、号码被禁用（即企业号码管理内将号码被禁用）
     */
    private String failCause;
}
