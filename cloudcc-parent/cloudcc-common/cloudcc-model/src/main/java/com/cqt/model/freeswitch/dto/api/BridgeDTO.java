package com.cqt.model.freeswitch.dto.api;

import cn.hutool.core.util.StrUtil;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:08
 * 桥接入参
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BridgeDTO extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = 6931112307435613387L;

    /**
     * 是  | 目标通话ID
     */
    @JsonProperty("d_uuid")
    private String dUuid;

    /**
     * 是  | 源通话ID
     */
    @JsonProperty("s_uuid")
    private String sUuid;

    /**
     * 构建
     */
    public static BridgeDTO build(String companyCode, String sUuid, String dUuid) {
        BridgeDTO bridgeDTO = new BridgeDTO();
        bridgeDTO.setReqId(sUuid + StrUtil.AT + dUuid);
        bridgeDTO.setCompanyCode(companyCode);
        bridgeDTO.setSUuid(sUuid);
        bridgeDTO.setDUuid(dUuid);
        return bridgeDTO;
    }

}
