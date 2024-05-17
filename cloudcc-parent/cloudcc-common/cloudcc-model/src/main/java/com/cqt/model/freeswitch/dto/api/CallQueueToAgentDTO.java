package com.cqt.model.freeswitch.dto.api;

import com.cqt.model.client.dto.ClientTransDTO;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:02
 * 排队找到空闲坐席，呼叫坐席前调用，避免检测到超时，分机还未接起，就停止排队了
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CallQueueToAgentDTO extends FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = 5825845585171704309L;

    /**
     * 是  | 通话ID
     */
    private String uuid;

    /**
     * 是  | 分机ID
     */
    @JsonProperty("ext_id")
    private String extId;

    /**
     * 对象转化
     * clientTransDTO -> CallQueueToAgentDTO
     */
    public static CallQueueToAgentDTO build(ClientTransDTO clientTransDTO, String uuid, String extId) {
        CallQueueToAgentDTO callQueueToAgentDTO = new CallQueueToAgentDTO();
        callQueueToAgentDTO.setReqId(clientTransDTO.getReqId());
        callQueueToAgentDTO.setUuid(uuid);
        callQueueToAgentDTO.setCompanyCode(clientTransDTO.getCompanyCode());
        callQueueToAgentDTO.setExtId(extId);
        return callQueueToAgentDTO;
    }

    /**
     * 对象构建
     */
    public static CallQueueToAgentDTO build(String reqId, String companyCode, String uuid, String extId) {
        CallQueueToAgentDTO callQueueToAgentDTO = new CallQueueToAgentDTO();
        callQueueToAgentDTO.setReqId(reqId);
        callQueueToAgentDTO.setUuid(uuid);
        callQueueToAgentDTO.setCompanyCode(companyCode);
        callQueueToAgentDTO.setExtId(extId);
        return callQueueToAgentDTO;
    }
}
