package com.cqt.model.queue.dto;

import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.model.agent.entity.AgentInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-12-01 13:57
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentCheckinCacheDTO implements Serializable {

    private static final long serialVersionUID = 6206521856905761245L;

    private String companyCode;

    private String agentId;

    private Integer offlineAgent;

    private String phoneNumber;

    private AgentWeightInfoDTO agentWeightInfoDTO;

    private OperateTypeEnum operateTypeEnum;

    private String msg;

    public static AgentCheckinCacheDTO buildNew(AgentInfo agentInfo, AgentWeightInfoDTO agentWeightInfoDTO) {
        return AgentCheckinCacheDTO.builder()
                .companyCode(agentInfo.getTenantId())
                .agentId(agentInfo.getSysAgentId())
                .offlineAgent(agentInfo.getOfflineAgent())
                .phoneNumber(agentInfo.getPhoneNumber())
                .agentWeightInfoDTO(agentWeightInfoDTO)
                .operateTypeEnum(OperateTypeEnum.INSERT)
                .build();
    }

    public static AgentCheckinCacheDTO buildDel(String companyCode, String agentId) {
        return AgentCheckinCacheDTO.builder()
                .companyCode(companyCode)
                .agentId(agentId)
                .operateTypeEnum(OperateTypeEnum.DELETE)
                .build();
    }
}
