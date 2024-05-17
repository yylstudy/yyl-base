package com.cqt.model.bind.axe.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cqt.model.common.BaseAuth;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * AXE模式绑定关系(BindInfoAxe)实体类
 *
 * @author linshiqiang
 * @since 2021-10-18 15:09:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("private_bind_info_axe")
public class PrivateBindInfoAxe extends BaseAuth implements Serializable {

    private static final long serialVersionUID = -93597918780232001L;

    /**
     * 绑定关系ID
     * （长度可以超过32个字符，但建议最长不要超过40个字符），需要增加特殊的前缀，如cqt
     */
    @TableId(type = IdType.INPUT)
    private String bindId;

    /**
     * 企业每个请求Id唯一，如果是同一个请求重复提交，则Id保持相同
     */
    private String requestId;

    /**
     * 供应商id
     */
    private String supplierId;

    /**
     * 企业id
     */
    private String vccId;

    /**
     * X绑定的真实被叫号码, 可以接受手机号码、固定电话。固话有区号且为全数字；如：18600008888或0108888999
     */
    private String tel;

    /**
     * AX回呼B
     */
    private String telB;

    /**
     * 虚号
     */
    @TableField("tel_x")
    private String telX;

    /**
     * 分机号
     */
    @TableField("tel_x_ext")
    private String telXExt;

    /**
     * 以0开头的虚拟号区号（如010）
     */
    private String areaCode;

    /**
     * 有效持续时间，即过expiration秒后AX关系失效自动解绑；
     */
    private Long expiration;

    /**
     * 使用全国池, 1 是, 0 否
     */
    private Integer wholeArea;

    /**
     * 当前绑定关系是否需要录音 0：不需要 1：需要（数值字符串）
     */
    private Integer enableRecord;

    /**
     * 用户透传字段
     */
    private String userData;

    /**
     * 城市编码 全国0000
     */
    private String cityCode;

    /**
     * 0：正常(可不传，系统默认0)  1：禁用短信
     */
    private Integer type;

    /**
     * AX模型B打给A，接通后B播放的语音
     */
    private String audio;

    /**
     * AX模型B打给A，接通后A播放的语音
     */
    private String audioCalled;

    /**
     * 是否生成AYB绑定关系
     * 1: 是 (默认值)
     * 0: 否
     */
    private Integer aybFlag;

    /**
     * AYB有效持续时间单位秒
     */
    @TableField("ayb_expiration")
    private Long aybExpiration;

    /**
     * 获取Y号码地区；默认=area_code
     */
    @TableField("ayb_area_code")
    private String aybAreaCode;

    /**
     * tel回呼Y时被叫看到的来电号码 1：看见Y   2：看见tel_x
     */
    @TableField("ayb_other_show")
    private Integer aybOtherShow;

    /**
     * AYB模型A打给B，接通后主叫A听到的提示音
     */
    @TableField("ayb_audio_a_call_x")
    private String aybAudioACallX;

    /**
     * AYB模型B打给A，接通后主叫B听到的提示音
     */
    @TableField("ayb_audio_b_call_x")
    private String aybAudioBCallX;

    /**
     * AYB模型B打给A，接通后A端听到的提示音
     */
    @TableField("ayb_audio_a_called_x")
    private String aybAudioACalledX;

    /**
     * AYB模型A打给B，接通后B端听到的提示音
     */
    @TableField("ayb_audio_b_called_x")
    private String aybAudioBCalledX;

    /**
     * 不区分模型，A打给B，接通前主叫A听到的提示音
     */
    @TableField("ayb_audio_a_call_x_before")
    private String aybAudioACallXBefore;

    /**
     * 不区分模型，B打给A，接通前主叫B听到的提示音
     */
    @TableField("ayb_audio_b_call_x_before")
    private String aybAudioBCallXBefore;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 修改时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 过期时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 最大通话时长
     */
    private Integer maxDuration;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 指示呼叫转接的录音格式，仅下列值有效。默认是 wav。
     * mp3
     * wav
     */
    private String recordFileFormat;

    /**
     * 非A用户呼叫X时, A接到呼叫时的主显号码。
     * 2：显示X号码(默认值)
     * 3：显示真实号码
     */
    private Integer model;

    /**
     * 录音方式。
     * 0：混音，即通话双方的声音混合在一个声道中。
     * 1：双声道，即通话双方的声音分别录制在左、右两个声道中。
     * 如果不携带该参数，参数值默认为0。
     */
    private Integer recordMode;

    /**
     * 双声道录音模式，取值范围如下：
     * 0：主叫录音到左声道，被叫录音到右声道。
     * 1：被叫录音到左声道，主叫录音到右声道。
     * 录音模式为双声道时有效，而且是必选
     * 默认主叫录音到左声道，被叫录音到右声道
     */
    private Integer dualRecordMode;

    /**
     * 通话最后一分钟放音
     */
    private String lastMinVoice;

    /**
     * 地市编码匹配规则
     * 当whole_area=1时该字段无效
     * 1: 地市池不足, 分配全国号码池;
     * 2: 地市池不足, 不分配全国号码池(默认值)
     */
    private Integer areaMatchMode;

    /**
     * tel是否可以回呼X找到最近联系人, 回呼有效时间为callback_expiration:
     * 1: 是
     * 0: 否(默认值)
     */
    private Integer callbackFlag;

    /**
     * 回呼AX绑定有效持续时间(秒),
     * 仅当callback_flag=1时有效
     * 必须小于expiration
     * 为空默认与expiration相同
     */
    private Long callbackExpiration;

    /**
     * 回呼AX过期时间
     */
    private Date callbackExpireTime;

}
