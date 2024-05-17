package com.cqt.queue.callin.service.idle.impl;

import cn.hutool.core.text.StrFormatter;
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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-10-10 10:03
 * 当前空闲持续时长最长的优先
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaxFreeTimeIdleStrategyImpl extends AbstractIdleStrategy implements IdleStrategy {

    private final DataStoreService dataStoreService;

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public IdleStrategyEnum getIdleStrategy() {
        return IdleStrategyEnum.MAX_FREE_TIME;
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
        while (true) {
            TransferAgentQueueDTO agentQueueDTO = commonDataOperateService.popFreeAgentQueue(companyCode, skillId,
                    AgentServiceModeEnum.CUSTOMER, getIdleStrategy());
            if (Objects.isNull(agentQueueDTO)) {
                return ResultVO.fail(StrFormatter.format("{}-无空闲坐席, 进入排队队列!", getIdleStrategy().name()));
            }
            String agentId = agentQueueDTO.getAgentId();
            ResultVO<CallInIvrActionVO> resultVO = callBridge(userQueueUpDTO, agentId);
            if (resultVO.success()) {
                return ResultVO.fail(StrFormatter.format("桥接坐席: {}, 成功!", getIdleStrategy().name()));
            }
        }
    }

    @Override
    public ResultVO<CallInIvrActionVO> execute(UserQueueUpDTO userQueueUpDTO, List<TransferAgentQueueDTO> freeAgents)
            throws Exception {
        // 根据进入空闲时间排序 从小到大
        List<String> matchFreeAgentList = freeAgents.stream()
                .sorted(Comparator.comparing(TransferAgentQueueDTO::getTimestamp))
                .map(TransferAgentQueueDTO::getAgentId)
                .collect(Collectors.toList());
        return executeTemplate(userQueueUpDTO, matchFreeAgentList);
    }

    @Override
    public ResultVO<CallInIvrActionVO> executeIdle(UserQueueUpDTO userQueueUpDTO,
                                                   List<TransferAgentQueueDTO> freeAgents,
                                                   List<String> callTimeList,
                                                   List<String> callCountList) {
        freeAgents = freeAgents.stream()
                .sorted(Comparator.comparing(TransferAgentQueueDTO::getTimestamp))
                .collect(Collectors.toList());
        Iterator<TransferAgentQueueDTO> iterator = freeAgents.iterator();
        while (iterator.hasNext()) {
            TransferAgentQueueDTO transferAgentQueueDTO = iterator.next();
            String agentId = transferAgentQueueDTO.getAgentId();
            ResultVO<CallInIvrActionVO> resultVO = callBridge(userQueueUpDTO, agentId);
            if (resultVO.success()) {
                iterator.remove();
                return ResultVO.fail(StrFormatter.format("桥接坐席: {}, 成功!", getIdleStrategy().name()));
            }
        }
        return ResultVO.fail(StrFormatter.format("{}-无空闲坐席, 进入排队队列!", getIdleStrategy().name()));
    }

    @Override
    public List<String> getMatchFreeAgentList(String companyCode,
                                              String skillId,
                                              List<MatchAgentWeightVO.MatchAgent> sameWeightList) {
        // nothing
        return null;
    }
}
