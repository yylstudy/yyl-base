package com.cqt.model.client.vo;

import com.cqt.base.enums.SdkErrCode;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientGetTokenDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 获取token返回
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientGetTokenVO extends ClientResponseBaseVO implements Serializable {

    private static final long serialVersionUID = 3113424155698909917L;

    /**
     * token
     */
    private String token;

    /**
     * 响应
     */
    public static ClientGetTokenVO response(ClientGetTokenDTO getTokenDTO, String token, SdkErrCode sdkErrCode) {
        ClientGetTokenVO getTokenVO = new ClientGetTokenVO();
        getTokenVO.setReqId(getTokenDTO.getReqId());
        getTokenVO.setCompanyCode(getTokenDTO.getCompanyCode());
        getTokenVO.setMsgType(getTokenDTO.getMsgType());
        getTokenVO.setToken(token);
        getTokenVO.setCode(sdkErrCode.getCode());
        getTokenVO.setMsg(sdkErrCode.getName());
        getTokenVO.setReply(true);
        return getTokenVO;
    }
}
