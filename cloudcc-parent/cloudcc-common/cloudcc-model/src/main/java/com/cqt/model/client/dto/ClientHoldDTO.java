package com.cqt.model.client.dto;

import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-05 14:28
 * SDK 保持/取消保持参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientHoldDTO extends ClientRequestBaseDTO implements Serializable {

    /**
     * 是(0否1是) 保持/取回
     */
    @JsonProperty("hold")
    @NotNull(message = "[hold]不能为空!")
    private String hold;

    /**
     * 是 需要保持的通话uuid
     */
    @NotEmpty(message = "[uuid]不能为空!")
    private String uuid;

}
