package com.cqt.model.cdr.entity;

import lombok.Data;

@Data
public class RemoteQualityCdr {
    /**
     * 呼叫编号
     */
    private String callId;

    /**
     * 呼叫流水号
     */
    private String sid;

    /**
     * 呼叫方向
     */
    private Integer direction;

    /**
     * 呼叫类型（1.IVR，3：坐席，话单表中还有0未知）
     */
    private String calltype;

    /**
     * 坐席号码
     */
    private String cno;

    /**
     * 原始主叫号码
     */
    private String origcalling;

    /**
     * 原始被叫号码
     */
    private String origcalled;

    /**
     * 转移号码
     */
    private String destnumber;

    /**
     * 转移标识
     */
    private String destsid;

    /**
     * 呼叫开始时间
     */
    private String startTime;

    /**
     * 呼叫应答时间
     */
    private String answerTime;


    /**
     * 呼叫结束时间
     */
    private String endTime;

    /**
     * 振铃时长
     */
    private String ringSeconds;

    /**
     * 主叫号码
     */
    private String callingnumber;

    /**
     * 被叫号码
     */
    private String callednumber;

    /**
     * 通话时长
     */
    private Long bridgeDuration;

    /**
     * 呼叫结果
     */
    private String status;

    /**
     * 挂机方（1：主叫2：被叫）
     */
    private Integer disconnection;


    /**
     * 呼叫结果明细（呼入-IVR溢出、呼入-IVR应答
     */
    private String detail;

    /**
     * 排队时长
     */
    private Long queuesec;

    /**
     * 服务（任务）标识
     */
    private String taskid;

    /**
     * 录音文件
     */
    private String recordFile;

    /**
     * 服务类型
     */
    private String service;

    /**
     * 后处理时间
     */
    private String postprocess;

    /**
     * 透明参数
     */
    private String transparam;

    /**
     * 所在的媒体服务器名称
     */
    private String msserver;

    /**
     * 技能
     */
    private String userId;

    /**
     * 随路数据
     */
    private String calldata;

    /**
     * 区号
     */
    private String areacode;

    /**
     * 单号/流水号
     */
    private String orderid;

//    /**
//     * 跟踪记录
//     */
////    private String orderid;


    private String calledid;

    /**
     *  部门（班组号）
     */
    private String department;

    /**
     *  企业标识
     */
    private String vccid;

    /**
     *  手机前三位
     */
    private String telthree;

    /**
     *  手机前七位
     */
    private String telseven;

    /**
     *  坐席号码
     */
    private String agentNumber;

    /**
     *  客户号码
     */
    private String customerNumber;
}
