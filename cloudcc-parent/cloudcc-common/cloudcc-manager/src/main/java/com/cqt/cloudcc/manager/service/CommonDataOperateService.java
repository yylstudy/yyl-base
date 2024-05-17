package com.cqt.cloudcc.manager.service;

import com.cqt.base.enums.CallDirectionEnum;
import com.cqt.base.enums.DefaultToneEnum;
import com.cqt.base.enums.IdleStrategyEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.base.enums.calltask.CallTaskEnum;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.ext.entity.ExtInfo;
import com.cqt.model.number.entity.NumberInfo;
import com.cqt.model.queue.dto.AgentCheckinCacheDTO;
import com.cqt.model.queue.dto.AgentWeightInfoDTO;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.model.queue.entity.IvrServiceInfo;
import com.cqt.model.skill.entity.SkillInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-08-22 9:33
 * 通用查询服务接口
 */
public interface CommonDataOperateService {

    /**
     * 查询平台默认音配置
     */
    String getDefaultTone(DefaultToneEnum defaultTone);

    /**
     * 查询企业信息
     *
     * @param companyCode 企业id
     * @return 企业信息
     */
    CompanyInfo getCompanyInfoDTO(String companyCode);

    /**
     * 获取全部启用的企业id集合
     *
     * @return 企业id集合
     */
    Set<String> getEnableCompanyCode();

    /**
     * 获取全部的企业id集合
     *
     * @return 企业id集合
     */
    Set<String> getAllCompanyCode();

    /**
     * 查询号码信息
     *
     * @param number 号码
     * @return 号码信息
     */
    Optional<NumberInfo> getNumberInfo(String number);

    /**
     * 检测号码是否在黑名单内
     *
     * @param companyCode   企业id
     * @param number        号码
     * @param callDirection 呼叫方向
     * @return 是否在黑名单内
     */
    Boolean checkBlackNumber(String companyCode, String number, CallDirectionEnum callDirection);

    /**
     * 查询技能配置信息
     *
     * @param skillId 技能id
     * @return 技能配置信息
     */
    SkillInfo getSkillInfo(String skillId);

    /**
     * 根据文件id查询文件目录
     *
     * @param companyCode 企业id
     * @param fileId      文件id
     * @return 文件目录
     */
    String getFilePath(String companyCode, String fileId);

    /**
     * 查询来电号码优先级
     *
     * @param companyCode  企业号码
     * @param callerNumber 来电号码
     * @return 用户等级
     */
    Integer getClientPriority(String companyCode, String callerNumber);

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
     * 从redis查询坐席实时状态
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 坐席实时状态
     */
    Optional<AgentStatusDTO> getActualAgentStatus(String companyCode, String agentId);

    /**
     * 保存坐席实时状态
     *
     * @param agentStatusDTO 坐席实时状态
     */
    void updateActualAgentStatus(AgentStatusDTO agentStatusDTO);

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
     * 获取坐席主显号码
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 主显号
     * @throws Exception 异常
     */
    String getAgentDisplayNumber(String companyCode, String agentId) throws Exception;

    /**
     * 分机与坐席绑定关系
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 分机id
     */
    String getExtIdRelateAgentId(String companyCode, String agentId);

    /**
     * 坐席与分机绑定关系
     *
     * @param companyCode 企业id
     * @param extId       分机id
     * @return 坐席id
     */
    String getAgentIdRelateExtId(String companyCode, String extId);

    /**
     * 查询分机基本信息-配置
     *
     * @param companyCode 企业id
     * @param extId       分机id
     * @return 分机基本信息
     */
    ExtInfo getExtInfo(String companyCode, String extId) throws Exception;

    /**
     * 从redis查询分机实时状态
     *
     * @param companyCode 企业id
     * @param extId       分机id
     * @return 分机实时状态
     */
    ExtStatusDTO getActualExtStatus(String companyCode, String extId);

    /**
     * 保存分机实时状态
     *
     * @param extStatusDTO 分机实时状态
     */
    void updateActualExtStatus(ExtStatusDTO extStatusDTO);

    /**
     * 获取通话uuid上下文
     *
     * @param companyCode 企业id
     * @param uuid        通话id
     * @return 上下文
     */
    CallUuidContext getCallUuidContext(String companyCode, String uuid);

    /**
     * 保存通话uuid之间关联关系
     *
     * @param callUuidContext uuid上下文
     */
    void saveCallUuidContext(CallUuidContext callUuidContext);

    /**
     * 保存话单关系
     *
     * @param companyCode 企业id
     * @param mainCallId  主话单id
     * @param sourceUUID  主叫uuid
     * @param destUUID    被叫uuid
     */
    void saveCdrLink(String companyCode, String mainCallId, String sourceUUID, String destUUID);

    /**
     * uuid是否已挂断
     *
     * @param companyCode 企业id
     * @param uuid        uuid
     * @return 是否已挂断
     */
    Boolean isHangup(String companyCode, String uuid);

    /**
     * 话单是否已经生成
     *
     * @param companyCode 企业id
     * @param mainCallId  通话id
     * @return 是否已经生成
     */
    Boolean isCdrGenerated(String companyCode, String mainCallId);

    /**
     * 保存话单平台号码
     *
     * @param companyCode  企业id
     * @param mainCallId   通话id
     * @param platformNumber 平台号码
     */
    void saveCdrPlatformNumber(String companyCode, String mainCallId, String platformNumber);

    /**
     * 查询话单平台号码
     *
     * @param companyCode 企业id
     * @param mainCallId  通话id
     * @return platformNumber 计费号码
     */
    String getPlatformNumber(String companyCode, String mainCallId);

    /**
     * 生成主通话id
     *
     * @return 主通话id
     */
    String createMainCallId();

    /**
     * 保存咨询标志
     *
     * @param uuid 被咨询方通话id
     * @return 是否保存成功
     */
    Boolean saveConsultFlag(String uuid);

    /**
     * 查询ivr服务配置信息
     *
     * @param serviceId ivr服务id
     * @return ivr服务配置信息
     */
    IvrServiceInfo getIvrServiceInfo(String serviceId);

    /**
     * 号码达到呼叫次数上限 移除zset, 接通移除zset
     *
     * @param taskId       任务id
     * @param member       成员
     * @param callTaskEnum 任务类型
     */
    void removeNumber(String taskId, String member, CallTaskEnum callTaskEnum);

    /**
     * 保存录制文件路径到缓存
     */
    void saveRecordFile(String companyCode, String uuid, String filePath);

    /**
     * 获取录制文件路径从缓存
     */
    String getRecordFile(String companyCode, String uuid);

    /**
     * 生成token
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return token
     */
    String createToken(String companyCode, String agentId, String os);

    /**
     * 保存token
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @param token       token
     */
    void saveToken(String companyCode, String agentId, String os, String token);

    /**
     * 校验token
     *
     * @param sdkToken token
     * @return 是否校验通过
     */
    boolean checkToken(String sdkToken);

    /**
     * 取出一个空闲坐席
     *
     * @param companyCode        企业id
     * @param skillId            技能id
     * @param agentServiceModeEnum 坐席服务模式
     * @param idleStrategyEnum   空闲策略
     * @return 空闲坐席
     */
    TransferAgentQueueDTO popFreeAgentQueue(String companyCode,
                                            String skillId,
                                            AgentServiceModeEnum agentServiceModeEnum,
                                            IdleStrategyEnum idleStrategyEnum);

    void dealAgentCheckinCache(AgentCheckinCacheDTO agentCheckinCacheDTO);

}
