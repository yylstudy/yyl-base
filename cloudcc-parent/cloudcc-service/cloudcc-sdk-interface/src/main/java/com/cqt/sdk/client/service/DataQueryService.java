package com.cqt.sdk.client.service;

import com.cqt.model.agent.dto.AgentCheckInRecord;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.client.vo.ClientRequestVO;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.freeswitch.vo.GetExtensionRegVO;
import com.cqt.model.queue.dto.AgentWeightInfoDTO;
import com.cqt.model.queue.entity.IvrServiceInfo;
import com.cqt.model.skill.entity.SkillInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-07-12 14:59
 */
public interface DataQueryService {

    /**
     * 查询坐席实时状态
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 状态
     */
    Optional<AgentStatusDTO> getAgentStatusDTO(String companyCode, String agentId);

    /**
     * 查询坐席基本信息-配置
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 分机坐席信息
     */
    AgentInfo getAgentInfo(String companyCode, String agentId);

    /**
     * 查询实时的分机注册状态
     *
     * @param companyCode 企业编码
     * @param extId       分机id
     * @return GetExtensionRegVO
     */
    GetExtensionRegVO getExtRealRegStatus(String companyCode, String extId);

    /**
     * 从redis查询分机实时状态
     *
     * @param companyCode 企业id
     * @param extId       分机id
     * @return 分机实时状态
     */
    ExtStatusDTO getActualExtStatus(String companyCode, String extId);

    /**
     * 查询坐席签入记录
     * @param agentId 坐席id
     * @param os 操作系统
     * @return 坐席签入记录
     * */
    AgentCheckInRecord getAgentCheckInRecord(String agentId, String os);

    /**
     * 查询通话中的号码
     *
     * @param agentStatusDTO 坐席状态信息
     * @return 通话中的号码
     */
    Set<String> getInCallNumbers(AgentStatusDTO agentStatusDTO);

    /**
     * 获取技能组列表
     *
     * @param companyCode 企业id
     * @return 坐席技能组列表
     */
    ClientRequestVO<List<SkillInfo>> getSkillServiceList(String companyCode, String serviceName);

    /**
     * 获取ivr技能组列表
     *
     * @param companyCode 企业id
     * @return ivr技能组列表
     */
    ClientRequestVO<List<IvrServiceInfo>> getIvrServiceList(String companyCode, String serviceName);

    /**
     * 判断目标状态是否可以转移到源状态
     *
     * @param targetStatus 目标状态
     * @param sourceStatus 源状态
     * @return 是否可以转移
     */
    boolean isDealAgentStatus(String targetStatus, String sourceStatus);

    void initCallStats(String companyCode, String agentId, AgentWeightInfoDTO agentWeightInfoDTO);

}
