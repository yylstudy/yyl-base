package com.cqt.model.freeswitch.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-08-01 17:09
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetExtensionRegVO extends FreeswitchApiVO implements Serializable {

    private static final long serialVersionUID = 5201864191154318942L;
    
    /**
     * 分机号
     */
    @JsonProperty("ext_id")
    private String extId;

    /**
     * 当前注册地址
     */
    @JsonProperty("reg_addr")
    private String regAddr;

    /**
     * 注册CALL_ID
     */
    @JsonProperty("reg_call_id")
    private String regCallId;

    /**
     * 当前剩余有效期
     */
    @JsonProperty("reg_expire")
    private Long regExpire;

    /**
     * 注册状态
     */
    @JsonProperty("reg_status")
    private Boolean regStatus;

    /**
     * 最近注册时间
     */
    @JsonProperty("reg_time")
    private String regTime;

    /**
     * 呼叫状态[IDLE:空闲，RING:响铃，ANSWER:通话中]
     */
    @JsonProperty("call_status")
    private String callStatus;

    /**
     * 复制对象
     */
    public void copy(GetExtensionRegVO getExtensionRegVO, GetExtensionRegVO copyGetExtensionRegVO) {
        copyGetExtensionRegVO.setCallStatus(getExtensionRegVO.getCallStatus());
        copyGetExtensionRegVO.setRegAddr(getExtensionRegVO.getRegAddr());
    }
}
