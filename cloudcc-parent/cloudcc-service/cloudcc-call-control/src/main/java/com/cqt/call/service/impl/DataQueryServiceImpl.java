package com.cqt.call.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.contants.SystemConstant;
import com.cqt.base.enums.CallDirectionEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.call.service.DataQueryService;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-07-05 16:28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataQueryServiceImpl implements DataQueryService {

    private final Snowflake snowflake;

    private final RedissonUtil redissonUtil;

    private final RedissonClient redissonClient;

    private final CommonDataOperateService commonDataOperateService;

    private final ObjectMapper objectMapper;

    @Override
    public AgentInfo getAgentInfo(String companyCode, String agentId) throws Exception {
        return commonDataOperateService.getAgentInfo(companyCode, agentId);
    }

    @Override
    public AgentInfo getAgentInfo(String companyCode, String agentId, boolean context) throws Exception {
        return commonDataOperateService.getAgentInfo(companyCode, agentId, context);
    }

    @Override
    public String getAgentIdRelateExtId(String companyCode, String extId) {
        return commonDataOperateService.getAgentIdRelateExtId(companyCode, extId);
    }

    @Override
    public ExtStatusDTO getActualExtStatus(String companyCode, String extId) {
        return commonDataOperateService.getActualExtStatus(companyCode, extId);
    }

    @Override
    public Optional<AgentStatusDTO> getActualAgentStatus(String companyCode, String agentId) {
        return commonDataOperateService.getActualAgentStatus(companyCode, agentId);
    }

    @Override
    public CallUuidContext getCallUuidContext(String companyCode, String uuid) {
        return commonDataOperateService.getCallUuidContext(companyCode, uuid);
    }

    @Override
    public CompanyInfo getCompanyInfoDTO(String companyCode) throws Exception {
        return commonDataOperateService.getCompanyInfoDTO(companyCode);
    }

    @Override
    public String getAgentDisplayNumber(CompanyInfo companyInfo, AgentInfo agentInfo) {
        String displayNumber = agentInfo.getDisplayNumber();
        if (StrUtil.isNotEmpty(displayNumber)) {
            return displayNumber;
        }
        // 企业主显号码应该不会为空吧
        return companyInfo.getMainDisplayNumber();
    }

    @Override
    public String getAgentDisplayNumber(String companyCode, String agentId) throws Exception {
        return commonDataOperateService.getAgentDisplayNumber(companyCode, agentId);
    }

    @Override
    public String getIvrName(String companyCode, String ivrId, Boolean satisfaction) {
        if (satisfaction) {
            // 满意度ivr文件格式: {company_code}_9个9.lua
            return StrFormatter.format(SystemConstant.SATISFACTION_LUA_TEMPLATE, companyCode);
        }
        return StrFormatter.format(SystemConstant.LUA_TEMPLATE, ivrId);
    }

    @Override
    public Set<String> getCdrLink(String companyCode, String mainCallId) {
        String cdrLinkKey = CacheUtil.getCdrLinkKey(companyCode, mainCallId);
        return redissonUtil.getSet(cdrLinkKey);
    }

    @Override
    public Map<String, Object> getCdrChannelData(String companyCode, String uuid) {
        try {
            String cdrChannelDataKey = CacheUtil.getCdrChannelDataKey(companyCode, uuid);
            String data = redissonUtil.get(cdrChannelDataKey);
            if (StrUtil.isEmpty(data)) {
                return null;
            }
            return objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("[查询通道变量] 异常: ", e);
        }
        return null;
    }

    @Override
    public String createMainCallId() {
        return snowflake.nextIdStr();
    }

    @Override
    public Boolean isHangup(String companyCode, String uuid) {
        return commonDataOperateService.isHangup(companyCode, uuid);
    }

    @Override
    public Boolean isCdrGenerated(String companyCode, String mainCallId) {
        return commonDataOperateService.isCdrGenerated(companyCode, mainCallId);
    }

    @Override
    public Boolean checkBlackNumber(String companyCode, String number, CallDirectionEnum callDirection) {
        return commonDataOperateService.checkBlackNumber(companyCode, number, callDirection);
    }
}
