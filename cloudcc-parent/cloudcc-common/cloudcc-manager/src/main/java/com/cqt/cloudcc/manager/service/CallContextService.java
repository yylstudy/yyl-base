package com.cqt.cloudcc.manager.service;

import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.freeswitch.dto.api.GetSessionVarDTO;

import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-08-22 9:41
 */
public interface CallContextService {

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
     * 添加通话id 正在通话的号码
     *
     * @param companyCode 企业id
     * @param mainCallId  主通话id
     * @param number      号码
     * @param uuid        uuid
     */
    void addInCallNumbers(String companyCode, String mainCallId, String number, String uuid);

    /**
     * 删除通话id 正在通话的号码
     *
     * @param companyCode 企业id
     * @param mainCallId  主通话id
     * @param number      号码
     * @param uuid        uuid
     */
    void deleteInCallNumbers(String companyCode, String mainCallId, String number, String uuid);

    /**
     * 获取正在通话的号码
     *
     * @param companyCode 企业id
     * @param mainCallId  主通话id
     * @return Set
     */
    Set<String> getInCallNumbers(String companyCode, String mainCallId);

    /**
     * 查询通话中的号码
     *
     * @param agentStatusDTO 坐席状态信息
     * @return 通话中的号码
     */
    Set<String> getInCallNumbers(AgentStatusDTO agentStatusDTO);

    /**
     * uuid是否已挂断
     *
     * @param companyCode 企业id
     * @param uuid        uuid
     * @return 是否已挂断
     */
    Boolean isHangup(String companyCode, String uuid);

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
     * 话单是否已经生成
     *
     * @param companyCode 企业id
     * @param mainCallId  通话id
     * @return 是否已经生成
     */
    Boolean isCdrGenerated(String companyCode, String mainCallId);

    /**
     * 从fs通道变量中获取
     *
     * @param getSessionVarDTO 参数
     * @param clazz            对象类型
     * @param <T>              泛型
     * @return 对象
     */
    <T> T getChannelVariable(GetSessionVarDTO getSessionVarDTO, Class<T> clazz) throws Exception;

    /**
     * 保存话单平台号码
     *
     * @param companyCode  企业id
     * @param mainCallId   通话id
     * @param platformNumber 平台号码
     */
    void saveCdrPlatformNumber(String companyCode, String mainCallId, String platformNumber);

    /**
     * 查询话单计费号码
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
     * 保存录制文件路径到缓存
     */
    void saveRecordFile(String companyCode, String uuid, String filePath);

    /**
     * 获取录制文件路径从缓存
     */
    String getRecordFile(String companyCode, String uuid);

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
     * @return 是否校验成功
     */
    boolean checkToken(String sdkToken);
}
