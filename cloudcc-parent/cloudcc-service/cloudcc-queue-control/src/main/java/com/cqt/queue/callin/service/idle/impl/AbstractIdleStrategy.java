package com.cqt.queue.callin.service.idle.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.extra.spring.SpringUtil;
import com.cqt.base.enums.IdleStrategyEnum;
import com.cqt.base.model.ResultVO;
import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.cqt.model.queue.dto.AgentWeightInfoDTO;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;
import com.cqt.model.queue.vo.MatchAgentWeightVO;
import com.cqt.queue.callin.service.DataStoreService;
import com.cqt.starter.redis.util.RedissonUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-10-11 10:31
 */
@Slf4j
public abstract class AbstractIdleStrategy {

    /**
     * 遍历外呼-混合规则
     *
     * @param userQueueUpDTO 排队参数
     * @param freeAgents     空闲坐席列表
     * @return 结果
     */
    public ResultVO<CallInIvrActionVO> executeMixedTemplate(UserQueueUpDTO userQueueUpDTO,
                                                            List<TransferAgentQueueDTO> freeAgents) {
        String userQueueLockKey = CacheUtil.getUserQueueLockKey(userQueueUpDTO.getSkillId());
        RedissonUtil redissonUtil = SpringUtil.getBean(RedissonUtil.class);
        RLock lock = redissonUtil.getFairLock(userQueueLockKey);
        lock.lock(30, TimeUnit.SECONDS);
        try {
            return mixed(userQueueUpDTO, freeAgents);
        } finally {
            try {
                lock.unlock();
            } catch (Exception e) {
                log.error("[queue] call in executeMixedTemplate skill unlock e: {}", e.getMessage());
            }
        }
    }

    public ResultVO<CallInIvrActionVO> executeMixedTemplate(UserQueueUpDTO userQueueUpDTO,
                                                            List<TransferAgentQueueDTO> freeAgents,
                                                            List<String> callTimeList,
                                                            List<String> callCountList) {
        String companyCode = userQueueUpDTO.getCompanyCode();
        String skillId = userQueueUpDTO.getSkillId();
        // 1. 空闲坐席 获取技能匹配的坐席, 根据坐席权值排序
        List<MatchAgentWeightVO.MatchAgent> matchAgentList = null;
        for (TransferAgentQueueDTO freeAgent : freeAgents) {
            AgentWeightInfoDTO agentWeightInfoDTO = freeAgent.getAgentWeightInfoDTO();
            if (Objects.isNull(agentWeightInfoDTO)) {
                continue;
            }
            Map<String, Integer> agentWeightMap = agentWeightInfoDTO.getAgent();
            if (CollUtil.isEmpty(agentWeightMap)) {
                continue;
            }
            Integer agentWeight = agentWeightMap.get(skillId);
            if (Objects.nonNull(agentWeight)) {
                MatchAgentWeightVO.MatchAgent matchAgent = MatchAgentWeightVO.MatchAgent.builder()
                        .agentId(freeAgent.getAgentId())
                        .phoneNumber(freeAgent.getPhoneNumber())
                        .agentWeight(agentWeight)
                        .freeTimestamp(freeAgent.getTimestamp())
                        .build();
                if (CollUtil.isEmpty(matchAgentList)) {
                    matchAgentList = new ArrayList<>();
                }
                matchAgentList.add(matchAgent);
            }
        }
        if (CollUtil.isEmpty(matchAgentList)) {
            return ResultVO.fail(StrFormatter.format("技能: {}, 没有匹配的坐席", skillId));
        }
        // 是否有权值最高的 size==1 or min
        int matchSize = matchAgentList.size();
        if (matchSize == 1) {
            String agentId = matchAgentList.get(0).getAgentId();
            ResultVO<CallInIvrActionVO> resultVO = callBridge(userQueueUpDTO, agentId);
            if (Objects.nonNull(resultVO) && resultVO.success()) {
                return resultVO;
            }
            log.info("[ivr转技能-{}] 桥接失败, 坐席: {}", getIdleStrategy().name(), agentId);
            return ResultVO.fail(StrFormatter.format("分配坐席: {}, 桥接失败", agentId));
        }

        // 2. 坐席权值一样的空闲坐席, 根据进入空闲排序
        Map<Integer, List<MatchAgentWeightVO.MatchAgent>> groupWeightMap = matchAgentList.stream()
                .collect(Collectors.groupingBy(MatchAgentWeightVO.MatchAgent::getAgentWeight));
        for (Map.Entry<Integer, List<MatchAgentWeightVO.MatchAgent>> entry : groupWeightMap.entrySet()) {
            List<MatchAgentWeightVO.MatchAgent> sameWeightList = entry.getValue();
            if (sameWeightList.size() == 1) {
                String agentId = sameWeightList.get(0).getAgentId();
                ResultVO<CallInIvrActionVO> resultVO = callBridge(userQueueUpDTO, agentId);
                if (Objects.nonNull(resultVO) && resultVO.success()) {
                    removeAgent(freeAgents, agentId);
                    return resultVO;
                }
                log.info("[ivr转技能-{}] 桥接失败, 坐席: {}", getIdleStrategy().name(), agentId);
                continue;
            }
            List<String> matchFreeAgentList = getMatchFreeAgentList(callTimeList, callCountList, sameWeightList);
            if (CollUtil.isEmpty(matchFreeAgentList)) {
                log.info("[ivr转技能-{}] 无匹配坐席.", getIdleStrategy().name());
                return ResultVO.fail(StrFormatter.format("无匹配坐席."));
            }
            // 通话时长/通话次数最少优先, 空闲最长优先
            for (String agentId : matchFreeAgentList) {
                ResultVO<CallInIvrActionVO> resultVO = callBridge(userQueueUpDTO, agentId);
                if (Objects.nonNull(resultVO) && resultVO.success()) {
                    removeAgent(freeAgents, agentId);
                    return resultVO;
                }
                log.info("[ivr转技能-{}] 桥接失败, 坐席: {}", getIdleStrategy().name(), agentId);
            }
        }
        log.info("[ivr转技能-{}] 分配坐席失败.", getIdleStrategy().name());
        return ResultVO.fail(StrFormatter.format("分配坐席失败"));
    }

