package com.cqt.model.cdr.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ld
 * @since 2023-08-15
 */
@Getter
@Setter
@TableName("cloudcc_cdr_channeldata")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CdrChanneldata implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 变更主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;

    @JsonProperty("FreeSWITCH-Switchname")
    @TableField(exist = false)
    private String freeSwitchName;


    private String callId;


    /**
     * 主话单uuid
     */
    private String uuid;


    /**
     * 主话单的clientUuid
     */
    private String clientUuid;


    @JsonProperty("direction")
    private Integer direction;

    /**
     * 最后的IVR标识
     */
    @JsonProperty("variable_ivridE")
    private String ivridE;

    /**
     * 满意度的IVR标识
     */
    @JsonProperty("variable_satisfactionIvrid")
    private String satisfactionIvrid;

    /**
     * 满意度1
     */
    @JsonProperty("variable_satisfactionF1")
    private String satisfactionF1;

    /**
     * 满意度2
     */
    @JsonProperty("variable_satisfactionF2")
    private String satisfactionF2;

    /**
     * 满意度3
     */
    @JsonProperty("variable_satisfactionF3")
    private String satisfactionF3;

    /**
     * 满意度4
     */
    @JsonProperty("variable_satisfactionF4")
    private String satisfactionF4;

    /**
     * 满意度5
     */
    @JsonProperty("variable_satisfactionF5")
    private String satisfactionF5;

    /**
     * 满意度6
     */
    @JsonProperty("variable_satisfactionF6")
    private String satisfactionF6;

    /**
     * 满意度7
     */
    @JsonProperty("variable_satisfactionF7")
    private String satisfactionF7;

    /**
     * 满意度8
     */
    @JsonProperty("variable_satisfactionF8")
    private String satisfactionF8;

    /**
     * 满意度9
     */
    @JsonProperty("variable_satisfactionF9")
    private String satisfactionF9;

    /**
     * 满意度10
     */
    @JsonProperty("variable_satisfactionF10")
    private String satisfactionF10;

    /**
     * 用户按键1
     */
    @JsonProperty("variable_userkeyF1")
    private String userkeyF1;

    /**
     * 用户按键2
     */
    @JsonProperty("variable_userkeyF2")
    private String userkeyF2;

    /**
     * 用户按键3
     */
    @JsonProperty("variable_userkeyF3")
    private String userkeyF3;

    /**
     * 用户按键4
     */
    @JsonProperty("variable_userkeyF4")
    private String userkeyF4;

    /**
     * 用户按键5
     */
    @JsonProperty("variable_userkeyF5")
    private String userkeyF5;

    /**
     * 用户按键6
     */
    @JsonProperty("variable_userkeyF6")
    private String userkeyF6;

    /**
     * 用户按键7
     */
    @JsonProperty("variable_userkeyF7")
    private String userkeyF7;

    /**
     * 用户按键8
     */
    @JsonProperty("variable_userkeyF8")
    private String userkeyF8;

    /**
     * 用户按键9
     */
    @JsonProperty("variable_userkeyF9")
    private String userkeyF9;

    /**
     * 用户按键10
     */
    @JsonProperty("variable_userkeyF10")
    private String userkeyF10;

    /**
     * 生成话单通道 方
     */
    @JsonProperty("variable_shortChannelName")
    private String shortChannelName;

    /**
     * 企业标识
     */
    @JsonProperty("variable_company_code")
    private String companyCode;

    /**
     * 是否已质检
     */
    @JsonProperty("ifqa")
    private Integer ifqa;

    /**
     * 绑定的工单
     */
    @JsonProperty("caseid")
    private String caseid;

    /**
     * 绑定的客户
     */
    @JsonProperty("cusid")
    private String cusid;

    /**
     * 是否听过留言
     */
    @JsonProperty("ifplayedleavemsg")
    private Integer ifplayedleavemsg;

    /**
     * ivr轨迹
     */
    @JsonProperty("variable_ivr_tracks")
    private String ivrTracks;

    /**
     * 队列标识，1进队列、0出队列、空没触发队列节点
     */
    @JsonProperty("variable_cc_queue_hangup")
    private String ccQueueHangup;

    /**
     * 第一次进队列开始时间
     */
    @JsonProperty("variable_runcc_times")
    private String runccTimes;

    /**
     * 排队次数
     */
    @JsonProperty("variable_cc_busy_no")
    private String ccBusyNo;


    @JsonProperty("ctiCallid")
    private String ctiCallid;


    @JsonProperty("ivrParametersData")
    private String ivrParametersData;

    /**
     * 创建时间（主话单的call_end_time）
     */
    private Date createTime;


    @JsonProperty("variable_lcc_acd_server")
    @TableField(exist = false)
    private String lccAcdServer;


    @JsonProperty("variable_mainMenuPushKey")
    private String mainMenuPushKey;


    @JsonProperty("variable_secMenuPushKey")
    private String secMenuPushKey;
}
