package com.cqt.sdk.client.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.base.contants.CommonConstant;
import com.cqt.base.enums.SdkErrCode;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.service.CallContextService;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.mapper.IvrServiceInfoMapper;
import com.cqt.mapper.SkillInfoMapper;
import com.cqt.model.agent.dto.AgentCheckInRecord;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.client.vo.ClientRequestVO;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.freeswitch.dto.api.GetExtensionRegStatusDTO;
import com.cqt.model.freeswitch.vo.GetExtensionRegVO;
import com.cqt.model.queue.dto.AgentWeightInfoDTO;
import com.cqt.model.queue.entity.IvrServiceInfo;
import com.cqt.model.skill.entity.SkillInfo;
import com.cqt.sdk.client.service.DataQueryService;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-07-12 14:59
 * 数据查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataQueryServiceImpl implements DataQueryService {

    private final RedissonUtil redissonUtil;

    private final CallContextService callContextService;

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    private final ObjectMapper objectMapper;

    private final SkillInfoMapper skillInfoMapper;

    private final IvrServiceInfoMapper ivrServiceInfoMapper;

    @Override
    public Optional<AgentStatusDTO> getAgentStatusDTO(String companyCode, String agentId) {

        return commonDataOperateService.getActualAgentStatus(companyCode, agentId);
    }

    @Override
    public AgentInfo getAgentInfo(String companyCode, String agentId) {
        return commonDataOperateService.getAgentInfo(companyCode, agentId);
    }

    @Override
    public GetExtensionRegVO getExtRealRegStatus(String companyCode, String extId) {
        GetExtensionRegStatusDTO param = new GetExtensionRegStatusDTO();
        param.setExtId(extId);
        param.setReqId(IdUtil.objectId());
        param.setCompanyCode(companyCode);
        GetExtensionRegVO response = freeswitchRequestService.getExtensionReg(param);
        log.info("企业: {}, 分机: {} 当前的注册信息: {}", companyCode, extId, response);
        return response;
    }

    @Override
    public ExtStatusDTO getActualExtStatus(String companyCode, String extId) {

        return commonDataOperateService.getActualExtStatus(companyCode, extId);
    }

    @Override
    public AgentCheckInRecord getAgentCheckInRecord(String agentId, String os) {
        String key = CacheUtil.getCheckInRecordKey(agentId);
        try {
            String json = redissonUtil.getHashByItem(key, os);
            if (StringUtils.hasText(json)) {
                return objectMapper.readValue(json, AgentCheckInRecord.class);
            }
        } catch (Exception e) {
            log.error("[查询坐席签入记录] key: {}, 异常: ", key, e);
        }
        return null;
    }

    @Override
    public Set<String> getInCallNumbers(AgentStatusDTO agentStatusDTO) {
        return callContextService.getInCallNumbers(agentStatusDTO);
    }

    @Override
    public ClientRequestVO<List<SkillInfo>> getSkillServiceList(String companyCode, String serviceName) {
        LambdaQueryWrapper<SkillInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SkillInfo::getSkillId, SkillInfo::getSkillName);
        queryWrapper.eq(SkillInfo::getTenantId, companyCode);
        queryWrapper.like(StringUtils.hasText(serviceName), SkillInfo::getSkillName, serviceName);
        return ClientRequestVO.response(companyCode, skillInfoMapper.selectList(queryWrapper), SdkErrCode.OK);
    }

    @Override
    public ClientRequestVO<List<IvrServiceInfo>> getIvrServiceList(String companyCode, String serviceName) {
        LambdaQueryWrapper<IvrServiceInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(IvrServiceInfo::getId, IvrServiceInfo::getServiceName);
        queryWrapper.eq(IvrServiceInfo::getCompanyCode, companyCode);
        queryWrapper.like(StringUtils.hasText(serviceName), IvrServiceInfo::getServiceName, serviceName);
        return ClientRequestVO.response(companyCode, ivrServiceInfoMapper.selectList(queryWrapper), SdkErrCode.OK);
    }

    @Override
    public boolean isDealAgentStatus(String targetStatus, String sourceStatus) {
        // not null
        if (StrUtil.isNotBlank(targetStatus) && StrUtil.isNotBlank(sourceStatus)) {
            if (AgentStatusEnum.FREE.name().equals(targetStatus)
                    && AgentStatusEnum.FREE.name().equals(sourceStatus)) {
                return false;
            }
            if (AgentStatusEnum.BUSY.name().equals(targetStatus)
                    && AgentStatusEnum.BUSY.name().equals(sourceStatus)) {
                return false;
            }
            if (AgentStatusEnum.REST.name().equals(targetStatus)
                    && AgentStatusEnum.REST.name().equals(sourceStatus)) {
                return false;
            }
            if (AgentStatusEnum.OFFLINE.name().equals(targetStatus)
                    && AgentStatusEnum.OFFLINE.name().equals(sourceStatus)) {
                return false;
            }
            return !AgentStatusEnum.ARRANGE.name().equals(targetStatus)
                    || !AgentStatusEnum.ARRANGE.name().equals(sourceStatus);
        }
        return true;
    }

    @Override
    public void initCallStats(String companyCode, String agentId, AgentWeightInfoDTO agentWeightInfoDTO) {
        Optional<Set<String>> skillIdSetOptional = commonDataOperateService.getSkillIdFromAgentWeight(agentWeightInfoDTO);

        // 当天通话次数
        String date = DateUtil.format(DateUtil.date(), CommonConstant.DATE_FORMAT);
        String agentCallCountKey = CacheUtil.getAgentCallCountKey(companyCode, date);
        redissonUtil.addScoreZsetIfAbsent(agentCallCountKey, agentId, 0D);
        if (skillIdSetOptional.isPresent()) {
            Set<String> skillIdSet = skillIdSetOptional.get();
            for (String skillId : skillIdSet) {
                String agentSkillCallCountKey = CacheUtil.getAgentSkillCallCountKey(companyCode, skillId, date);
                redissonUtil.addScoreZsetIfAbsent(agentSkillCallCountKey, agentId, 0D);
            }
        }

        // 当天通话时长
        String agentCallTimeKey = CacheUtil.getAgentCallTimeKey(companyCode, date);
        redissonUtil.addScoreZsetIfAbsent(agentCallTimeKey, agentId, 0D);
        if (skillIdSetOptional.isPresent()) {
            Set<String> skillIdSet = skillIdSetOptional.get();
            for (String skillId : skillIdSet) {
                String agentSkillCallTimeKey = CacheUtil.getAgentSkillCallTimeKey(companyCode, skillId, date);
                redissonUtil.addScoreZsetIfAbsent(agentSkillCallTimeKey, agentId, 0D);
            }
        }
    }
}
