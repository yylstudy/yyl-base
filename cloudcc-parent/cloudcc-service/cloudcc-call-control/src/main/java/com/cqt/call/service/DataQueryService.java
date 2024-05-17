package com.cqt.call.service;

import com.cqt.base.enums.CallDirectionEnum;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.ext.dto.ExtStatusDTO;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-07-05 16:18
 * redis和mysql数据查询服务
 */
public interface DataQueryService {

    /**
     * 查询坐席基本信息-配置
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 分机坐席信息
     */
    AgentInfo getAgentInfo(String companyCode, String agentId) throws Exception;

    /**
     * 查询坐席基本信息-配置
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 分机坐席信息
     */
    AgentInfo getAgentInfo(String companyCode, String agentId, boolean context) throws Exception;


    /**
     * 坐席与分机绑定关系
     *
     * @param companyCode 企业id
     * @param extId 分机id
     * @return 坐席id
     */
    String getAgentIdRelateExtId(String companyCode, String extId);

    /**
     * 从redis查询分机实时状态
     *
     * @param companyCode 企业id
     * @param extId       分机id
     * @return 分机实时状态
     */
    ExtStatusDTO getActualExtStatus(String companyCode, String extId);

    /**
     * 从redis查询坐席实时状态
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 坐席实时状态
     */
    Optional<AgentStatusDTO> getActualAgentStatus(String companyCode, String agentId);


    /**
     * 获取通话uuid上下文
     *
     * @param companyCode 企业id
     * @param uuid        通话id
     * @return 上下文
     */
    CallUuidContext getCallUuidContext(String companyCode, String uuid);

    /**
     * 查询企业信息
     *
     * @param companyCode 企业id
     * @return 企业信息
     */
    CompanyInfo getCompanyInfoDTO(String companyCode) throws Exception;

    /**
     * 外呼客户号码时的外显号码
     *
     * @param companyInfo 企业信息
     * @param agentInfo   坐席信息
     * @return 外显号码
     */
    String getAgentDisplayNumber(CompanyInfo companyInfo, AgentInfo agentInfo);

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
     * 查询ivr文件名称
     *
     * @param companyCode  企业id
     * @param ivrId        id
     * @param satisfaction 是否为满意度
     * @return ivr文件名称
     */
    String getIvrName(String companyCode, String ivrId, Boolean satisfaction);

    /**
     * 获取话单连接
     *
     * @param companyCode 企业id
     * @param mainCallId  通话id
     * @return set
     */
    Set<String> getCdrLink(String companyCode, String mainCallId);

    /**
     * 获取通道变量
     *
     * @param companyCode 企业id
     * @param uuid        通话id
     * @return 通道变量map
     */
    Map<String, Object> getCdrChannelData(String companyCode, String uuid);

    /**
     * 生成主通话id
     *
     * @return 主通话id
     */
    String createMainCallId();

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
     * 检测号码是否在黑名单内
     *
     * @param companyCode   企业id
     * @param number        号码
     * @param callDirection 呼叫方向
     * @return 是否在黑名单内
     */
    Boolean checkBlackNumber(String companyCode, String number, CallDirectionEnum callDirection);

}
