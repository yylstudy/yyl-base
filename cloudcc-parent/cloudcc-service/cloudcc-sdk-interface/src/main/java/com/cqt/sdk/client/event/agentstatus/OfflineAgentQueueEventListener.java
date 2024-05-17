package com.cqt.sdk.client.event.agentstatus;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.sdk.client.service.DataStoreService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-07-18 10:57
 * 企业离线坐席队列增删
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OfflineAgentQueueEventListener implements ApplicationListener<OfflineAgentQueueEvent> {

    private final DataStoreService dataStoreService;

    private final CommonDataOperateService commonDataOperateService;

    @SneakyThrows
    @Override
    public void onApplicationEvent(OfflineAgentQueueEvent event) {
        OperateTypeEnum operateTypeEnum = event.getOperateTypeEnum();
        String companyCode = event.getCompanyCode();
        String agentId = event.getAgentId();
        Long timestamp = event.getTimestamp();
        try {
            AgentInfo agentInfo = commonDataOperateService.getAgentInfo(companyCode, agentId);
            AgentServiceModeEnum serviceMode = getServiceMode(companyCode, agentId, event.getServiceMode());
            if (Objects.isNull(serviceMode)) {
                return;
            }
            if (OperateTypeEnum.INSERT.equals(operateTypeEnum)) {
                Integer offlineAgent = agentInfo.getOfflineAgent();
                if (Objects.isNull(offlineAgent)) {
                    return;
                }
                if (1 != offlineAgent) {
                    return;
                }
                String phoneNumber = agentInfo.getPhoneNumber();
                if (StrUtil.isEmpty(phoneNumber)) {
                    return;
                }
                log.info("[企业离线坐席队列监听-新增] 模式: {}, 企业id: {}, 坐席id: {}, 空闲时间戳: {}, 手机号: {}",
                        serviceMode.getName(), companyCode, agentId, timestamp, phoneNumber);
                dataStoreService.addAgentQueue(companyCode, agentId, serviceMode, false,
                        timestamp, phoneNumber);
                return;
            }
            if (OperateTypeEnum.DELETE.equals(operateTypeEnum)) {
                dataStoreService.deleteAgentQueue(companyCode, agentId, serviceMode, false);
                log.info("[企业离线坐席队列监听-删除] 模式: {}, 企业id: {}, 坐席id: {}",
                        serviceMode.getName(), companyCode, agentId);
            }
        } catch (Exception e) {
            log.error("[企业离线坐席队列增删事件监听] 处理异常: ", e);
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
