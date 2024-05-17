package com.cqt.model.bind.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author linshiqiang
 * @date 2021/9/12 15:07
 * 绑定关系回收 to mq
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BindRecycleDTO implements Serializable {

    private static final long serialVersionUID = -2516378149555274339L;

    /**
     * 请求id
     */
    private String requestId;

    /**
     * A号码
     * AXB
     */
    private String telA;

    /**
     * B号码
     * AXB
     */
    private String telB;

    /**
     * A号码
     * AXE
     */
    private String tel;

    /**
     * X号码
     */
    private String telX;

    /**
     * Y号码
     * AXYB
     */
    private String telY;

    /**
     * 其他B号码
     * AXG AXBN
     */
    private String otherTelB;

    /**
     * 分机号
     * AXE
     */
    private String extNum;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 地市编码
     */
    private String areaCode;

    /**
     * 城市编码 全国池为0000
     */
    private String cityCode;

    /**
     * AXB AYB
     */
    private String numType;

    /**
     * 操作类型 INSERT UPDATE DELETE
     */
    private String operateType;

    /**
     * 企业id
     */
    private String vccId;

    /**
     * 绑定id
     */
    private String bindId;

    /**
     * 供应商id
     */
    private String supplierId;

    /**
     * 指定X号码  1 不生成号码池   0 生成号码池
     */
    @JsonProperty("direct_tel_x")
    private Integer directTelX;

    /**
     * 是否生成AYB 1 是, 0 否
     */
    private Integer aybFlag;
}
