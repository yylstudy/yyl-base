package com.cqt.model.bind.axg.entity;

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
 * AXG绑定关系(BindInfoAxb)实体类
 *
 * @author linshiqiang
 * @since 2021-10-18 15:09:42
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("private_bind_info_axg")
public class PrivateBindInfoAxg extends BaseAuth implements Serializable {

    private static final long serialVersionUID = 886367304675826099L;

    /**
     * 绑定关系ID
     * （长度可以超过32个字符，但建议最长不要超过40个字符），需要增加特殊的前缀，如cqt; 
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
     * A号码；18600008888或0108888999;
     */
    private String telA;

    /**
     * B号码；18600008888或0108888999,
     */
    private String telB;

    /**
     * 虚拟号码X
     */
    private String telX;

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
     * 有效持续时间，即过expiration ms后AXB关系失效自动解绑；
     */
    private Long expiration;

    /**
     * 0：正常(可不传，系统默认0)  1：禁用短信
     */
    private Integer type;

    /**
     * A打给B，接通后A号码拨打X放音文件
     */
    @TableField("audio_a_call_x")
    private String audioACallX;

    /**
     * B打给A，接通后B号码拨打X放音文件
     */
    @TableField("audio_b_call_x")
    private String audioBCallX;

    /**
     * B打给A，接通后A端听到的提示音
     */
    @TableField("audio_a_called_x")
    private String audioACalledX;

    /**
     * A打给B，接通后B端听到的提示音
     */
    @TableField("audio_b_called_x")
    private String audioBCalledX;

    /**
     * A打给B，接通前A号码拨打X放音文件
     */
    @TableField("audio_a_call_x_before")
    private String audioACallXBefore;

    /**
     * B打给A，接通前B号码拨打X放音文件
     */
    @TableField("audio_b_call_x_before")
    private String audioBCallXBefore;

    /**
     * 当前绑定关系是否需要录音 0：不需要 1：需要（数值）
     */
    private Integer enableRecord;

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
    private Date expireTime;

    @JSONField(format = "user_data")
    private String userData;

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
}
