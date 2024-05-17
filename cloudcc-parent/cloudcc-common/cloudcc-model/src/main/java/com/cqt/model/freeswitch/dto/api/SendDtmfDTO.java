package com.cqt.model.freeswitch.dto.api;

import com.cqt.model.client.dto.ClientDtmfDTO;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 10:28
 * 二次拨号流程，往某路发送按键接口
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SendDtmfDTO extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = -1629274644447710954L;

    /**
     * 是  | 按键内容
     */
    @JsonProperty("content")
    private String content;

    /**
     * 否  | 【0:info，1:inban，3:2833】默认3
     */
    @JsonProperty("dtmf_type")
    private Integer dtmfType;

    /**
     * 是  | 通话ID
     */
    @JsonProperty("uuid")
    private String uuid;

    /**
     * 构建
     * clientDtmfDTO -> SendDtmfDTO
     */
    public static SendDtmfDTO build(ClientDtmfDTO clientDtmfDTO) {
        SendDtmfDTO sendDtmfDTO = new SendDtmfDTO();
        sendDtmfDTO.setReqId(clientDtmfDTO.getReqId());
        sendDtmfDTO.setDtmfType(clientDtmfDTO.getDtmfType());
        sendDtmfDTO.setUuid(clientDtmfDTO.getUuid());
        sendDtmfDTO.setContent(clientDtmfDTO.getContent());
        sendDtmfDTO.setCompanyCode(clientDtmfDTO.getCompanyCode());
        return sendDtmfDTO;
    }
}
