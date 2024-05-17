package com.cqt.queue.callin.service.idle.impl;

import com.cqt.base.enums.IdleStrategyEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.base.model.ResultVO;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;
import com.cqt.model.queue.vo.MatchAgentWeightVO;
import com.cqt.queue.callin.service.DataStoreService;
import com.cqt.queue.callin.service.idle.IdleStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-10-10 10:03
 * 坐席权值+当前空闲持续时长最长的优先
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeightAndMaxFreeTimeIdleStrategyImpl extends AbstractIdleStrategy implements IdleStrategy {

    private final DataStoreService dataStoreService;

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public IdleStrategyEnum getIdleStrategy() {
        return IdleStrategyEnum.AGENT_WEIGHT_AND_MAX_FREE_TIME;
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
        List<TransferAgentQueueDTO> freeAgents = commonDataOperateService.getSkillAgentQueue(companyCode, skillId,
                AgentServiceModeEnum.CUSTOMER, true);
        return executeMixedTemplate(userQueueUpDTO, freeAgents);
    }

    @Override
    public ResultVO<CallInIvrActionVO> execute(UserQueueUpDTO userQueueUpDTO, List<TransferAgentQueueDTO> freeAgents)
            throws Exception {

        return executeMixedTemplate(userQueueUpDTO, freeAgents);
    }

    @Override
    public ResultVO<CallInIvrActionVO> executeIdle(UserQueueUpDTO userQueueUpDTO,
                                                   List<TransferAgentQueueDTO> freeAgents,
                                                   List<String> callTimeList,
                                                   List<String> callCountList) {
        return executeMixedTemplate(userQueueUpDTO, freeAgents, callTimeList, callCountList);
    }

    @Override
    public List<String> getMatchFreeAgentList(String companyCode,
                                              String skillId,
                                              List<MatchAgentWeightVO.MatchAgent> sameWeightList) {
        return sameWeightList.stream()
                .sorted(Comparator.comparing(MatchAgentWeightVO.MatchAgent::getFreeTimestamp))
                .map(MatchAgentWeightVO.MatchAgent::getAgentId)
                .collect(Collectors.toList());
    }

}
