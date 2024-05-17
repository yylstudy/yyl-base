package com.cqt.sms.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class MtAxe implements Serializable {
    private static final long serialVersionUID = -83345686579968337L;
    /**
     * 请求ID,request_id相同的请求，须返回相同的结果。
     * request_id不同的请求，代表不同的绑定申请，即使tel_a、tel_b相同，也应返回不同的tel_x。
     */
    @TableId
    private String requestId;
    /**
     * 绑定关系ID
     * （长度可以超过32个字符，但建议最长不要超过40个字符），需要增加特殊的前缀，如cqt; 
     */
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
     * 有效持续时间，过expiration秒后自动解绑。数值字符串
     */
    private String expiration;
    /**
     * 标识当前绑定请求是否使用全国号池
     * 传1：使用全国号池
     * 不传该字段或传0：不启用全国号池
     */
    @TableField("wholearea")
    private String wholearea;
    /**
     * 其它号码拨打X放音文件编码
     */
    private String audio;
    /**
     * 当前绑定关系是否需要录音 0：不需要 1：需要（数值字符串）
     */
    private String enableRecord;
    /**
     * 业务侧信息透传字段。所有当前bind_id产生的通话在话单回推时需要将该字段带回
     */
    private String userData;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 修改时间
     */
    private String updateTime;
    /**
     * 过期时间
     */
    private String expireTime;

    /**
     * 最大通话时长
     */
    private Integer maxDuration;

    /**
     * 地市编码 通area_code,  wholearea=1时 为0000
     */
    private String cityCode;


    private String ts;

    private String sign;
}