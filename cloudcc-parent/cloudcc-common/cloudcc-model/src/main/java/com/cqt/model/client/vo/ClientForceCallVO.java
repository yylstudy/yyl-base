package com.cqt.model.client.vo;

import com.cqt.base.enums.SdkErrCode;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientAdminXferDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 强插 响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientForceCallVO extends ClientResponseBaseVO implements Serializable {

    private static final long serialVersionUID = 3113424155698909917L;

    /**
     * 与坐席正在一起通话的号码列表
     */
    @JsonProperty("in_call_numbers")
    private Set<String> inCallNumbers;

    /**
     * 对象
     */
    public static ClientForceCallVO response(ClientAdminXferDTO clientAdminXferDTO,
                                             String uuid,
                                             Set<String> inCallNumbers,
                                             SdkErrCode sdkErrCode) {
        ClientForceCallVO clientThreeWayVO = new ClientForceCallVO();
        clientThreeWayVO.setReqId(clientAdminXferDTO.getReqId());
        clientThreeWayVO.setCompanyCode(clientAdminXferDTO.getCompanyCode());
        clientThreeWayVO.setMsgType(clientAdminXferDTO.getMsgType());
        clientThreeWayVO.setCode(sdkErrCode.getCode());
        clientThreeWayVO.setUuid(uuid);
        clientThreeWayVO.setMsg(sdkErrCode.getName());
        clientThreeWayVO.setInCallNumbers(inCallNumbers);
        clientThreeWayVO.setReply(true);
        return clientThreeWayVO;
    }
}
