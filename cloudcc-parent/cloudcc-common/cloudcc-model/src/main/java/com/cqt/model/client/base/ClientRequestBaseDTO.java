package com.cqt.model.client.base;

import com.cqt.model.client.validategroup.AgentIdGroup;
import com.cqt.model.client.validategroup.ExtIdGroup;
import com.cqt.model.client.validategroup.OsGroup;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-28 17:54
 * SDK基础参数
 */
@Data
public class ClientRequestBaseDTO implements ClientBase, Serializable {

    private static final long serialVersionUID = -4944149241054723334L;

    @JsonProperty("msg_type")
    @NotEmpty(message = "[msg_type]不能为空!")
    private String msgType;

    @JsonProperty("req_id")
    @NotEmpty(message = "[req_id]不能为空!")
    private String reqId;

    @JsonProperty("company_code")
    @NotEmpty(message = "[company_code]不能为空!")
    private String companyCode;

    @JsonProperty("ext_id")
    @NotEmpty(message = "[ext_id]不能为空!", groups = {ExtIdGroup.class})
    private String extId;

    @JsonProperty("agent_id")
    @NotEmpty(message = "[agent_id]不能为空!", groups = {AgentIdGroup.class})
    private String agentId;

    /**
     * 终端
     */
    @NotEmpty(message = "[os]不能为空!", groups = {OsGroup.class})
    private String os;

    private String token;
}
