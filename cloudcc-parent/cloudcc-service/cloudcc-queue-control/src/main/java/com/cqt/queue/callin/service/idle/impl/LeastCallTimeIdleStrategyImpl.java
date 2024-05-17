package com.cqt.queue.callin.service.idle.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrFormatter;
import com.cqt.base.enums.IdleStrategyEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.base.model.ResultVO;
import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;
import com.cqt.model.queue.vo.MatchAgentWeightVO;
import com.cqt.queue.callin.service.DataStoreService;
import com.cqt.queue.callin.service.idle.IdleStrategy;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-10-10 10:03
 * 通话时间最少优先
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeastCallTimeIdleStrategyImpl extends AbstractIdleStrategy implements IdleStrategy {

    private final DataStoreService dataStoreService;

    private final CommonDataOperateService commonDataOperateService;

    private final RedissonUtil redissonUtil;

    @Override
    public IdleStrategyEnum getIdleStrategy() {
        return IdleStrategyEnum.TODAY_LEAST_CALL_TIME;
    }
    
    @Override
    public DataStoreService getDataStoreService() {
        return dataStoreService;
    }

    @Override
    public CommonDataOperateService getCommonDataOperateService() {
        return commonDataOperateService;
    }

    @Override
    public ResultVO<CallInIvrActionVO> execute(UserQueueUpDTO userQueueUpDTO) throws Exception {
        String companyCode = userQueueUpDTO.getCompanyCode();
        String skillId = userQueueUpDTO.getSkillId();
        String userQueueLockKey = CacheUtil.getUserQueueLockKey(userQueueUpDTO.getSkillId());
        RLock lock = redissonUtil.getFairLock(userQueueLockKey);
        lock.lock(30, TimeUnit.SECONDS);
        try {
            List<TransferAgentQueueDTO> freeAgents = commonDataOperateService.getSkillAgentQueue(companyCode, skillId,
                    AgentServiceModeEnum.CUSTOMER, true);
            return execute(userQueueUpDTO, freeAgents);
        } finally {
            try {
                lock.unlock();
            } catch (Exception e) {
                log.error("[queue] call in LeastCallTime skill unlock e: {}", e.getMessage());
            }
        }
    }

    @Override
    public ResultVO<CallInIvrActionVO> execute(UserQueueUpDTO userQueueUpDTO, List<TransferAgentQueueDTO> freeAgents)
            throws Exception {
        String companyCode = userQueueUpDTO.getCompanyCode();
        String skillId = userQueueUpDTO.getSkillId();
        // 坐席id列表 按通话次数从小到大
        List<String> callTimeList = commonDataOperateService.getCallTime(companyCode, skillId);
        List<String> agentIdList = freeAgents.stream().map(TransferAgentQueueDTO::getAgentId).collect(Collectors.toList());
        List<String> matchFreeAgentList = getMatchFreeAgentListByCallStats(agentIdList, callTimeList);
        return executeTemplate(userQueueUpDTO, matchFreeAgentList);
    }

    @Override
    public ResultVO<CallInIvrActionVO> executeIdle(UserQueueUpDTO userQueueUpDTO,
                                                   List<TransferAgentQueueDTO> freeAgents,
                                                   List<String> callTimeList,
                                                   List<String> callCountList) {
        List<String> agentIdList = freeAgents.stream()
                .map(TransferAgentQueueDTO::getAgentId)
                .collect(Collectors.toList());
        List<String> matchFreeAgentList = getMatchFreeAgentListByCallStats(agentIdList, callTimeList);
        // return executeTemplate(userQueueUpDTO, matchFreeAgentList);
        if (CollUtil.isEmpty(matchFreeAgentList)) {
            log.info("[ivr转技能-{}] 无匹配坐席, 进入排队队列", getIdleStrategy().name());
            return ResultVO.fail(StrFormatter.format("{}-无匹配坐席, 进入排队队列!", getIdleStrategy().name()));
        }
        for (String agentId : matchFreeAgentList) {
            log.info("[ivr转技能-{}] 分配坐席: {}", getIdleStrategy().name(), agentId);
            ResultVO<CallInIvrActionVO> resultVO = callBridge(userQueueUpDTO, agentId);
            if (Objects.nonNull(resultVO) && resultVO.success()) {
                // remove
                removeAgent(freeAgents, agentId);
                return resultVO;
            }
        }
        return ResultVO.fail(StrFormatter.format("{}-分配坐席失败, 进入排队队列!", getIdleStrategy().name()));
    }

    @Override
    public List<String> getMatchFreeAgentList(String companyCode,
                                              String skillId,
                                              List<MatchAgentWeightVO.MatchAgent> sameWeightList) {
        // nothing
        List<String> callTimeList = commonDataOperateService.getCallTime(companyCode, skillId);
        List<String> agentIdList = sameWeightList.stream()
                .map(MatchAgentWeightVO.MatchAgent::getAgentId)
                .collect(Collectors.toList());
        return getMatchFreeAgentListByCallStats(agentIdList, callTimeList);
    }
}
