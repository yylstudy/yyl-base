package com.cqt.cdr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.cqt.model.cdr.entity.CallCenterMainCdr;
import com.cqt.model.cdr.entity.CallCenterSubCdr;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName clouccc_acr_record
 */
@TableName(value = "acr_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcrRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 序列号
     */
    @TableId
    private String streamnumber;

    /**
     * 业务关键字
     * 点击拨号servicekey＝900001
     * sip直拨servicekey＝900002电话会议（）
     * 回呼Referto呼转话单servicekey＝900003(呼转)
     * 回呼 servicekey＝900004
     * 电话会议 servicekey＝900005
     * 国家6位省级行政区划表（如北京110000，天津120000）
     */
    private String servicekey;

    /**
     * 通话费用，单位：人民币分
     */
    private Integer callcost;

    /**
     * 被叫号码，即用户拨叫的号码
     * 对于主叫话单，填写主叫实际拨打的号码
     * 对于被叫话单，填写被叫计费的16位分机号
     * 对于前转话单，填写前转流程处理前的被叫号码
     */
    private String calledpartynumber;

    /**
     * 主叫号码
     * 对于主叫话单，填写主叫计费的16位分机号
     * 对于被叫话单，填写主叫号码，即INVITE的PAI消息头，如果PAI消息头为空，则填写INVITE的From消息头
     * 对于前转话单，填写前转计费的16位分机号
     * 对于被叫话单，填写主叫号码，即INVITE的PAI消息头，如果PAI消息头为空，则填写INVITE的From消息头
     * 对于前转话单，填写前转计费的16位分机号
     */
    private String callingpartynumber;

    /**
     * 计费模式， 1：计费
     */
    private Integer chargemode;

    /**
     * 计费号码
     * 对于主叫话单，填写主叫计费分机的显示号码
     * 对于被叫话单，填写被叫计费分机的显示号码
     * 对于前转话单，填写前转计费分机的显示号码
     */
    private String specificchargedpar;

    /**
     * 翻译号码，即用户实际接通的号码。
     * 对于主叫话单，填写主叫流程处理后的被叫号码
     * 对于被叫话单，填写被叫流程处理后的被叫号码
     * 对于前转话单，填写前转流程处理后的被叫号码
     */
    private String translatednumber;

    /**
     * 开始时间，格式：yyyymmddhhmmss
     */
    private String startdateandtime;

    /**
     * 结束时间，格式：yyyymmddhhmmss
     */
    private String stopdateandtime;

    /**
     * 通话时长，单位：秒
     */
    private Long duration;

    /**
     * 计费类别
     */
    private Integer chargeclass;

    /**
     * 透明参数
     */
    private String transparentparamet;

    /**
     * 呼叫类型：（目前未使用）
     * 0:正常呼叫
     * 13:计费码呼叫
     */
    private Integer calltype;

    /**
     * 坐席工号
     */
    private String callersubgroup;

    /**
     * 网关名称
     */
    private String calleesubgroup;

    /**
     * 当前呼叫的CallID
     */
    private String acrcallid;

    /**
     * 原始被叫
     * 对于主叫话单，填写原始INVITE消息的To消息头
     * 对于被叫话单，填写原始INVITE消息的To消息头
     * 对于前转话单，填写原始INVITE消息的To消息头
     */
    private String oricallednumber;

    /**
     * 原始主叫
     * 对于主叫话单，填写原始INVITE消息的From消息头
     * 对于被叫话单，填写原始INVITE消息的From消息头
     * 对于前转话单，填写原始INVITE消息的From消息头
     */
    private String oricallingnumber;

    /**
     * 主叫短号（暂时未用）
     */
    private String callerpnp;

    /**
     * 叫短号（暂时未用）
     */
    private String calleepnp;

    /**
     * 重路由类型（目前未使用）
     */
    private Integer reroute;

    /**
     * 集团号
     */
    private String groupnumber;

    /**
     * 点击拨号的呼叫顺序：1：第一通呼叫， 2：第二通呼叫；默认值：1。
     */
    private Integer callcategory;

    /**
     * 话单类型：
     * 0：市话
     * 1：国内长途
     * 2：国际长途
     */
    private Integer chargetype;

    /**
     * 计费码
     */
    private String userpin;

    /**
     * 呼叫类型，指发端或终端
     * 1：主叫话单
     * 2：被叫话单
     * 3：前转话单
     * 100：质检
     * 注：servicekey=900003 的referTo转移话单的时候，  ACRTYPE 这个字段的值填的是  3：前转话单 ；ACRTYPE=3 前传话单是对于CTD 来说跟主叫是一样，会产生话费。
     */
    private Integer acrtype;

    /**
     * 0非视频，1是视频，默认0
     */
    private Integer videocallflag;

    /**
     *
     */
    private String serviceid;

    /**
     * 通话Uuid
     */
    private String forwardnumber;

    /**
     * 振铃时间
     */
    private String extforwardnumber;

    /**
     * 录音地址
     */
    private String srfmsgid;

    /**
     * 所在的媒体服务器名称
     */
    private String msserver;

    /**
     * 呼叫开始时间，时间戳，
     * 例如：20140401091026.759
     */
    private String begintime;

    /**
     * 结束码
     * 0 应答后主叫挂机
     * 1 应答后被叫挂机
     * 10 应答前主叫放弃 （通话时长都是0）
     * 99 未接通，分辨不出来原因
     * >=300被叫未接通错误码 （通话时长都是0）
     * 结束码
     * 0 应答后主叫挂机
     * 1 应答后被叫挂机
     * 10 应答前主叫放弃 （通话时长都是0）
     * 99 未接通，分辨不出来原因
     * >=300被叫未接通错误码 （通话时长都是0）
     */
    private Integer releasecause;

    /**
     * 结束原因值
     */
    private String releasereason;

    /**
     * 区号
     */
    private String areanumber;

    /**
     * 呼入时间
     */
    private String calledareacode;

    /**
     *
     */
    private String localorlong;

    /**
     *
     */
    private Integer id;

    /**
     *
     */
    private String dtmfkey;


    /**
     * 呼入时间
     */
    private String callintime;
}