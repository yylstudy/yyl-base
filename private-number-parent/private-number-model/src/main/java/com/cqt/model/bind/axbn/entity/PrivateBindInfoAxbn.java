package com.cqt.model.bind.axbn.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cqt.model.common.BaseAuth;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author linshiqiang
 * @date 2022/3/22 14:26
 * AXBN 绑定关系
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("private_bind_info_axbn")
public class PrivateBindInfoAxbn extends BaseAuth implements Serializable {

    private static final long serialVersionUID = 6109172633259123623L;

    /**
     * 绑定关系ID
     * （长度可以超过32个字符，但建议最长不要超过40个字符），需要增加特殊的前缀，如cqt; 
     */
    @TableId(type = IdType.INPUT)
    private String bindId;

    /**
     * 企业id
     */
    private String vccId;

    /**
     * 企业每个请求Id唯一，如果是同一个请求重复提交，则Id保持相同
     */
    private String requestId;

    /**
     * 供应商id
     */
    private String supplierId;

    /**
     * 源绑定id, 追加
     */
    private String sourceBindId;

    /**
     * A号码；18600008888或0108888999,
     */
    private String telA;

    /**
     * B号码；18600008888或0108888999,可以使用11开头的11位假手机号
     */
    private String telB;

    /**
     * 虚号
     */
    private String telX;

    /**
     * 其他B号码；号码可为最多为5个号码，中间以英文逗号分隔
     */
    @TableField("tel_b_other")
    private String otherTelB;

    /**
     * 虚号个数对应tel_b_other号码的个数，
     * 中间以英文逗号分隔
     * 例如：
     * tel_b_other = 186xxxx0001,186xxxx0002
     * tel_y = 176xxxx0008,176xxxx0009
     * tel_a呼叫号码0008转接到0001
     * tel_a呼叫号码0009转接到 0002
     */
    private String telY;

    /**
     * tel_other_b与tel_y对应关系；例"otherB_Y":[{"18613883404":"18540257293"},{"18611111111":"18540250502"}]
     */
    @TableField("otherB_Y")
    private String otherBy;

    /**
     * 被叫来显号码
     */
    private String displayNumber;

    /**
     * 以0开头的虚拟号区号（如010）
     */
    private String areaCode;

    /**
     * 城市编码 全国0000
     */
    private String cityCode;

    /**
     * 使用全国池, 1 是, 0 否
     */
    private Integer wholeArea;

    /**
     * 有效持续时间，即过expiration秒后AX关系失效自动解绑；
     */
    private Long expiration;

    /**
     * 0：正常(可不传，系统默认0)  1：禁用短信
     */
    private Integer type;

    /**
     * A拨打Y放音文件
     */
    @TableField("audio_a_call_x")
    private String audioACallX;

    /**
     * B拨打X放音文件编码
     */
    @TableField("audio_b_call_x")
    private String audioBCallX;

    /**
     * 1:只需要联系telB（只能使用x虚拟号接通被叫B，使用y1到y5拨打失败，放统一提示音）
     * 2:需要能联系到所有telB（可以使用x,y1到y5虚拟号联系B，并成功通话）
     * 以上两种模式中所有B号码呼叫tel_x都能转接到tel_a号码,
     * tel_a呼叫tel_x转接到tel_b号码；
     */
    private Integer mode;

    /**
     * 当前绑定关系是否需要录音 0：不需要 1：需要（数值字符串）
     */
    private Integer enableRecord;

    /**
     * 创建时间
     */
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 修改时间
     */
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 过期时间
     */
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    /**
     * 最大通话时长
     */
    private Integer maxDuration;

    private String userData;

    /**
     * 指示呼叫转接的录音格式，仅下列值有效。默认是 wav。
     *     mp3
     *     wav
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
     *   0：混音，即通话双方的声音混合在一个声道中。
     *   1：双声道，即通话双方的声音分别录制在左、右两个声道中。
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
}
