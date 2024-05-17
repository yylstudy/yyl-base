package com.cqt.cloudcc.manager.service;

import com.cqt.base.enums.IdleStrategyEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.queue.dto.AgentCheckinCacheDTO;
import com.cqt.model.queue.dto.AgentWeightInfoDTO;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-08-22 9:37
 */
public interface AgentInfoService {

    /**
     * 查询坐席基本信息-配置
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 分机坐席信息
     */
    AgentInfo getAgentInfo(String companyCode, String agentId);

    /**
     * 查询坐席基本信息-配置
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 分机坐席信息
     */
    AgentInfo getAgentInfo(String companyCode, String agentId, boolean context);

    /**
     * 查询坐席实时状态
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 状态
     */
    Optional<AgentStatusDTO> getAgentStatusDTO(String companyCode, String agentId);

    /**
     * 保存坐席实时状态
     *
     * @param agentStatusDTO 坐席实时状态
     */
    void updateActualAgentStatus(AgentStatusDTO agentStatusDTO);

    /**
     * 坐席与分机绑定关系
     *
     * @param companyCode 企业id
     * @param extId       分机id
     * @return 坐席id
     */
    String getAgentIdRelateExtId(String companyCode, String extId);

    /**
     * 分机与坐席绑定关系
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 分机id
     */
    String getExtIdRelateAgentId(String companyCode, String agentId);

    /**
     * 获取坐席主显号码
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 主显号
     */
    String getAgentDisplayNumber(String companyCode, String agentId);

    /**
     * 坐席通话时间累加
     *
     * @param companyCode  企业id
     * @param agentId      坐席id
     * @param timestamp    挂断时间戳
     * @param callDuration 通话时长
     */
    void addCallTime(String companyCode, String agentId, Long timestamp, Double callDuration);

    /**
     * 坐席通话时间累加
     *
     * @param companyCode  企业id
     * @param agentId      坐席id
     * @param skillId      技能id
     * @param timestamp    挂断时间戳
     * @param callDuration 通话时长
     */
    void addCallTime(String companyCode, String agentId, String skillId, Long timestamp, Double callDuration);

    /**
     * 坐席通话次数累加
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @param timestamp   接通时间戳
     */
    void addCallCount(String companyCode, String agentId, Long timestamp);

    /**
     * 坐席通话次数累加
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @param skillId     技能id
     * @param timestamp   接通时间戳
     */
    void addCallCount(String companyCode, String agentId, String skillId, Long timestamp);

    /**
     * 坐席通话时间统计列表
     *
     * @param companyCode 企业id
     * @return 坐席通话次数列表
     */
    List<String> getCallTime(String companyCode);

    /**
     * 坐席通话时间统计列表
     *
     * @param companyCode 企业id
     * @param skillId     技能id
     * @return 坐席通话次数列表
     */
    List<String> getCallTime(String companyCode, String skillId);

    /**
     * 坐席通话次数统计列表
     *
     * @param companyCode 企业id
     * @return 坐席通话次数列表
     */
    List<String> getCallCount(String companyCode);

    /**
     * 坐席通话次数统计列表
     *
     * @param companyCode 企业id
     * @param skillId     技能id
     * @return 坐席通话次数列表
     */
    List<String> getCallCount(String companyCode, String skillId);

    /**
     * 查询坐席的 权值配置
     *
     * @param agentId 坐席id
     * @return 权值配置
     */
    AgentWeightInfoDTO getAgentWithWeightInfo(String agentId);

    /**
     * 查询坐席的 权值配置
     *
     * @param agentId 坐席id
     * @return 权值配置
     */
    AgentWeightInfoDTO getAgentWithWeightInfo(String companyCode, String agentId);

    /**
     * 获取坐席配置的技能
     *
     * @param agentWeightInfoDTO 权值配置
     * @return 坐席配置的技能
     */
    Optional<Set<String>> getSkillIdFromAgentWeight(AgentWeightInfoDTO agentWeightInfoDTO);

    /**
     * 预测外呼-查询空闲坐席-根据服务模式
     *
     * @param companyCode 企业id
     * @param taskId      任务id
     * @return 空闲坐席
     */
    List<String> getPredictFreeAgentQueue(String companyCode, String taskId);

    /**
     * 查询企业坐席队列 - 空闲, 离线
     *
     * @param companyCode 企业id
     * @param agentMode   坐席服务模式
     * @param free        是否空闲
     * @return 坐席队列
     */
    List<TransferAgentQueueDTO> getCompanyAgentQueue(String companyCode,
                                                     AgentServiceModeEnum agentMode,
                                                     boolean free);

    /**
     * 查询技能坐席队列 - 空闲, 离线
     *
     * @param companyCode 企业id
     * @param skillId     技能id
     * @param agentMode   坐席服务模式
     * @param free        是否空闲
     * @return 坐席队列
     */
    List<TransferAgentQueueDTO> getSkillAgentQueue(String companyCode,
                                                   String skillId,
                                                   AgentServiceModeEnum agentMode,
                                                   boolean free);

    TransferAgentQueueDTO popFreeQueue(String companyCode,
                                       String skillId,
                                       AgentServiceModeEnum agentServiceModeEnum,
                                       IdleStrategyEnum idleStrategyEnum);

    void dealAgentCheckinCache(AgentCheckinCacheDTO agentCheckinCacheDTO);

}
