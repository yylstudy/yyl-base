package com.cqt.cdr.cloudccsfaftersales.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CallStateDetails {

    /**
     * 企业标识（集团号）
     */
    private String vccid;

    /**
     * 呼叫编号（序号）
     */
    private String callId;

    /**
     * 	呼叫流水号
     */
    private String sid;

    /**
     * 呼叫方向	1：外线呼入2：外线呼出3：内线呼入4：内线呼出(用以生成两字段 cdrType:分为两种【呼入：cdr_ib 、呼出：cdr_ob_agent】callType:分为两种【呼入:1 、 呼出:2】)
     */
    private Integer direction;

    /**
     * 呼叫类型1：IVR 3：Agent（座席）
     */
    private String calltype;

    /**
     * 座席标识	callingid字段(注:agentName为空则使用cno的值)
     */
    private String cno;

    /**
     * 坐席号码	callingnumber字段
     */
    private String agentNumber;

    /**
     * 客户号码	callednumber字段
     */
    private String customerNumber;

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
     * 开始时间 （YYYYMMDDhhmmss） 	starttime字段(to天润时需转为十位时间戳）
     */
    private String startTime;

    /**
     * 应答时间 （YYYYMMDDhhmmss）	answertime字段(to天润时需转为十位时间戳）
     */
    private String answerTime;

    /**
     * 结束时间 （YYYYMMDDhhmmss -> 十位时间戳）	endtime字段(to天润时需转为十位时间戳,结合startTime用以计算totalDuration=endTime-startTime）ringSeconds	extInfo	int	Y	振铃时长	ringseconds字段(用来计算接通时间bridgeTime= startTime+ringseconds)
     */
    private String endTime;

    /**
     * 振铃时长(单位：秒)	ringseconds字段(用来计算接通时间bridgeTime= startTime+ringseconds)
     */
    private String ringSeconds;

    /**
     * 通话时长 (单位：秒)	duration字段
     */
    private Long bridgeDuration;

    /**
     * 接听状态	传1(接通)即可
     */
    private String status;

    /**
     * 挂机方	1：主叫2：被叫(direction结合disconnection生成endReason,0：客户未挂机/坐席挂机 1：客户挂机)
     */
    private Integer disconnection;

    /**
     * 呼叫结果明细
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
     * 录音文件id，srfmsgid字段
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
     * 透明参数,先用于存储工单流水号
     */
    private String transparam;

    /**
     * 所在的媒体服务器名称
     */
    private String msserver;

    /**
     * 部门/班组号，顺丰该通话坐席所在的区域号（由department字段，需要截取）
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
     * 区号或手机前三位
     */
    private String telthree;

    /**
     * 区号或手机前七位
     */
    private String telseven;

    /**
     * 单号/流水号
     */
    private String orderid;
}
