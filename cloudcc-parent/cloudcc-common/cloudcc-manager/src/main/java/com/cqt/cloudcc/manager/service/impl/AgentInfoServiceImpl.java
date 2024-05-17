package com.cqt.cloudcc.manager.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.base.contants.CacheConstant;
import com.cqt.base.contants.CommonConstant;
import com.cqt.base.enums.IdleStrategyEnum;
import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.cache.AgentCheckinCache;
import com.cqt.cloudcc.manager.context.AgentInfoContext;
import com.cqt.cloudcc.manager.service.AgentInfoService;
import com.cqt.cloudcc.manager.service.CompanyInfoService;
import com.cqt.mapper.AgentInfoMapper;
import com.cqt.mapper.task.PredictOutboundTaskAgentMapper;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.agent.entity.AgentSkill;
import com.cqt.model.calltask.entity.PredictOutboundTaskAgent;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.queue.dto.AgentCheckinCacheDTO;
import com.cqt.model.queue.dto.AgentWeightInfoDTO;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-08-22 9:39
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentInfoServiceImpl implements AgentInfoService {

    private final RedissonUtil redissonUtil;

    private final ObjectMapper objectMapper;

    private final AgentInfoMapper agentInfoMapper;

    private final PredictOutboundTaskAgentMapper predictOutboundTaskAgentMapper;

    private final CompanyInfoService companyInfoService;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    @Override
    public AgentInfo getAgentInfo(String companyCode, String agentId) {
        AgentInfo agentInfoCtx = AgentInfoContext.get();
        if (Objects.nonNull(agentInfoCtx)) {
            return agentInfoCtx;
        }

        return getAgentInfo(agentId);
    }

    @Override
    public AgentInfo getAgentInfo(String companyCode, String agentId, boolean context) {
        if (context) {
            AgentInfo agentInfoCtx = AgentInfoContext.get();
            if (Objects.nonNull(agentInfoCtx)) {
                return agentInfoCtx;
            }
        }

        return getAgentInfo(agentId);
    }

    private AgentInfo getAgentInfo(String agentId) {
        String agentInfoKey = CacheUtil.getAgentInfoKey(agentId);
        try {
            AgentInfo agentInfo = redissonUtil.get(agentInfoKey, AgentInfo.class);
            if (Objects.nonNull(agentInfo)) {
                return agentInfo;
            }
        } catch (Exception e) {
            log.error("[查询坐席信息] key: {}, 异常: ", agentInfoKey, e);
        }
        // 异常或为空查db
        AgentInfo agentInfo = agentInfoMapper.selectById(agentId);
        if (Objects.nonNull(agentInfo)) {
            try {
                boolean set = redissonUtil.setString(agentInfoKey, objectMapper.writeValueAsString(agentInfo));
                log.info("[查询坐席信息] agentId: {}, 查询db, 回写redis: {}", agentId, set);
            } catch (JsonProcessingException e) {
                log.error("[查询坐席信息] agentId: {}, 查询db, 回写redis error: ", agentId, e);
            }
        }
        return agentInfo;
    }

    @Override
    public Optional<AgentStatusDTO> getAgentStatusDTO(String companyCode, String agentId) {
        String agentStatusKey = CacheUtil.getAgentStatusKey(companyCode, agentId);
        try {
            AgentStatusDTO agentStatusDTO = redissonUtil.get(agentStatusKey, AgentStatusDTO.class);
            return Optional.ofNullable(agentStatusDTO);
        } catch (Exception e) {
            log.error("[查询坐席状态] 企业id: {}, 坐席id: {}, 异常: ", companyCode, agentId, e);
        }
        return Optional.empty();
    }

    @Override
    public void updateActualAgentStatus(AgentStatusDTO agentStatusDTO) {
        String companyCode = agentStatusDTO.getCompanyCode();
        String agentId = agentStatusDTO.getAgentId();
        String agentStatusUpdateLockKey = CacheUtil.getAgentStatusUpdateLockKey(companyCode, agentId);
        RLock fairLock = redissonUtil.getFairLock(agentStatusUpdateLockKey);
        fairLock.lock(cloudCallCenterProperties.getLockTIme().toMillis(), TimeUnit.MILLISECONDS);
        try {
            String agentStatusKey = CacheUtil.getAgentStatusKey(companyCode, agentId);
            String agentStatusJson = objectMapper.writeValueAsString(agentStatusDTO);
            boolean set = redissonUtil.set(agentStatusKey, agentStatusJson);
            log.info("[坐席状态-缓存] target: {}, key: {}, 保存redis结果: {}", agentStatusDTO.getTargetStatus(), agentStatusKey, set);
            if (AgentStatusEnum.FREE.name().equals(agentStatusDTO.getTargetStatus())) {
                String originateLockKey = CacheUtil.getOriginateLockKey(companyCode + StrUtil.COLON + agentId);
                redissonUtil.delKey(originateLockKey);
            }
            // 设置坐席状态到hash-实时监控使用
            String key = CacheUtil.getAllAgentStatusKey(companyCode);
            AgentStatusDTO copy = AgentStatusDTO.copy(agentStatusDTO);
            copy.removeUselessField();
            boolean setHash = redissonUtil.setHash(key, agentId, objectMapper.writeValueAsString(copy));
            log.info("[坐席状态-缓存] target: {}, key: {}, 保存redis结果: {}", agentStatusDTO.getTargetStatus(), agentStatusKey, setHash);
        } catch (Exception e) {
            log.error("[修改坐席状态] key: {}, 异常: ", agentStatusUpdateLockKey, e);
        } finally {
            try {
                fairLock.unlock();
            } catch (Exception e) {
                log.error("[updateActualAgentStatus] fairLock unlock error: ", e);
            }
        }
    }

    @Override
    public String getAgentIdRelateExtId(String companyCode, String extId) {
        String agentBindExtKey = CacheUtil.getAgentBindExtKey(companyCode, extId);
        try {
            String agentId = redissonUtil.getString(agentBindExtKey);
            if (StrUtil.isNotEmpty(agentId)) {
                return agentId;
            }
        } catch (Exception e) {
            log.error("[查询坐席关联的分机] key: {}, 异常: ", agentBindExtKey, e);
        }
        String agentIdByExtId = agentInfoMapper.getAgentIdByExtId(extId);
        if (StrUtil.isNotEmpty(agentIdByExtId)) {
            boolean set = redissonUtil.set(agentBindExtKey, agentIdByExtId);
            log.info("[查询坐席关联的分机] extId: {},  agentId: {}, 查询db, 回写redis: {}", extId, agentIdByExtId, set);
        }
        return agentIdByExtId;
    }

    @Override
    public String getExtIdRelateAgentId(String companyCode, String agentId) {
        String extBindAgentKey = CacheUtil.getExtBindAgentKey(companyCode, agentId);
        try {
            String extId = redissonUtil.getString(extBindAgentKey);
            if (StrUtil.isNotEmpty(extId)) {
                return extId;
            }
        } catch (Exception e) {
            log.error("[查询分机关联的坐席] key: {}, 异常: ", extBindAgentKey, e);
        }
        String extIdByAgentId = agentInfoMapper.getExtIdByAgentId(agentId);
        if (StrUtil.isNotEmpty(extIdByAgentId)) {
            boolean set = redissonUtil.set(extBindAgentKey, extIdByAgentId);
            log.info("[查询分机关联的坐席] agentId: {}, extId: {}, 查询db, 回写redis: {}", agentId, extIdByAgentId, set);
        }
        return extIdByAgentId;
    }

    @Override
    public String getAgentDisplayNumber(String companyCode, String agentId) {
        AgentInfo agentInfo = getAgentInfo(companyCode, agentId);
        if (Objects.nonNull(agentInfo)) {
            String displayNumber = agentInfo.getDisplayNumber();
            if (StrUtil.isNotEmpty(displayNumber)) {
                return displayNumber;
            }
        }

        CompanyInfo companyInfoDTO = companyInfoService.getCompanyInfoDTO(companyCode);
        if (Objects.nonNull(companyInfoDTO)) {
            String mainDisplayNumber = companyInfoDTO.getMainDisplayNumber();
            if (StrUtil.isNotEmpty(mainDisplayNumber)) {
                return mainDisplayNumber;
            }
        }
        return null;
    }

    @Override
    public void addCallTime(String companyCode, String agentId, Long timestamp, Double callDuration) {
        try {
            String date = DateUtil.format(DateUtil.date(timestamp), CommonConstant.DATE_FORMAT);
            String agentCallTimeKey = CacheUtil.getAgentCallTimeKey(companyCode, date);
            Double score = redissonUtil.addScoreZset(agentCallTimeKey, agentId, callDuration);
            redissonUtil.setTTL(agentCallTimeKey, Duration.ofHours(CacheConstant.TTL_2D));
            log.info("[通话时长累加] 企业: {}, 坐席: {}, 时长: {}, 结果: {}", companyCode, agentId, callDuration, score);
        } catch (Exception e) {
            log.error("[通话时长累加] 企业: {}, 坐席: {}, 时长: {}, 异常: ", companyCode, agentId, callDuration, e);
        }
    }

    @Override
    public void addCallTime(String companyCode, String agentId, String skillId, Long timestamp, Double callDuration) {
        try {
            String date = DateUtil.format(DateUtil.date(timestamp), CommonConstant.DATE_FORMAT);
            String agentSkillCallTimeKey = CacheUtil.getAgentSkillCallTimeKey(companyCode, skillId, date);
            Double score = redissonUtil.addScoreZset(agentSkillCallTimeKey, agentId, callDuration);
            redissonUtil.setTTL(agentSkillCallTimeKey, Duration.ofHours(CacheConstant.TTL_2D));
            log.info("[坐席通话时长累加] 企业: {}, 坐席: {}, 技能: {}, 时长: {}, 结果: {}",
                    companyCode, agentId, skillId, callDuration, score);
        } catch (Exception e) {
            log.error("[坐席通话时长累加] 企业: {}, 坐席: {}, 技能: {}, 时长: {}, 异常: ",
                    companyCode, agentId, skillId, callDuration, e);
        }
    }

    @Override
    public void addCallCount(String companyCode, String agentId, Long timestamp) {
        try {
            String date = DateUtil.format(DateUtil.date(timestamp), CommonConstant.DATE_FORMAT);
            String agentCallCountKey = CacheUtil.getAgentCallCountKey(companyCode, date);
            Double score = redissonUtil.addScoreZset(agentCallCountKey, agentId, 1);
            redissonUtil.setTTL(agentCallCountKey, Duration.ofHours(CacheConstant.TTL_2D));
            log.info("[坐席通话次数累加] 企业: {}, 坐席: {}, 结果: {}", companyCode, agentId, score);
        } catch (Exception e) {
            log.info("[坐席通话次数累加] 企业: {}, 坐席: {}, 异常", companyCode, agentId, e);
        }
    }

    @Override
    public void addCallCount(String companyCode, String agentId, String skillId, Long timestamp) {
        try {
            String date = DateUtil.format(DateUtil.date(timestamp), CommonConstant.DATE_FORMAT);
            String agentSkillCallCountKey = CacheUtil.getAgentSkillCallCountKey(companyCode, skillId, date);
            Double score = redissonUtil.addScoreZset(agentSkillCallCountKey, agentId, 1);
            redissonUtil.setTTL(agentSkillCallCountKey, Duration.ofHours(CacheConstant.TTL_2D));
            log.info("[坐席技能通话次数累加] 企业: {}, 坐席: {},  技能: {}, 结果: {}", companyCode, agentId, skillId, score);
        } catch (Exception e) {
            log.info("[坐席技能通话次数累加] 企业: {}, 坐席: {},  技能: {}, 异常", companyCode, agentId, skillId, e);
        }
    }

    @Override
    public List<String> getCallTime(String companyCode) {
        String date = DateUtil.format(DateUtil.date(), CommonConstant.DATE_FORMAT);
        String agentCallTimeKey = CacheUtil.getAgentCallTimeKey(companyCode, date);
        return redissonUtil.readAllAscZset(agentCallTimeKey);
    }

    @Override
    public List<String> getCallTime(String companyCode, String skillId) {
        String date = DateUtil.format(DateUtil.date(), CommonConstant.DATE_FORMAT);
        String agentSkillCallTimeKey = CacheUtil.getAgentSkillCallTimeKey(companyCode, skillId, date);
        return redissonUtil.readAllAscZset(agentSkillCallTimeKey);
    }

    @Override
    public List<String> getCallCount(String companyCode) {
        String date = DateUtil.format(DateUtil.date(), CommonConstant.DATE_FORMAT);
        String agentCallCountKey = CacheUtil.getAgentCallCountKey(companyCode, date);
        return redissonUtil.readAllAscZset(agentCallCountKey);
    }

    @Override
    public List<String> getCallCount(String companyCode, String skillId) {
        String date = DateUtil.format(DateUtil.date(), CommonConstant.DATE_FORMAT);
        String agentSkillCallCountKey = CacheUtil.getAgentSkillCallCountKey(companyCode, skillId, date);
        return redissonUtil.readAllAscZset(agentSkillCallCountKey);
    }

    @Override
    public AgentWeightInfoDTO getAgentWithWeightInfo(String agentId) {
        String agentWithWeightInfoKey = CacheUtil.getAgentWithWeightInfoKey(agentId);
        AgentWeightInfoDTO agentWeightInfoDTO = null;
        boolean find = true;
        try {
            agentWeightInfoDTO = redissonUtil.get(agentWithWeightInfoKey, AgentWeightInfoDTO.class);
            if (Objects.isNull(agentWeightInfoDTO)) {
                find = false;
            } else {
                if (CollUtil.isEmpty(agentWeightInfoDTO.getSkill()) && CollUtil.isEmpty(agentWeightInfoDTO.getAgent())) {
                    find = false;
                }
            }
        } catch (Exception e) {
            // 为空或异常查db
            find = false;
            log.error("[查询坐席权值信息] 企业: {}, 坐席id: {}, 异常: ", agentId, e);
        }
        if (!find) {
            try {
                agentWeightInfoDTO = getAgentWithWeightInfoByDb(agentId);
            } catch (Exception e) {
                log.error("[查询坐席权值信息-db] 查询失败: ", e);
            }
        }
        return agentWeightInfoDTO;
    }

    @Override
    public AgentWeightInfoDTO getAgentWithWeightInfo(String companyCode, String agentId) {
        AgentCheckinCacheDTO agentCheckinCacheDTO = getAgentCheckinCacheDTO(companyCode, agentId);
        return agentCheckinCacheDTO.getAgentWeightInfoDTO();
    }

    private AgentCheckinCacheDTO getAgentCheckinCacheDTO(String companyCode, String agentId) {
        AgentCheckinCacheDTO agentCheckinCacheDTO = AgentCheckinCache.get(companyCode, agentId);
        if (Objects.nonNull(agentCheckinCacheDTO)) {
            return agentCheckinCacheDTO;
        }
        AgentWeightInfoDTO agentWeightInfoDTO = getAgentWithWeightInfo(agentId);
        AgentInfo agentInfo = getAgentInfo(companyCode, agentId);
        AgentCheckinCacheDTO checkinCacheDTO = AgentCheckinCacheDTO.buildNew(agentInfo, agentWeightInfoDTO);
        AgentCheckinCache.put(companyCode, agentId, checkinCacheDTO);
        return checkinCacheDTO;
    }

    private AgentWeightInfoDTO getAgentWithWeightInfoByDb(String agentId) throws Exception {
        // 查询出当前坐席拥有的技能
        List<AgentSkill> agentSkillList = agentInfoMapper.findAgentSkillWeights(agentId);
        List<AgentSkill> agentSkills = agentInfoMapper.findAgentSkillPackContainSkillWeighs(agentId);
        if (CollUtil.isEmpty(agentSkillList) && CollUtil.isEmpty(agentSkills)) {
            return null;
        }
        agentSkillList.addAll(agentSkills);

        agentSkillList = agentSkillList.stream()
                .filter(e -> StrUtil.isNotBlank(e.getSkillId()))
                .collect(Collectors.toList());

        String agentWithWeightInfoKey = CacheUtil.getAgentWithWeightInfoKey(agentId);
        // 按坐席权值排序, 取出技能
        Map<String, Integer> agentWeightMap = agentSkillList.stream()
                .sorted(Comparator.comparing(AgentSkill::getAgentWeight))
                .collect(Collectors.collectingAndThen(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(AgentSkill::getSkillId))), ArrayList::new))
                .stream()
                .collect(Collectors.toMap(AgentSkill::getSkillId, AgentSkill::getAgentWeight));
        // 按技能权值排序
        Map<String, Integer> skillWeightMap = agentSkillList.stream()
                .sorted(Comparator.comparing(AgentSkill::getSkillWeight))
                .collect(Collectors.collectingAndThen(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(AgentSkill::getSkillId))), ArrayList::new))
                .stream()
                .collect(Collectors.toMap(AgentSkill::getSkillId, AgentSkill::getSkillWeight));
        AgentWeightInfoDTO agentWeightInfoDTO = new AgentWeightInfoDTO();
        agentWeightInfoDTO.setAgent(agentWeightMap);
        agentWeightInfoDTO.setSkill(skillWeightMap);
        String json = objectMapper.writeValueAsString(agentWeightInfoDTO);
        boolean set = redissonUtil.set(agentWithWeightInfoKey, json);
        log.info("[查询坐席权值信息-db] 坐席: {}, 查询db: {}, 回写redis: {}", agentId, json, set);
        return agentWeightInfoDTO;
    }

    @Override
    public Optional<Set<String>> getSkillIdFromAgentWeight(AgentWeightInfoDTO agentWeightInfoDTO) {
        if (Objects.nonNull(agentWeightInfoDTO)) {
            HashSet<String> skillIdSet = new HashSet<>();
            // 查询技能id集合
            Map<String, Integer> agentWeightMap = agentWeightInfoDTO.getAgent();
            if (CollUtil.isNotEmpty(agentWeightMap)) {
                skillIdSet.addAll(agentWeightMap.keySet());
            }
            Map<String, Integer> skillWeightMap = agentWeightInfoDTO.getSkill();
            if (CollUtil.isNotEmpty(skillWeightMap)) {
                skillIdSet.addAll(skillWeightMap.keySet());
            }
            return Optional.of(skillIdSet);
        }
        return Optional.empty();
    }

    @Override
    public List<String> getPredictFreeAgentQueue(String companyCode, String taskId) {
        String companyAgentQueueKey = CacheUtil.getCompanyAgentQueueKey(companyCode, AgentServiceModeEnum.OUTBOUND, true);
        List<String> outCallAgentIdSet = redissonUtil.readAllAscZset(companyAgentQueueKey);
        if (CollUtil.isEmpty(outCallAgentIdSet)) {
            return Lists.newArrayList();
        }
        Set<String> agentIdSet = null;
        try {
            String predictTaskAgentKey = CacheUtil.getPredictTaskAgentKey(taskId);
            agentIdSet = redissonUtil.getSet(predictTaskAgentKey);
        } catch (Exception e) {
            log.error("[查询预测空闲坐席] 企业: {}, taskId:{}, 异常: ", companyCode, taskId, e);
        }
        if (CollUtil.isEmpty(agentIdSet)) {
            agentIdSet = getPredictAgentIdSetFromDb(taskId);
        }
        if (CollUtil.isEmpty(agentIdSet)) {
            return Lists.newArrayList();
        }
        // 查询空闲坐席 - 外呼型
        List<String> match = new ArrayList<>();
        for (String agentId : agentIdSet) {
            if (outCallAgentIdSet.contains(agentId)) {
                match.add(agentId);
            }
        }
        return match;
    }

    private Set<String> getPredictAgentIdSetFromDb(String taskId) {
        LambdaQueryWrapper<PredictOutboundTaskAgent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(PredictOutboundTaskAgent::getAgentId);
        queryWrapper.eq(PredictOutboundTaskAgent::getTaskId, taskId);
        queryWrapper.eq(PredictOutboundTaskAgent::getStartTaskFlag, 1);
        return predictOutboundTaskAgentMapper.selectList(queryWrapper)
                .stream().map(PredictOutboundTaskAgent::getAgentId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<TransferAgentQueueDTO> getCompanyAgentQueue(String companyCode, AgentServiceModeEnum agentMode, boolean free) {
        String companyAgentQueueKey = CacheUtil.getCompanyAgentQueueKey(companyCode, agentMode, free);
        return getAgentQueue(companyCode, companyAgentQueueKey);
    }

    @Override
    public List<TransferAgentQueueDTO> getSkillAgentQueue(String companyCode, String skillId,
                                                          AgentServiceModeEnum agentMode, boolean free) {
        String skillAgentQueueKey = CacheUtil.getSkillAgentQueueKey(companyCode, skillId, agentMode, free);
        return getAgentQueue(companyCode, skillAgentQueueKey);
    }

    private List<TransferAgentQueueDTO> getAgentQueue(String companyCode, String agentQueueKey) {
        // TODO 改成 zset
        List<ScoredEntry<String>> entryList = redissonUtil.getZsetAsc(agentQueueKey);
        if (CollUtil.isEmpty(entryList)) {
            return Lists.newArrayList();
        }
        List<TransferAgentQueueDTO> transferAgentQueueDtoList = new ArrayList<>();
        for (ScoredEntry<String> entry : entryList) {
            String agentId = entry.getValue();
            Double ts = entry.getScore();
            try {
                AgentCheckinCacheDTO agentCheckinCacheDTO = getAgentCheckinCacheDTO(companyCode, agentId);
                TransferAgentQueueDTO transferAgentQueueDTO = TransferAgentQueueDTO.builder()
                        .agentId(agentId)
                        .timestamp(Convert.toLong(ts))
                        .phoneNumber(agentCheckinCacheDTO.getPhoneNumber())
                        .agentWeightInfoDTO(agentCheckinCacheDTO.getAgentWeightInfoDTO())
                        .build();
                transferAgentQueueDtoList.add(transferAgentQueueDTO);
            } catch (Exception e) {
                log.error("[查询坐席队列] key: {}, error: ", agentQueueKey, e);
            }
        }
        return transferAgentQueueDtoList;
    }

    @Deprecated
    private List<TransferAgentQueueDTO> getAgentQueue(String agentQueueKey) {
        Map<String, String> map = redissonUtil.readAllHash(agentQueueKey);
        if (CollUtil.isEmpty(map)) {
            return Lists.newArrayList();
        }
        List<TransferAgentQueueDTO> transferAgentQueueDtoList = new ArrayList<>();
        for (String value : map.values()) {
            if (StrUtil.isEmpty(value)) {
                continue;
            }
            try {
                TransferAgentQueueDTO transferAgentQueueDTO = objectMapper.readValue(value, TransferAgentQueueDTO.class);
                transferAgentQueueDtoList.add(transferAgentQueueDTO);
            } catch (Exception e) {
                log.error("[查询坐席队列] key: {}, data: {}, error: ", agentQueueKey, value, e);
            }
        }
        // 按空闲时间顺序
        return transferAgentQueueDtoList.stream()
                .sorted(Comparator.comparing(TransferAgentQueueDTO::getTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public TransferAgentQueueDTO popFreeQueue(String companyCode,
                                              String skillId,
                                              AgentServiceModeEnum serviceMode,
                                              IdleStrategyEnum idleStrategyEnum) {
        String date = DateUtil.format(DateUtil.date(), CommonConstant.DATE_FORMAT);
        switch (idleStrategyEnum) {
            case RANDOM:
                // TODO random
            case MAX_FREE_TIME:
                String skillAgentQueueKey = CacheUtil.getSkillAgentQueueKey(companyCode, skillId, serviceMode, true);
                return getTransferAgentQueueDTO(companyCode, skillAgentQueueKey);
            case TODAY_LEAST_CALL_TIME:
                String agentSkillCallTimeKey = CacheUtil.getAgentSkillCallTimeKey(companyCode, skillId, date);
                return getTransferAgentQueueDTO(companyCode, agentSkillCallTimeKey);
            case TODAY_LEAST_CALL_COUNT:
                String agentSkillCallCountKey = CacheUtil.getAgentSkillCallCountKey(companyCode, skillId, date);
                return getTransferAgentQueueDTO(companyCode, agentSkillCallCountKey);
            default:
        }
        return null;
    }

    private TransferAgentQueueDTO getTransferAgentQueueDTO(String companyCode, String skillAgentQueueKey) {
        ScoredEntry<String> entry = redissonUtil.popZsetMinScore(skillAgentQueueKey);
        if (Objects.isNull(entry)) {
            return null;
        }
        String agentId = entry.getValue();

        AgentCheckinCacheDTO agentCheckinCacheDTO = getAgentCheckinCacheDTO(companyCode, agentId);
        if (Objects.nonNull(agentCheckinCacheDTO)) {
            return TransferAgentQueueDTO.builder()
                    .agentWeightInfoDTO(agentCheckinCacheDTO.getAgentWeightInfoDTO())
                    .agentId(agentId)
                    .timestamp(Convert.toLong(entry.getScore()))
                    .phoneNumber(agentCheckinCacheDTO.getPhoneNumber())
                    .build();
        }
        AgentWeightInfoDTO agentWithWeightInfo = getAgentWithWeightInfo(companyCode, agentId);
        AgentInfo agentInfo = getAgentInfo(companyCode, agentId);
        String phoneNumber = "";
        if (Objects.nonNull(agentInfo)) {
            phoneNumber = agentInfo.getPhoneNumber();
        }
        return TransferAgentQueueDTO.builder()
                .agentWeightInfoDTO(agentWithWeightInfo)
                .agentId(agentId)
                .timestamp(Convert.toLong(entry.getScore()))
                .phoneNumber(phoneNumber)
                .build();
    }

    @Override
    public void dealAgentCheckinCache(AgentCheckinCacheDTO agentCheckinCacheDTO) {
        String companyCode = agentCheckinCacheDTO.getCompanyCode();
        String agentId = agentCheckinCacheDTO.getAgentId();
        OperateTypeEnum operateTypeEnum = agentCheckinCacheDTO.getOperateTypeEnum();
        if (OperateTypeEnum.DELETE.equals(operateTypeEnum)) {
            AgentCheckinCache.remove(companyCode, agentId);
            log.info("[坐席签出] 本地缓存移除, 企业: {}, 坐席: {}", companyCode, agentId);
            return;
        }
        AgentCheckinCache.put(companyCode, agentId, agentCheckinCacheDTO);
        log.info("[坐席签入] 本地缓存新增, 企业: {}, 坐席: {}", companyCode, agentId);
    }
}
