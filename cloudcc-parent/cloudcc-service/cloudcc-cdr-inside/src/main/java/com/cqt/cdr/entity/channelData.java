package com.cqt.cdr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * (channelData)表实体类
 *
 * @author xinson
 * @since 2023-08-10 11:25:27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("cc_cdr_channeldata")
public class channelData implements Serializable {

    private static final long serialVersionUID = -6952884878306698799L;

    /**
     * 变更主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;

    /**
     * 自定义主话单id
     */
    @TableField("call_id")
    private String callId;

//  暂时不确定
//    /**
//     * auuid
//     */
//    @TableField("auuid")
//    private String auuid;
//
//    /**
//     * buuid
//     */
//    @TableField("buuid")
//    private String buuid;

    /**
     * 最后的IVR标识
     */
    @TableField("ivrid_e")
    private String ivridE;

    /**
     * 满意度的IVR标识
     */
    @TableField("satisfaction_ivrid")
    private Long satisfactionIvrid;

    /**
     * 满意度1
     */
    @TableField("satisfaction_f1")
    private String satisfactionF1;

    /**
     * 满意度2
     */
    @TableField("satisfaction_f2")
    private String satisfactionF2;

    /**
     * 满意度3
     */
    @TableField("satisfaction_f3")
    private String satisfactionF3;

    /**
     * 满意度4
     */
    @TableField("satisfaction_f4")
    private String satisfactionF4;

    /**
     * 满意度5
     */
    @TableField("satisfaction_f5")
    private String satisfactionF5;

    /**
     * 满意度6
     */
    @TableField("satisfaction_f6")
    private String satisfactionF6;

    /**
     * 满意度7
     */
    @TableField("satisfaction_f7")
    private String satisfactionF7;

    /**
     * 满意度8
     */
    @TableField("satisfaction_f8")
    private String satisfactionF8;

    /**
     * 满意度9
     */
    @TableField("satisfaction_f9")
    private String satisfactionF9;

    /**
     * 满意度10
     */
    @TableField("satisfaction_f10")
    private String satisfactionF10;

    /**
     * 用户按键1
     */
    @TableField("userkey_f1")
    private String userkeyF1;

    /**
     * 用户按键2
     */
    @TableField("userkey_f2")
    private String userkeyF2;

    /**
     * 用户按键3
     */
    @TableField("userkey_f3")
    private String userkeyF3;

    /**
     * 用户按键4
     */
    @TableField("userkey_f4")
    private String userkeyF4;

    /**
     * 用户按键5
     */
    @TableField("userkey_f5")
    private String userkeyF5;

    /**
     * 用户按键6
     */
    @TableField("userkey_f6")
    private String userkeyF6;

    /**
     * 用户按键7
     */
    @TableField("userkey_f7")
    private String userkeyF7;

    /**
     * 用户按键8
     */
    @TableField("userkey_f8")
    private String userkeyF8;

    /**
     * 用户按键9
     */
    @TableField("userkey_f9")
    private String userkeyF9;

    /**
     * 用户按键10
     */
    @TableField("userkey_f10")
    private String userkeyF10;

    /**
     * 生成话单通道 方
     */
    @TableField("short_channel_name")
    private String shortChannelName;

    /**
     * 企业标识
     */
    @TableField("company_code")
    private String companyCode;

    /**
     * 是否已质检
     */
    @TableField("ifqa")
    private String ifqa;

    /**
     * 绑定的工单
     */
    @TableField("caseid")
    private String caseid;

    /**
     * 绑定的客户
     */
    @TableField("cusid")
    private String cusid;

    /**
     * 是否听过留言
     */
    @TableField("ifplayedleavemsg")
    private String ifplayedleavemsg;

    /**
     * ivr轨迹
     */
    @TableField("ivr_tracks")
    private String ivrTracks;

    /**
     * 队列标识，1进队列、0出队列、空没触发队列节点
     */
    @TableField("cc_queue_hangup")
    private String ccQueueHangup;

    /**
     * 第一次进队列开始时间
     */
    @TableField("runcc_times")
    private String runccTimes;

    /**
     * 排队次数
     */
    @TableField("cc_busy_no")
    private String ccBusyNo;

    /**
     * 热线号码
     */
    @TableField("cti_callid")
    private String ctiCallid;

//  暂时不确定
//    /**
//     * 随路数据
//     */
//    @TableField("ivr_parameters_data")
//    private String ivrParametersData;
}

