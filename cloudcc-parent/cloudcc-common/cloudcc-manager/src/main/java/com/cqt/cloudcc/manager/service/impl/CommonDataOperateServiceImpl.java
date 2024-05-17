package com.cqt.cloudcc.manager.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.cqt.base.enums.CallDirectionEnum;
import com.cqt.base.enums.DefaultToneEnum;
import com.cqt.base.enums.IdleStrategyEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.base.enums.calltask.CallTaskEnum;
import com.cqt.cloudcc.manager.context.AgentInfoContext;
import com.cqt.cloudcc.manager.context.CompanyInfoContext;
import com.cqt.cloudcc.manager.service.*;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.ext.entity.ExtInfo;
import com.cqt.model.number.entity.NumberInfo;
import com.cqt.model.queue.dto.AgentCheckinCacheDTO;
import com.cqt.model.queue.dto.AgentWeightInfoDTO;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.model.queue.entity.IvrServiceInfo;
import com.cqt.model.skill.entity.SkillInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-08-22 9:34
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommonDataOperateServiceImpl implements CommonDataOperateService {

    private final CompanyInfoService companyInfoService;

    private final NumberInfoService numberInfoService;

    private final SkillInfoService skillInfoService;

    private final AgentInfoService agentInfoService;

    private final ExtInfoService extInfoService;

    private final CallContextService callContextService;

    private final IvrServiceInfoService ivrServiceInfoService;

    private final OutCallTaskService outCallTaskService;

    private final FileInfoService fileInfoService;

    @Override
    public String getDefaultTone(DefaultToneEnum defaultTone) {
        return companyInfoService.getPlatformDefaultTone(defaultTone);
    }

    @Override
    public CompanyInfo getCompanyInfoDTO(String companyCode) {
        CompanyInfo companyInfo = CompanyInfoContext.get();
        if (Objects.nonNull(companyInfo)) {
            return companyInfo;
        }
        return companyInfoService.getCompanyInfoDTO(companyCode);
    }

    @Override
    public Set<String> getEnableCompanyCode() {
        return companyInfoService.getEnableCompanyCode();
    }

    @Override
    public Set<String> getAllCompanyCode() {
        return companyInfoService.getAllCompanyCode();
    }

    @Override
    public Optional<NumberInfo> getNumberInfo(String number) {
        return numberInfoService.getNumberInfo(number);
    }

    @Override
    public Boolean checkBlackNumber(String companyCode, String number, CallDirectionEnum callDirection) {
        return numberInfoService.checkBlackNumber(companyCode, number, callDirection);
    }

    @Override
    public SkillInfo getSkillInfo(String skillId) {
        return skillInfoService.getSkillInfo(skillId);
    }

    @Override
    public String getFilePath(String companyCode, String fileId) {
        return fileInfoService.getFilePath(companyCode, fileId);
    }

    @Override
    public Integer getClientPriority(String companyCode, String callerNumber) {
        return numberInfoService.getClientPriority(companyCode, callerNumber);
    }

    @Override
    public void addCallTime(String companyCode, String agentId, Long timestamp, Double callDuration) {
        agentInfoService.addCallTime(companyCode, agentId, timestamp, callDuration);
    }

    @Override
    public void addCallTime(String companyCode, String agentId, String skillId, Long timestamp, Double callDuration) {
        agentInfoService.addCallTime(companyCode, agentId, skillId, timestamp, callDuration);
    }

    @Override
    public void addCallCount(String companyCode, String agentId, Long timestamp) {
        agentInfoService.addCallCount(companyCode, agentId, timestamp);
    }

    @Override
    public void addCallCount(String companyCode, String agentId, String skillId, Long timestamp) {
        agentInfoService.addCallCount(companyCode, agentId, skillId, timestamp);
    }

    @Override
    public List<String> getCallTime(String companyCode) {
        return agentInfoService.getCallTime(companyCode);
    }

    @Override
    public List<String> getCallTime(String companyCode, String skillId) {
        return agentInfoService.getCallTime(companyCode, skillId);
    }

    @Override
    public List<String> getCallCount(String companyCode) {
        return agentInfoService.getCallCount(companyCode);
    }

    @Override
    public List<String> getCallCount(String companyCode, String skillId) {
        return agentInfoService.getCallCount(companyCode, skillId);
    }

    @Override
    public Optional<AgentStatusDTO> getActualAgentStatus(String companyCode, String agentId) {
        return agentInfoService.getAgentStatusDTO(companyCode, agentId);
    }

    @Override
    public void updateActualAgentStatus(AgentStatusDTO agentStatusDTO) {
        agentInfoService.updateActualAgentStatus(agentStatusDTO);
    }

    @Override
    public AgentInfo getAgentInfo(String companyCode, String agentId) {
        AgentInfo agentInfo = AgentInfoContext.get();
        if (Objects.nonNull(agentInfo)) {
            return agentInfo;
        }
        return agentInfoService.getAgentInfo(companyCode, agentId);
    }

    @Override
    public AgentInfo getAgentInfo(String companyCode, String agentId, boolean context) {
        if (context) {
            AgentInfo agentInfo = AgentInfoContext.get();
            if (Objects.nonNull(agentInfo)) {
                return agentInfo;
            }
        }
        return agentInfoService.getAgentInfo(companyCode, agentId, false);
    }

    @Override
    public List<String> getPredictFreeAgentQueue(String companyCode, String taskId) {
        return agentInfoService.getPredictFreeAgentQueue(companyCode, taskId);
    }

    @Override
    public List<TransferAgentQueueDTO> getCompanyAgentQueue(String companyCode, AgentServiceModeEnum agentMode, boolean free) {
        return agentInfoService.getCompanyAgentQueue(companyCode, agentMode, free);
    }

    @Override
    public List<TransferAgentQueueDTO> getSkillAgentQueue(String companyCode,
                                                          String skillId,
                                                          AgentServiceModeEnum agentMode,
                                                          boolean free) {
        return agentInfoService.getSkillAgentQueue(companyCode, skillId, agentMode, free);
    }

    @Override
    public AgentWeightInfoDTO getAgentWithWeightInfo(String agentId) {
        return agentInfoService.getAgentWithWeightInfo(agentId);
    }

    @Override
    public AgentWeightInfoDTO getAgentWithWeightInfo(String companyCode, String agentId) {
        return agentInfoService.getAgentWithWeightInfo(companyCode, agentId);
    }

    @Override
    public Optional<Set<String>> getSkillIdFromAgentWeight(AgentWeightInfoDTO agentWeightInfoDTO) {
        return agentInfoService.getSkillIdFromAgentWeight(agentWeightInfoDTO);
    }

    @Override
    public String getAgentDisplayNumber(String companyCode, String agentId) throws Exception {
        return agentInfoService.getAgentDisplayNumber(companyCode, agentId);
    }

    @Override
    public String getExtIdRelateAgentId(String companyCode, String agentId) {
        return agentInfoService.getExtIdRelateAgentId(companyCode, agentId);
    }

    @Override
    public String getAgentIdRelateExtId(String companyCode, String extId) {
        return agentInfoService.getAgentIdRelateExtId(companyCode, extId);
    }

    @Override
    public ExtInfo getExtInfo(String companyCode, String extId) throws Exception {
        return extInfoService.getExtInfo(companyCode, extId);
    }

    @Override
    public ExtStatusDTO getActualExtStatus(String companyCode, String extId) {
        return extInfoService.getActualExtStatus(companyCode, extId);
    }

    @Override
    public void updateActualExtStatus(ExtStatusDTO extStatusDTO) {
        extInfoService.updateActualExtStatus(extStatusDTO);
    }

    @Override
    public CallUuidContext getCallUuidContext(String companyCode, String uuid) {
        return callContextService.getCallUuidContext(companyCode, uuid);
    }

    @Override
    public void saveCallUuidContext(CallUuidContext callUuidContext) {
        callContextService.saveCallUuidContext(callUuidContext);
    }

    @Override
    public void saveCdrLink(String companyCode, String mainCallId, String sourceUUID, String destUUID) {
        callContextService.saveCdrLink(companyCode, mainCallId, sourceUUID, destUUID);
    }

    @Override
    public Boolean isHangup(String companyCode, String uuid) {
        return callContextService.isHangup(companyCode, uuid);
    }

    @Override
    public Boolean isCdrGenerated(String companyCode, String mainCallId) {
        return callContextService.isCdrGenerated(companyCode, mainCallId);
    }

    @Override
    public void saveCdrPlatformNumber(String companyCode, String mainCallId, String platformNumber) {
        callContextService.saveCdrPlatformNumber(companyCode, mainCallId, platformNumber);
    }

    @Override
    public String getPlatformNumber(String companyCode, String mainCallId) {
        return callContextService.getPlatformNumber(companyCode, mainCallId);
    }

    @Override
    public String createMainCallId() {
        return callContextService.createMainCallId();
    }

    @Override
    public Boolean saveConsultFlag(String uuid) {
        return callContextService.saveConsultFlag(uuid);
    }

    @Override
    public IvrServiceInfo getIvrServiceInfo(String serviceId) {
        return ivrServiceInfoService.getIvrServiceInfo(serviceId);
    }

    @Override
    public void removeNumber(String taskId, String member, CallTaskEnum callTaskEnum) {
        outCallTaskService.removeNumber(taskId, member, callTaskEnum);
    }

    @Override
    public void saveRecordFile(String companyCode, String uuid, String filePath) {
        callContextService.saveRecordFile(companyCode, uuid, filePath);
    }

    @Override
    public String getRecordFile(String companyCode, String uuid) {
        return callContextService.getRecordFile(companyCode, uuid);
    }

    @Override
    public String createToken(String companyCode, String agentId, String os) {
        String token = SecureUtil.hmacMd5(IdUtil.fastSimpleUUID()).digestHex(companyCode + os + agentId);
        saveToken(companyCode, agentId, os, token);
        return token;
    }

    @Override
    public void saveToken(String companyCode, String agentId, String os, String token) {
        callContextService.saveToken(companyCode, agentId, os, token);
    }

    @Override
    public boolean checkToken(String sdkToken) {
        return callContextService.checkToken(sdkToken);
    }

    @Override
    public TransferAgentQueueDTO popFreeAgentQueue(String companyCode,
                                                   String skillId,
                                                   AgentServiceModeEnum agentServiceModeEnum,
                                                   IdleStrategyEnum idleStrategyEnum) {

        return agentInfoService.popFreeQueue(companyCode, skillId, agentServiceModeEnum, idleStrategyEnum);
    }

    @Override
    public void dealAgentCheckinCache(AgentCheckinCacheDTO agentCheckinCacheDTO) {
        agentInfoService.dealAgentCheckinCache(agentCheckinCacheDTO);
    }
}
