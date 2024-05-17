package com.cqt.model.bind.axe.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AXE绑定关系历史(CustomerBindInfoAxeHis)实体类
 *
 * @author linshiqiang
 * @since 2021-10-18 15:09:42
 */
@Data
@TableName("private_bind_info_axe_his")
public class PrivateBindInfoAxeHis implements Serializable {

    private static final long serialVersionUID = 886367304675826099L;

    /**
     * 企业每个请求Id唯一，如果是同一个请求重复提交，则Id保持相同
     */
    private String requestId;

    /**
     * 绑定关系ID
     * （长度可以超过32个字符，但建议最长不要超过40个字符），需要增加特殊的前缀，如cqt; 
     */
    @TableId
    private String bindId;

    /**
     * 供应商id
     */
    private String supplierId;

    /**
     * 企业id
     */
    @TableField("vcc_id")
    private String vccId;

    /**
     * X绑定的真实被叫号码, 可以接受手机号码、固定电话。固话有区号且为全数字；如：18600008888或0108888999
     */
    private String tel;

    /**
     * 分机号
     */
    @TableField("tel_x_ext")
    private String telXExt;

    /**
     * 虚拟号码
     */
    @TableField("tel_x")
    private String telX;

    /**
     * 以0开头的虚拟号区号（如010）
     */
    private String areaCode;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 过期时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

}
