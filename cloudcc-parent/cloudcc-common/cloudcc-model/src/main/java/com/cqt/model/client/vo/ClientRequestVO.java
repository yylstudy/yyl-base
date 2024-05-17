package com.cqt.model.client.vo;

import com.cqt.base.enums.SdkErrCode;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 技能列表 响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientRequestVO<T> extends ClientResponseBaseVO implements Serializable {

    private static final long serialVersionUID = -923055163963048215L;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public static <T> ClientRequestVO<T> response(String companyCode, T data, SdkErrCode sdkErrCode) {
        ClientRequestVO<T> requestVO = new ClientRequestVO<>();
        requestVO.setCompanyCode(companyCode);
        requestVO.setData(data);
        requestVO.setMsg(sdkErrCode.getName());
        requestVO.setCode(sdkErrCode.getCode());
        return requestVO;
    }

    public static <T> ClientRequestVO<T> response(T data, SdkErrCode sdkErrCode) {
        ClientRequestVO<T> requestVO = new ClientRequestVO<>();
        requestVO.setData(data);
        requestVO.setMsg(sdkErrCode.getName());
        requestVO.setCode(sdkErrCode.getCode());
        return requestVO;
    }

    public static <T> ClientRequestVO<T> response(SdkErrCode sdkErrCode) {
        ClientRequestVO<T> requestVO = new ClientRequestVO<>();
        requestVO.setMsg(sdkErrCode.getName());
        requestVO.setCode(sdkErrCode.getCode());
        return requestVO;
    }
}
