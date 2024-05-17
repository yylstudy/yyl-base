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
 * SDK 咨询中转接
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientConsultToTransDTO extends ClientRequestBaseDTO implements Serializable {

    /**
     * 被咨询方uuid
     */
    @JsonProperty("consult_uuid")
    @NotEmpty(message = "[consult_uuid]不能为空!")
    private String consultUuid;
}
