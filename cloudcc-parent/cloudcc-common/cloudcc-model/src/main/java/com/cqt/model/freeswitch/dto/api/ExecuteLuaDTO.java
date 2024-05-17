package com.cqt.model.freeswitch.dto.api;

import cn.hutool.core.util.IdUtil;
import com.cqt.model.client.dto.ClientTransDTO;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:02
 * 通过接口调用FS执行lua脚本
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExecuteLuaDTO extends FreeswitchApiBase implements Serializable {

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
     * clientTransDTO -> ExecuteLuaDTO
     */
    public static ExecuteLuaDTO build(ClientTransDTO clientTransDTO, String uuid, String fileName) {
        ExecuteLuaDTO executeLuaDTO = new ExecuteLuaDTO();
        executeLuaDTO.setReqId(clientTransDTO.getReqId());
        executeLuaDTO.setUuid(uuid);
        executeLuaDTO.setCompanyCode(clientTransDTO.getCompanyCode());
        executeLuaDTO.setFileName(fileName);
        return executeLuaDTO;
    }

    /**
     * 对象转化
     * clientTransDTO -> ExecuteLuaDTO
     */
    public static ExecuteLuaDTO build(String companyCode, String uuid, String fileName) {
        ExecuteLuaDTO executeLuaDTO = new ExecuteLuaDTO();
        executeLuaDTO.setReqId(IdUtil.fastUUID());
        executeLuaDTO.setUuid(uuid);
        executeLuaDTO.setCompanyCode(companyCode);
        executeLuaDTO.setFileName(fileName);
        return executeLuaDTO;
    }
}
