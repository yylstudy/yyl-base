package com.cqt.sdk.client.service;

import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.model.agent.dto.AgentCheckInRecord;
import com.cqt.model.agent.dto.AgentInfoEditDTO;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.client.dto.ClientCheckinDTO;
import com.cqt.model.client.dto.ClientCheckoutDTO;
import com.cqt.model.client.vo.ClientAgentStatusChangeVO;
import com.cqt.model.client.vo.ClientRequestVO;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.freeswitch.vo.DisExtensionRegAddrVO;
import com.cqt.model.freeswitch.vo.GetExtensionRegVO;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author linshiqiang
 * date:  2023-07-12 14:59
 */
public interface DataStoreService {

    /**
     * 保存分机实时状态
     *
     * @param extStatusDTO 分机实时状态
     */
    void updateActualExtStatus(ExtStatusDTO extStatusDTO);

    /**
     * 保存坐席实时状态
     *
     * @param agentStatusDTO 坐席实时状态
     */
    void updateActualAgentStatus(AgentStatusDTO agentStatusDTO);

    /**
     * 删除坐席实时状态
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     */
    void deleteActualAgentStatus(String companyCode, String agentId);

    /**
     * 坐席队列-删除
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @param serviceMode 服务模式
     * @param free        是否空闲
     * @throws Exception 异常
     */
    void deleteAgentQueue(String companyCode, String agentId, AgentServiceModeEnum serviceMode, boolean free);

    /**
     * 坐席队列-新增
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @param serviceMode 服务模式
     * @param free        是否空闲
     * @param timestamp   时间戳
     * @param phoneNumber 手机号
     * @throws Exception 异常
     */
    void addAgentQueue(String companyCode, String agentId, AgentServiceModeEnum serviceMode, boolean free,
                       Long timestamp, String phoneNumber);

    /**
     * 通知前端sdk 坐席状态变化
     *
     * @param agentStatusChangeVO 坐席状态变化通知
     */
    void notifySdkAgentStatus(ClientAgentStatusChangeVO agentStatusChangeVO);

    /**
     * 坐席签入记录
     *
     * @param clientCheckinDTO 签入请求参数
     */
    void addCheckInRecord(ClientCheckinDTO clientCheckinDTO);

    /**
     * 设置分机与坐席进行绑定
     *
     * @param extId   分机id
     * @param agentId 坐席id
     */
    void extBindAgent(String extId, String agentId);

    /**
     * 删除坐席签入记录
     *
     * @param clientCheckoutDTO 签出请求参数
     */
    void removeCheckInRecord(ClientCheckoutDTO clientCheckoutDTO);

    /**
     * 通知sdk下线
     *
     * @param checkinDTO 坐席签入请求参数
     * @param lastRecord 上一次的签入记录
     */
    void agentKickoffNotify(ClientCheckinDTO checkinDTO, AgentCheckInRecord lastRecord) throws JsonProcessingException;

    /**
     * 请求底层分配分机注册地址
     *
     * @param companyCode 企业编码
     * @param extId       分机id
     * @return DisExtensionRegAddrVO
     */
    DisExtensionRegAddrVO disExtRegAddr(String companyCode, String extId);

    /**
     * 比较自己存的分机状态和底层查询的分机状态
     *
     * @param extStatusDTO          自己查
     * @param copyGetExtensionRegVO 底层
     */
    void compareExtStatus(ExtStatusDTO extStatusDTO, GetExtensionRegVO copyGetExtensionRegVO);

    ClientRequestVO<Void> updateAgentInfo(AgentInfoEditDTO agentInfoEditDTO);

}
