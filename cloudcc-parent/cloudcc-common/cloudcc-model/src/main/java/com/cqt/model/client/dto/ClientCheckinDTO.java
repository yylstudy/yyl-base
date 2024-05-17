package com.cqt.model.client.dto;

import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-28 17:50
 * SDK 签入入参
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientCheckinDTO extends ClientRequestBaseDTO implements Serializable {

    private static final long serialVersionUID = 2815049436930620779L;

    @JsonProperty("agent_ip")
    private String agentIp;

    @JsonProperty("agent_pwd")
    @NotEmpty(message = "[agent_pwd]不能为空")
    private String agentPwd;

    @JsonProperty("start_status")
    private String startStatus;

    /**
     * 复位分机
     */
    private Boolean reset;

}
