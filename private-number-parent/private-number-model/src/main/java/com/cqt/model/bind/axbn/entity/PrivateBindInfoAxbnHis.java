package com.cqt.model.bind.axbn.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author linshiqiang
 * @date 2022/3/22 14:31
 * AXBN 历史绑定记录
 */
@Data
@TableName("private_bind_info_axbn_his")
public class PrivateBindInfoAxbnHis implements Serializable {

    private static final long serialVersionUID = -8428390869325886061L;

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
     * A号码；18600008888或0108888999,
     */
    private String telA;

    /**
     * B号码；18600008888或0108888999,可以使用11开头的11位假手机号
     */
    private String telB;

    /**
     * 其他B号码；号码可为最多为5个号码，中间以英文逗号分隔
     */
    @TableField("tel_b_other")
    private String otherTelB;

    /**
     * 虚号
     */
    private String telX;

    /**
     * 虚号
     */
    private String telY;

    /**
     * 以0开头的虚拟号区号（如010）
     */
    private String areaCode;

    /**
     * 创建时间
     */
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 过期时间
     */
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    /**
     * 有效持续时间，即过expiration秒后AX关系失效自动解绑；
     */
    private Long expiration;
}
