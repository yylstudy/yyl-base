package com.cqt.queue.callin.service.idle.impl;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.RandomUtil;
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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-10-10 10:03
 * 随机分配
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RandomIdleStrategyImpl extends AbstractIdleStrategy implements IdleStrategy {

    private final DataStoreService dataStoreService;

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public IdleStrategyEnum getIdleStrategy() {
        return IdleStrategyEnum.RANDOM;
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
        String companyCode = userQueueUpDTO.getCompanyCode();
        int randomInt = RandomUtil.randomInt(freeAgents.size());
        TransferAgentQueueDTO transferAgentQueueDTO = freeAgents.get(randomInt);
        String agentId = transferAgentQueueDTO.getAgentId();
        log.info("[ivr转技能-Random] 企业: {}, 分配坐席: {}", companyCode, agentId);
        // callInIvrActionDTO.getCallBridge() 有用?
        // TODO 外呼lock setnx
        // 外呼并桥接坐席
        ResultVO<CallInIvrActionVO> resultVO = callBridge(userQueueUpDTO, agentId);
        if (Objects.nonNull(resultVO)) {
            return resultVO;
        }

        freeAgents.remove(transferAgentQueueDTO);

        List<String> matchFreeAgentList = freeAgents.stream()
                .map(TransferAgentQueueDTO::getAgentId)
                .collect(Collectors.toList());
        return executeTemplate(userQueueUpDTO, matchFreeAgentList);
    }

    @Override
    public ResultVO<CallInIvrActionVO> executeIdle(UserQueueUpDTO userQueueUpDTO,
                                                   List<TransferAgentQueueDTO> freeAgents,
                                                   List<String> callTimeList,
                                                   List<String> callCountList) {
        // TODO random.
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
