package com.cqt.model.bind.axebn.entity;

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
 * AXEBN模式绑定关系(MtkfBindInfoAxebn)实体类
 *
 * @author linshiqiang
 * @since 2021-10-18 15:09:42
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("private_bind_info_axebn")
public class PrivateBindInfoAxebn extends BaseAuth implements Serializable {

    private static final long serialVersionUID = 444550212677779088L;

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
     * 企业id
     */
    private String vccId;

    /**
     * A号码
     */
    private String telA;

    /**
     * B号码
     */
    private String telB;

    /**
     * 虚拟X号码
     */
    private String telX;

    /**
     * 分机号,与tel_b相对应，中间以英文逗号分隔
     */
    @TableField("tel_x_ext")
    private String telXExt;

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
     * B打给A，接通后主叫B听到的提示音
     */
    private String audio;

    /**
     * B打给A，接通后被叫A端听到的提示音
     */
    private String audioCalled;

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

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 是否禁用短信
     * 0：正常(可不传，系统默认0)  1：禁用短信
     */
    private Integer type;

    private String userData;


}
