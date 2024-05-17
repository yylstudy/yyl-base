package com.cqt.model.freeswitch.dto.api;

import com.cqt.model.client.dto.ClientTransDTO;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:02
 * [特殊接口]呼入事件收到之后，接听电话，然后执行lua，走IVR流程
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CallIvrLuaDTO extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = 5825845585171704309L;

    /**
     * 是  | 通话ID
     */
    private String uuid;

    /**
     * 是  | 执行lua的文件名
     */
    @JsonProperty("file_name")
    private String fileName;

    /**
     * 对象转化
     * clientTransDTO -> CallIvrLuaDTO
     */
    public static CallIvrLuaDTO build(ClientTransDTO clientTransDTO, String uuid, String fileName) {
        CallIvrLuaDTO callIvrLuaDTO = new CallIvrLuaDTO();
        callIvrLuaDTO.setReqId(clientTransDTO.getReqId());
        callIvrLuaDTO.setUuid(uuid);
        callIvrLuaDTO.setCompanyCode(clientTransDTO.getCompanyCode());
        callIvrLuaDTO.setFileName(fileName);
        return callIvrLuaDTO;
    }

    /**
     * 构造对象
     */
    public static CallIvrLuaDTO build(String reqId, String companyCode, String uuid, String fileName) {
        CallIvrLuaDTO callIvrLuaDTO = new CallIvrLuaDTO();
        callIvrLuaDTO.setReqId(reqId);
        callIvrLuaDTO.setUuid(uuid);
        callIvrLuaDTO.setCompanyCode(companyCode);
        callIvrLuaDTO.setFileName(fileName);
        return callIvrLuaDTO;
    }
}
