package com.cqt.model.bind.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 美团小号 AXB绑定关系(MtBindInfoAxb)实体类
 *
 * @author linshiqiang
 * @since 2021-09-09 14:42:34
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MtBindInfoAxb implements Serializable {

    private static final long serialVersionUID = -46448964222327413L;

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
     * 以0开头的虚拟号区号（如010）
     */
    private String areaCode;

    /**
     * 有效持续时间，过expiration秒后自动解绑。数值字符串
     */
    private String expiration;

    /**
     * A拨打X放音文件编码
     */
    @TableField("audio_a_call_x")
    private String audioACallX;

    /**
     * B拨打X放音文件编码
     */
    @TableField("audio_b_call_x")
    private String audioBCallX;

    /**
     * 非A、B的其它号码拨打X放音文件编码
     */
    @TableField("audio_other_call_x")
    private String audioOtherCallX;

    /**
     * A 为被叫时 给A播放语音文件编码
     */
    @TableField("audio_a_called_x")
    private String audioACalledX;

    /**
     * B 为被叫时 给B播放语音文件编码
     */
    @TableField("audio_b_called_x")
    private String audioBCalledX;

    /**
     * 标识当前绑定请求是否使用全国号池
     * 传1：使用全国号池
     * 不传该字段或传0：不启用全国号池
     */
    @TableField("wholearea")
    private String wholearea;

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

    private String relateBindId;
}
