package com.cqt.model.client.vo;

import com.cqt.base.enums.SdkErrCode;
import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 迁入 响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ClientCheckinVO extends ClientResponseBaseVO implements Serializable {

    private static final long serialVersionUID = 3113424155698909917L;

    @JsonProperty("ext_id")
    private String extId;

    @JsonProperty("ext_pwd")
    private String extPwd;

    @JsonProperty("login_type")
    private String loginType;

    private String pbx;

    @JsonProperty("rtc_address")
    private String rtcAddress;

    private String os;

    @JsonProperty("ext_call_mode")
    private Integer extCallMode;

    @JsonProperty("service_mode")
    private Integer serviceMode;

    /**
     * 鉴权token
     *
     * @since 7.0.0
     */
    private String token;

    public ClientCheckinVO(ClientRequestBaseDTO baseDTO, SdkErrCode errCode) {
        this(baseDTO, errCode, "");
    }

    public ClientCheckinVO(ClientRequestBaseDTO baseDTO, SdkErrCode errCode, String errorMsg) {
        setReply(true);
        setMsg(errorMsg);
        setOs(baseDTO.getOs());
        setCode(errCode.getCode());
        setReqId(baseDTO.getReqId());
        setMsgType(baseDTO.getMsgType());
        setCompanyCode(baseDTO.getCompanyCode());
        if (errorMsg == null || errorMsg.isEmpty()) {
            setMsg(errCode.getName());
        }
    }
}
