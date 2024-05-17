package com.cqt.model.freeswitch.dto.api;

import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Xienx
 * @date 2023-07-24 17:05:17:05
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DisExtensionRegAddrDTO extends FreeswitchApiBase {

    private static final long serialVersionUID = 8784348952701316551L;

    /**
     * 分机号
     */
    @JsonProperty("ext_id")
    private String extId;
}
