package com.cqt.queue.callin.service.idle;

import com.cqt.base.enums.IdleStrategyEnum;
import com.cqt.base.model.ResultVO;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-10-10 9:45
 * 闲时策略接口
 */
public interface IdleStrategy {

    /**
     * 获取闲时策略
     *
     * @return 闲时策略
     */
    IdleStrategyEnum getIdleStrategy();

    /**
     * 执行策略
     *
     * @param userQueueUpDTO 排队参数
     * @return 执行结果
     */
    ResultVO<CallInIvrActionVO> execute(UserQueueUpDTO userQueueUpDTO) throws Exception;

    /**
     * 执行策略
     *
     * @param userQueueUpDTO 排队参数
     * @return 执行结果
     */
    ResultVO<CallInIvrActionVO> execute(UserQueueUpDTO userQueueUpDTO, List<TransferAgentQueueDTO> freeAgents) throws Exception;

    ResultVO<CallInIvrActionVO> executeIdle(UserQueueUpDTO userQueueUpDTO,
                                            List<TransferAgentQueueDTO> freeAgents,
                                            List<String> callTimeList,
                                            List<String> callCountList);

}