    private List<String> getMatchFreeAgentList(List<String> callTimeList,
                                               List<String> callCountList,
                                               List<MatchAgentWeightVO.MatchAgent> sameWeightList) {
        List<String> matchFreeAgentList = null;
        if (IdleStrategyEnum.AGENT_WEIGHT_AND_TODAY_LEAST_CALL_COUNT.equals(getIdleStrategy())) {
            List<String> agentIdList = sameWeightList.stream()
                    .map(MatchAgentWeightVO.MatchAgent::getAgentId)
                    .collect(Collectors.toList());
            matchFreeAgentList = getMatchFreeAgentListByCallStats(agentIdList, callCountList);
        }
        if (IdleStrategyEnum.AGENT_WEIGHT_AND_TODAY_LEAST_CALL_TIME.equals(getIdleStrategy())) {
            List<String> agentIdList = sameWeightList.stream()
                    .map(MatchAgentWeightVO.MatchAgent::getAgentId)
                    .collect(Collectors.toList());
            matchFreeAgentList = getMatchFreeAgentListByCallStats(agentIdList, callTimeList);
        }
        if (IdleStrategyEnum.AGENT_WEIGHT_AND_MAX_FREE_TIME.equals(getIdleStrategy())) {
            matchFreeAgentList = sameWeightList.stream()
                    .sorted(Comparator.comparing(MatchAgentWeightVO.MatchAgent::getFreeTimestamp))
                    .map(MatchAgentWeightVO.MatchAgent::getAgentId)
                    .collect(Collectors.toList());
        }
        return matchFreeAgentList;
    }

    public void removeAgent(List<TransferAgentQueueDTO> freeAgents, String agentId) {
        Optional<TransferAgentQueueDTO> first = freeAgents.stream()
                .filter(e -> agentId.equals(e.getAgentId()))
                .findFirst();
        first.ifPresent(freeAgents::remove);
    }

