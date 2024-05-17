package com.cqt.sdk.client.event.agentstatus;

import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.sdk.client.service.DataStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-07-18 10:57
 * 企业空闲坐席队列增删
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FreeAgentQueueEventListener implements ApplicationListener<FreeAgentQueueEvent> {

    private final DataStoreService dataStoreService;

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public void onApplicationEvent(FreeAgentQueueEvent event) {
        OperateTypeEnum operateTypeEnum = event.getOperateTypeEnum();
        String companyCode = event.getCompanyCode();
        String agentId = event.getAgentId();
        Long timestamp = event.getTimestamp();

        AgentServiceModeEnum serviceMode = getServiceMode(companyCode, agentId, event.getServiceMode());
        if (Objects.isNull(serviceMode)) {
            return;
        }

        log.info("[企业空闲坐席队列增删事件监听] 操作类型: {}, 企业id: {}, 坐席id: {}, 空闲时间戳: {}",
                operateTypeEnum, companyCode, agentId, timestamp);
        try {
            if (OperateTypeEnum.INSERT.equals(operateTypeEnum)) {
                dataStoreService.deleteAgentQueue(companyCode, agentId, serviceMode, false);
                dataStoreService.addAgentQueue(companyCode, agentId, serviceMode, true, timestamp, "");
                log.info("[企业空闲坐席-新增] 模式: {}, 企业: {}, 坐席: {}", serviceMode.getName(), companyCode, agentId);
                return;
            }
            if (OperateTypeEnum.DELETE.equals(operateTypeEnum)) {
                dataStoreService.deleteAgentQueue(companyCode, agentId, serviceMode, true);
                log.info("[企业空闲坐席-删除] 模式: {}, 企业: {}, 坐席: {}", serviceMode.getName(), companyCode, agentId);
            }
        } catch (Exception e) {
            log.error("[企业空闲坐席队列增删事件监听] 处理异常: ", e);
        }
    }

    private AgentServiceModeEnum getServiceMode(String companyCode, String agentId, AgentServiceModeEnum serviceMode) {
        if (Objects.nonNull(serviceMode)) {
            return serviceMode;
        }
        Optional<AgentStatusDTO> agentStatusOptional = commonDataOperateService.getActualAgentStatus(companyCode, agentId);
        return agentStatusOptional
                .map(agentStatusDTO -> AgentServiceModeEnum.parse(agentStatusDTO.getServiceMode()))
                .orElse(null);
    }
}
