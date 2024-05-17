package com.cqt.queue.callin.service.idle;

import com.cqt.base.model.ResultVO;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-10-10 10:24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdleStrategyFactory implements CommandLineRunner {

    private static final Map<Integer, IdleStrategy> STRATEGY_MAP = new HashMap<>(8);

    private final List<IdleStrategy> idleStrategyList;

    @Override
    public void run(String... args) {
        for (IdleStrategy idleStrategy : idleStrategyList) {
            STRATEGY_MAP.put(idleStrategy.getIdleStrategy().getCode(), idleStrategy);
        }
        log.info("初始化排队闲时策略: {}", STRATEGY_MAP.size());
    }

    /**
     * 执行具体闲时策略-分配坐席
     *
     * @param userQueueUpDTO 排队参数
     */
    public ResultVO<CallInIvrActionVO> execute(UserQueueUpDTO userQueueUpDTO) throws Exception {

        IdleStrategy idleStrategy = STRATEGY_MAP.get(userQueueUpDTO.getIdleStrategy());
        if (Objects.isNull(idleStrategy)) {
            return ResultVO.fail("闲时策略类型不支持");
        }
        return idleStrategy.execute(userQueueUpDTO);
    }

    /**
     * 执行具体闲时策略-分配坐席
     *
     * @param userQueueUpDTO 排队参数
     */
    public ResultVO<CallInIvrActionVO> execute(UserQueueUpDTO userQueueUpDTO, List<TransferAgentQueueDTO> freeAgents)
            throws Exception {

        IdleStrategy idleStrategy = STRATEGY_MAP.get(userQueueUpDTO.getIdleStrategy());
        if (Objects.isNull(idleStrategy)) {
            return ResultVO.fail("闲时策略类型不支持");
        }
        return idleStrategy.execute(userQueueUpDTO, freeAgents);
    }

    public ResultVO<CallInIvrActionVO> executeIdle(UserQueueUpDTO userQueueUpDTO,
                                                   List<TransferAgentQueueDTO> freeAgents,
                                                   List<String> callTimeList,
                                                   List<String> callCountList) throws Exception {

        IdleStrategy idleStrategy = STRATEGY_MAP.get(userQueueUpDTO.getIdleStrategy());
        if (Objects.isNull(idleStrategy)) {
            return ResultVO.fail("闲时策略类型不支持");
        }
        return idleStrategy.executeIdle(userQueueUpDTO, freeAgents, callTimeList, callCountList);
    }

}
