package com.cqt.sdk.client.converter;

import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.dto.AgentStatusTransferDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author linshiqiang
 * date:  2023-07-03 17:51
 */
@Mapper(componentModel = "spring")
public interface ModelConverter {

    ModelConverter INSTANCE = Mappers.getMapper(ModelConverter.class);

    /**
     * 坐席状态实时状态 -> 坐席状态迁移日志实体
     */
    AgentStatusTransferDTO status2transfer(AgentStatusDTO agentStatusDTO);
}