    private ResultVO<CallInIvrActionVO> mixed(UserQueueUpDTO userQueueUpDTO, List<TransferAgentQueueDTO> freeAgents) {
        String companyCode = userQueueUpDTO.getCompanyCode();
        String skillId = userQueueUpDTO.getSkillId();
        // 1. 空闲坐席 获取技能匹配的坐席, 根据坐席权值排序
        List<MatchAgentWeightVO.MatchAgent> matchAgentList = null;
        for (TransferAgentQueueDTO freeAgent : freeAgents) {
            AgentWeightInfoDTO agentWeightInfoDTO = freeAgent.getAgentWeightInfoDTO();
            if (Objects.isNull(agentWeightInfoDTO)) {
                continue;
            }
            Map<String, Integer> agentWeightMap = agentWeightInfoDTO.getAgent();
            if (CollUtil.isEmpty(agentWeightMap)) {
                continue;
            }
            Integer agentWeight = agentWeightMap.get(skillId);
            if (Objects.nonNull(agentWeight)) {
                MatchAgentWeightVO.MatchAgent matchAgent = MatchAgentWeightVO.MatchAgent.builder()
                        .agentId(freeAgent.getAgentId())
                        .phoneNumber(freeAgent.getPhoneNumber())
                        .agentWeight(agentWeight)
                        .freeTimestamp(freeAgent.getTimestamp())
                        .build();
                if (CollUtil.isEmpty(matchAgentList)) {
                    matchAgentList = new ArrayList<>();
                }
                matchAgentList.add(matchAgent);
            }
        }
        if (CollUtil.isEmpty(matchAgentList)) {
            return ResultVO.fail(StrFormatter.format("技能: {}, 没有匹配的坐席", skillId));
        }
        // 是否有权值最高的 size==1 or min
        int matchSize = matchAgentList.size();
        if (matchSize == 1) {
            String agentId = matchAgentList.get(0).getAgentId();
            ResultVO<CallInIvrActionVO> resultVO = callBridge(userQueueUpDTO, agentId);
            if (Objects.nonNull(resultVO) && resultVO.success()) {
                return resultVO;
            }
            log.info("[ivr转技能-{}] 桥接失败, 坐席: {}", getIdleStrategy().name(), agentId);
            return ResultVO.fail(StrFormatter.format("分配坐席: {}, 桥接失败", agentId));
        }

        // 2. 坐席权值一样的空闲坐席, 根据进入空闲排序
        Map<Integer, List<MatchAgentWeightVO.MatchAgent>> groupWeightMap = matchAgentList.stream()
                .collect(Collectors.groupingBy(MatchAgentWeightVO.MatchAgent::getAgentWeight));
        for (Map.Entry<Integer, List<MatchAgentWeightVO.MatchAgent>> entry : groupWeightMap.entrySet()) {
            List<MatchAgentWeightVO.MatchAgent> sameWeightList = entry.getValue();
            if (sameWeightList.size() == 1) {
                String agentId = sameWeightList.get(0).getAgentId();
                ResultVO<CallInIvrActionVO> resultVO = callBridge(userQueueUpDTO, agentId);
                if (Objects.nonNull(resultVO) && resultVO.success()) {
                    return resultVO;
                }
                log.info("[ivr转技能-{}] 桥接失败, 坐席: {}", getIdleStrategy().name(), agentId);
                continue;
            }

            // 通话时长/通话次数最少优先, 空闲最长优先
            List<String> matchFreeAgentList = getMatchFreeAgentList(companyCode, skillId, sameWeightList);
            for (String agentId : matchFreeAgentList) {
                ResultVO<CallInIvrActionVO> resultVO = callBridge(userQueueUpDTO, agentId);
                if (Objects.nonNull(resultVO) && resultVO.success()) {
                    return resultVO;
                }
                log.info("[ivr转技能-{}] 桥接失败, 坐席: {}", getIdleStrategy().name(), agentId);
            }
        }
        log.info("[ivr转技能-{}] 分配坐席失败.", getIdleStrategy().name());
        return ResultVO.fail(StrFormatter.format("分配坐席失败"));
    }

