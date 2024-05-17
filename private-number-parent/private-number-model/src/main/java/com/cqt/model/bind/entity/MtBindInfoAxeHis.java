package com.cqt.model.bind.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 美团小号 AXE 分机号绑定关系历史(MtBindInfoAxeHis)实体类
 *
 * @author linshiqiang
 * @since 2021-09-09 14:42:35
 */
@Data
@TableName("mt_bind_info_axe_his")
public class MtBindInfoAxeHis implements Serializable {

    private static final long serialVersionUID = -320850228415631250L;

    /**
     * 请求ID,request_id相同的请求，须返回相同的结果。
     * request_id不同的请求，代表不同的绑定申请，即使tel_a、tel_b相同，也应返回不同的tel_x。
     */
    private String requestId;

    /**
     * 绑定关系ID
     * （长度可以超过32个字符，但建议最长不要超过40个字符），需要增加特殊的前缀，如cqt; 
     */
    @TableId
    private String bindId;

    /**
     * X绑定的真实被叫号码 A。可以接受手机号码、固定电话。固话有区号且为全数字。
     */
    private String tel;

    /**
     * 虚拟号码中总机号
     */
    @TableField("tel_x")
    private String telX;

    /**
     * 虚拟号码中分机号
     */
    @TableField("tel_x_ext")
    private String telXExt;

    /**
     * 以0开头的虚拟号区号（如010）
     */
    private String areaCode;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 过期时间
     */
    private String expireTime;

}
