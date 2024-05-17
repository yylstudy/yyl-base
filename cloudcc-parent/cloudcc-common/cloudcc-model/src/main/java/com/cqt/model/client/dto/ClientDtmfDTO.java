package com.cqt.model.client.dto;

import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-05 14:28
 * SDK 二次拨号（DTMF）参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientDtmfDTO extends ClientRequestBaseDTO implements Serializable {

    /**
     * 是 需要挂断的通话uuid
     */
    @NotEmpty(message = "[uuid]不能为空!")
    private String uuid;

    /**
     * 是 二次拨号内容
     */
    @NotEmpty(message = "[content]不能为空!")
    private String content;

    /**
     * 否 默认3
     */
    @JsonProperty("dtmf_type")
    private Integer dtmfType;
}
