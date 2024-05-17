package com.cqt.model.client.vo;

import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 咨询 响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientConsultVO extends ClientResponseBaseVO implements Serializable {

    private static final long serialVersionUID = 3113424155698909917L;

    @JsonProperty("consult_uuid")
    private String consultUuid;

    /**
     * 咨询结果对象
     */
    public static ClientConsultVO response(ClientRequestBaseDTO clientRequestBaseDTO,
                                           String consultUuid,
                                           String code,
                                           String msg) {
        ClientConsultVO clientConsultVO = new ClientConsultVO();
        clientConsultVO.setReqId(clientRequestBaseDTO.getReqId());
        clientConsultVO.setCompanyCode(clientRequestBaseDTO.getCompanyCode());
        clientConsultVO.setMsgType(clientRequestBaseDTO.getMsgType());
        clientConsultVO.setCode(code);
        clientConsultVO.setMsg(msg);
        clientConsultVO.setReply(true);
        clientConsultVO.setConsultUuid(consultUuid);
        return clientConsultVO;
    }
}
