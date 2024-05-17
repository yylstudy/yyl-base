package com.cqt.model.client.vo;

import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientHoldDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 保持/取消保持响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientHoldVO extends ClientResponseBaseVO implements Serializable {

    private static final long serialVersionUID = 3113424155698909917L;

    /**
     * 是(0否1是) 保持/取回
     */
    @JsonProperty("hold")
    private String hold;

    /**
     * 响应
     */
    public static ClientHoldVO response(ClientHoldDTO clientHoldDTO, String code, String msg) {
        ClientHoldVO responseBaseVO = new ClientHoldVO();
        responseBaseVO.setReqId(clientHoldDTO.getReqId());
        responseBaseVO.setCompanyCode(clientHoldDTO.getCompanyCode());
        responseBaseVO.setMsgType(clientHoldDTO.getMsgType());
        responseBaseVO.setHold(clientHoldDTO.getHold());
        responseBaseVO.setCode(code);
        responseBaseVO.setMsg(msg);
        responseBaseVO.setReply(true);
        return responseBaseVO;
    }
}
