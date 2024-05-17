package com.cqt.rpc.queue;

import com.cqt.base.model.ResultVO;
import com.cqt.model.queue.dto.CallInIvrActionDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;

/**
 * @author linshiqiang
 * date:  2023-07-13 15:16
 * 排队控制rqpc接口
 */
public interface QueueControlRemoteService {

    /**
     * 呼入转技能, 分配坐席
     *
     * @param callInIvrActionDTO 分配坐席参数
     * @return 结果
     * @throws Exception 异常
     */
    ResultVO<CallInIvrActionVO> distributeAgent(CallInIvrActionDTO callInIvrActionDTO) throws Exception;

    /**
     * 将用户加到排队队列
     *
     * @param userQueueUpDTO 排队信息
     * @return 是否成功
     */
    Boolean addUserLevelQueue(UserQueueUpDTO userQueueUpDTO);

    /**
     * 移除排队人员
     *
     * @param userQueueUpDTO 来电排队信息
     * @return true/false
     */
    boolean removeQueueUp(UserQueueUpDTO userQueueUpDTO);
}
