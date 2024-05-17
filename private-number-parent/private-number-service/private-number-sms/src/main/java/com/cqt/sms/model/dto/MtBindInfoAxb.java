package com.cqt.sms.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 美团小号 AXB绑定关系(MtBindInfoAxb)实体类
 *
 * @author linshiqiang
 * @since 2021-09-09 14:42:34
 */
@Data
public class MtBindInfoAxb implements Serializable {

    private static final long serialVersionUID = -46448964222327413L;
    private String requestId;
    private String bindId;
    private String telA;
    private String telB;
    private String telX;
    private String areaCode;
    private String expiration;
    private String audioACallX;
    private String audioBCallX;
    private String audioOtherCallX;
    private String audioACalledX;
    private String audioBCalledX;
    private String wholearea;
    private String enableRecord;
    private String userData;
    private String createTime;
    private String updateTime;
    private String expireTime;
    private Integer maxDuration;
    private String cityCode;
    private String ts;
    private String sign;
}