    /**
     * 遍历外呼
     *
     * @param userQueueUpDTO     排队参数
     * @param matchFreeAgentList 匹配坐席id列表
     * @return 结果
     */
    public ResultVO<CallInIvrActionVO> executeTemplate(UserQueueUpDTO userQueueUpDTO, List<String> matchFreeAgentList) {
        if (CollUtil.isEmpty(matchFreeAgentList)) {
            log.info("[ivr转技能-{}] 无匹配坐席, 进入排队队列", getIdleStrategy().name());
            return ResultVO.fail(StrFormatter.format("{}-无匹配坐席, 进入排队队列!", getIdleStrategy().name()));
        }
        for (String agentId : matchFreeAgentList) {
            log.info("[ivr转技能-{}] 分配坐席: {}", getIdleStrategy().name(), agentId);
            ResultVO<CallInIvrActionVO> resultVO = callBridge(userQueueUpDTO, agentId);
            if (Objects.nonNull(resultVO) && resultVO.success()) {
                return resultVO;
            }
        }
        return ResultVO.fail(StrFormatter.format("{}-分配坐席失败, 进入排队队列!", getIdleStrategy().name()));
    }

    /**
     * 桥接
     *
     * @param userQueueUpDTO 排队参数
     * @param agentId        坐席id
     */
    public ResultVO<CallInIvrActionVO> callBridge(UserQueueUpDTO userQueueUpDTO, String agentId) {
        String companyCode = userQueueUpDTO.getCompanyCode();
        try {
            String skillId = userQueueUpDTO.getSkillId();
            log.info("[ivr转技能-{}] 企业: {}, 分配坐席: {}", getIdleStrategy().name(), companyCode, agentId);
            // 外呼并桥接坐席
            FreeswitchApiVO freeswitchApiVO = getDataStoreService().callBridgeAgent(userQueueUpDTO, agentId, "");
            if (Objects.nonNull(freeswitchApiVO) && freeswitchApiVO.getResult()) {
                log.info("[ivr转技能-{}] 分配坐席成功, 外呼并桥接成功. 企业id: {}, 技能id: {}, 坐席id: {}, 移除空闲队列",
                        getIdleStrategy().name(), companyCode, skillId, agentId);
                return ResultVO.ok(CallInIvrActionVO.builder().agentId(agentId).build(), "存在匹配坐席, 已发起外呼!");
            }
        } catch (Exception e) {
            log.error("[callBridge] userQueueUpDTO: {}, agentId: {], error: {}", userQueueUpDTO, agentId, e);
        }
        log.info("[ivr转技能-{}] 企业: {}, 分配坐席: {}, 外呼并桥接失败!", getIdleStrategy().name(), companyCode, agentId);
        return ResultVO.fail();
    }

    /**
     * 获取匹配的空闲坐席列表
     *
     * @param freeAgentIdList 查询到的空闲坐席id列表
     * @param callStatsList   坐席呼叫统计排序列表
     * @return 匹配的空闲坐席列表
     */
    public List<String> getMatchFreeAgentListByCallStats(List<String> freeAgentIdList, List<String> callStatsList) {
        if (CollUtil.isEmpty(callStatsList)) {
            return freeAgentIdList;
        }
        int freeAgentSize = freeAgentIdList.size();
        if (freeAgentSize == 1) {
            return Lists.newArrayList(freeAgentIdList.get(0));
        }

        if (freeAgentSize == callStatsList.size()) {
            return callStatsList;
        }

        List<String> matchList = new ArrayList<>();
        for (String callCountAgentId : callStatsList) {
            if (freeAgentIdList.contains(callCountAgentId)) {
                matchList.add(callCountAgentId);
            }
        }
        freeAgentIdList.removeAll(matchList);
        freeAgentIdList.addAll(matchList);
        return freeAgentIdList;
    }

    public abstract IdleStrategyEnum getIdleStrategy();

    public abstract DataStoreService getDataStoreService();

    public abstract CommonDataOperateService getCommonDataOperateService();

    public abstract List<String> getMatchFreeAgentList(String companyCode,
                                                       String skillId,
                                                       List<MatchAgentWeightVO.MatchAgent> sameWeightList);
}
