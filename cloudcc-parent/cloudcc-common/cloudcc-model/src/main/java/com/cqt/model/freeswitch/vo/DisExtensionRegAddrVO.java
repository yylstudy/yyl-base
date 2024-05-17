package com.cqt.model.freeswitch.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-08-01 17:14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DisExtensionRegAddrVO extends FreeswitchApiVO implements Serializable {

    private static final long serialVersionUID = 279880692587921263L;

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
}
