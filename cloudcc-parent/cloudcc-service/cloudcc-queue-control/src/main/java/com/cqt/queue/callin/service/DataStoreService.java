package com.cqt.queue.callin.service;

import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.model.queue.vo.MatchAgentWeightVO;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-07-12 14:59
 */
public interface DataStoreService {

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

    void callIvrExit(UserQueueUpDTO userQueueUpDTO);

    /**
     * 无空闲坐席, 进入排队队列
     */
    void noneFreeAgentAndEnterQueue(UserQueueUpDTO userQueueUpDTO);

    /**
     * 从企业离线坐席队列删除坐席
     * 提供话务控制调用
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     */
    void deleteOfflineAgentQueue(String companyCode, String agentId);

    /**
     * rpc -> queue-control
     * 从企业空闲坐席队列删除坐席
     * 提供话务控制调用
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     */
    Boolean deleteFreeAgentQueue(String companyCode, String agentId);

    /**
     * rpc -> queue-control
     * 从企业空闲坐席队列新增坐席
     * 提供话务控制调用
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @param timestamp   空闲的时间戳
     */
    Boolean addFreeAgentQueue(String companyCode, String agentId, Long timestamp);

    /**
     * 坐席权值和技能权值以签入时的配置为准, 不管签入之后是否有更改, 只能在下次再次签入生效
     * <p>
     * 2.1 传入的技能id与空闲坐席列表比较, 返回拥有该技能id的空闲坐席列表
     * <p>
     * 2.2 查询空闲坐席, 对应技能id的坐席权值哪个最小
     * <p>
     * 2.3 找到技能的坐席权值最高的坐席
     * <p>
     * 2.4 查询这些坐席的空闲时间时长, 找空闲最长的坐席
     *
     * @param freeAgents  技能id对应的空闲坐席列表
     * @param companyCode 企业id
     * @param skillId     技能id
     */
    MatchAgentWeightVO matchAgentWeightBySkillId(List<TransferAgentQueueDTO> freeAgents,
                                                 String companyCode,
                                                 String skillId,
                                                 Boolean free);

    /**
     * 找到匹配坐席后, 发起外呼并桥接
     *
     * @param userQueueUpDTO 用户排队信息
     * @param matchAgentId   坐席id
     * @throws Exception json异常
     */
    FreeswitchApiVO callBridgeAgent(UserQueueUpDTO userQueueUpDTO, String matchAgentId, String phoneNumber) throws Exception;

    Boolean unlockOriginate(String companyCode, String number);

    Boolean lockOriginate(String companyCode, String number, String uuid);

}
