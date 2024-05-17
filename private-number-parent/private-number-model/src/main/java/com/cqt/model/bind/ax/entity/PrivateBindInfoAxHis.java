package com.cqt.model.bind.ax.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AX绑定关系历史(MtBindInfoAxHis)实体类
 *
 * @author linshiqiang
 * @since 2021-09-09 14:42:34
 */
@Data
@TableName("private_bind_info_ax_his")
public class PrivateBindInfoAxHis implements Serializable {

    private static final long serialVersionUID = -8760350156931952597L;

    /**
     * 绑定关系ID
     * （长度可以超过32个字符，但建议最长不要超过40个字符），需要增加特殊的前缀，如cqt; 
     */
    @TableId(type = IdType.INPUT)
    private String bindId;

    /**
     * 请求ID,request_id相同的请求，须返回相同的结果。
     * request_id不同的请求，代表不同的绑定申请，即使tel_a、tel_b相同，也应返回不同的tel_x。
     */
    private String requestId;

    /**
     * 供应商id
     */
    private String supplierId;

    /**
     * 号码A, 可以接受手机号码、固定电话。固话有区号且为全数字。
     */
    @TableField("tel_a")
    private String telA;

    /**
     * 号码B, 可以接受手机号码、固定电话。固话有区号且为全数字。
     */
    @TableField("tel_b")
    private String telB;

    /**
     * 虚拟号码
     */
    @TableField("tel_x")
    private String telX;

    /**
     * 企业id
     */
    @TableField("vcc_id")
    private String vccId;

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
