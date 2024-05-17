package com.cqt.model.client.vo;

import com.cqt.base.enums.SdkErrCode;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientToggleServiceModeDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 呼入 响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientToggleServiceModeVO extends ClientResponseBaseVO implements Serializable {

    private static final long serialVersionUID = 3113424155698909917L;

    /**
     * 切换后的服务模式 1-客服型 2-外呼型
     */
    @JsonProperty("service_mode")
    private Integer serviceMode;

    /**
     * 响应
     */
    public static ClientToggleServiceModeVO response(ClientToggleServiceModeDTO toggleServiceModeDTO, SdkErrCode sdkErrCode) {

        ClientToggleServiceModeVO toggleServiceModeVO = new ClientToggleServiceModeVO();
        toggleServiceModeVO.setMsg(sdkErrCode.getName());
        toggleServiceModeVO.setCode(sdkErrCode.getCode());
        toggleServiceModeVO.setReply(true);
        toggleServiceModeVO.setMsgType(toggleServiceModeDTO.getMsgType());
        toggleServiceModeVO.setCompanyCode(toggleServiceModeDTO.getCompanyCode());
        toggleServiceModeVO.setReqId(toggleServiceModeDTO.getReqId());
        toggleServiceModeVO.setOs(toggleServiceModeDTO.getOs());
        toggleServiceModeVO.setServiceMode(toggleServiceModeDTO.getServiceMode());
        return toggleServiceModeVO;
    }
}
