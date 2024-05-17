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
 * SDK 管理员 监听, 耳语, 代接, 强插, 强拆参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientAdminXferDTO extends ClientRequestBaseDTO implements Serializable {

    /**
     * 是 被操作的分机ID
     */
    @JsonProperty("operated_ext_id")
    @NotEmpty(message = "[operated_ext_id]不能为空!")
    private String operatedExtId;

    /**
     * 是 被操作的分机ID
     */
    @JsonProperty("operated_agent_id")
    @NotEmpty(message = "[operated_agent_id]不能为空!")
    private String operatedAgentId;
}
