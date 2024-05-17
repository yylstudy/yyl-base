package com.cqt.model.client.dto;

import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-05 14:28
 * SDK 切换坐席服务模式
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientToggleServiceModeDTO extends ClientRequestBaseDTO implements Serializable {

    /**
     * 切换后的服务模式 1-客服型 2-外呼型
     */
    @JsonProperty("service_mode")
    @NotNull(message = "[service_mode]不能为空")
    private Integer serviceMode;
}
