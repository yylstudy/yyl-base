package com.cqt.model.client.base;

import com.cqt.base.enums.SdkErrCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientResponseBaseVO implements ClientBase, Serializable {

    private static final long serialVersionUID = 4585690899010532177L;

    @JsonProperty("req_id")
    private String reqId;

    @JsonProperty("company_code")
    private String companyCode;

    private String code;

    private String msg;

    @JsonProperty("msg_type")
    private String msgType;

    private String uuid;

    /**
     * 是否需要响应前端sdk
     */
    private Boolean reply;

    @JsonProperty("task_id")
    private String taskId;

    private String os;

    /**
     * 响应
     */
    public static ClientResponseBaseVO response(ClientRequestBaseDTO clientRequestBaseDTO, String code, String msg) {
        ClientResponseBaseVO responseBaseVO = new ClientResponseBaseVO();
        responseBaseVO.setReqId(clientRequestBaseDTO.getReqId());
        responseBaseVO.setCompanyCode(clientRequestBaseDTO.getCompanyCode());
        responseBaseVO.setMsgType(clientRequestBaseDTO.getMsgType());
        responseBaseVO.setCode(code);
        responseBaseVO.setMsg(msg);
        responseBaseVO.setReply(true);
        return responseBaseVO;
    }

    /**
     * 响应
     */
    public static ClientResponseBaseVO response(ClientRequestBaseDTO clientRequestBaseDTO, SdkErrCode sdkErrCode) {
        ClientResponseBaseVO responseBaseVO = new ClientResponseBaseVO();
        responseBaseVO.setReqId(clientRequestBaseDTO.getReqId());
        responseBaseVO.setCompanyCode(clientRequestBaseDTO.getCompanyCode());
        responseBaseVO.setMsgType(clientRequestBaseDTO.getMsgType());
        responseBaseVO.setCode(sdkErrCode.getCode());
        responseBaseVO.setMsg(sdkErrCode.getName());
        responseBaseVO.setReply(true);
        return responseBaseVO;
    }

    /**
     * 响应
     */
    public static ClientResponseBaseVO response(ClientRequestBaseDTO clientRequestBaseDTO,
                                                String uuid,
                                                String code,
                                                String msg) {
        ClientResponseBaseVO responseBaseVO = new ClientResponseBaseVO();
        responseBaseVO.setReqId(clientRequestBaseDTO.getReqId());
        responseBaseVO.setCompanyCode(clientRequestBaseDTO.getCompanyCode());
        responseBaseVO.setMsgType(clientRequestBaseDTO.getMsgType());
        responseBaseVO.setCode(code);
        responseBaseVO.setMsg(msg);
        responseBaseVO.setUuid(uuid);
        responseBaseVO.setReply(true);
        return responseBaseVO;
    }

    /**
     * 响应
     */
    public static ClientResponseBaseVO response(ClientRequestBaseDTO clientRequestBaseDTO,
                                                String uuid,
                                                SdkErrCode sdkErrCode) {
        ClientResponseBaseVO responseBaseVO = new ClientResponseBaseVO();
        responseBaseVO.setReqId(clientRequestBaseDTO.getReqId());
        responseBaseVO.setCompanyCode(clientRequestBaseDTO.getCompanyCode());
        responseBaseVO.setMsgType(clientRequestBaseDTO.getMsgType());
        responseBaseVO.setCode(sdkErrCode.getCode());
        responseBaseVO.setMsg(sdkErrCode.getName());
        responseBaseVO.setUuid(uuid);
        responseBaseVO.setReply(true);
        return responseBaseVO;
    }

    /**
     * 响应reply
     */
    public static ClientResponseBaseVO response(ClientRequestBaseDTO clientRequestBaseDTO,
                                                SdkErrCode sdkErrCode,
                                                Boolean reply) {
        ClientResponseBaseVO responseBaseVO = response(clientRequestBaseDTO, sdkErrCode);
        responseBaseVO.setReply(reply);
        return responseBaseVO;
    }

    /**
     * 响应reply
     */
    public static ClientResponseBaseVO response(ClientRequestBaseDTO clientRequestBaseDTO,
                                                String code,
                                                String msg,
                                                Boolean reply) {
        ClientResponseBaseVO responseBaseVO = response(clientRequestBaseDTO, code, msg);
        responseBaseVO.setReply(reply);
        return responseBaseVO;
    }

    /**
     * 失败响应
     */
    public static ClientResponseBaseVO fail(String code, String msg) {
        ClientResponseBaseVO responseBaseVO = new ClientResponseBaseVO();
        responseBaseVO.setCode(code);
        responseBaseVO.setMsg(msg);
        return responseBaseVO;
    }

    /**
     * 失败响应
     */
    public static ClientResponseBaseVO fail(SdkErrCode sdkErrCode) {
        ClientResponseBaseVO responseBaseVO = new ClientResponseBaseVO();
        responseBaseVO.setCode(sdkErrCode.getCode());
        responseBaseVO.setMsg(sdkErrCode.getName());
        return responseBaseVO;
    }
}
