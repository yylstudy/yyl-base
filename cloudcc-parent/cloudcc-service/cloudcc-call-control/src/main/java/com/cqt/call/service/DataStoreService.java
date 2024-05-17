package com.cqt.call.service;

import com.cqt.base.model.ResultVO;
import com.cqt.model.agent.bo.AgentStatusTransferBO;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.queue.dto.CallInIvrActionDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;

/**
 * @author linshiqiang
 * date:  2023-07-06 14:59
 * 数据存储服务
 */
public interface DataStoreService {

    /**
     * 从企业离线坐席队列新增坐席
     * 提供话务控制调用
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @param timestamp   空闲的时间戳
     * @param phoneNumber 手机号码
     */
    void addOfflineAgentQueue(String companyCode, String agentId, Long timestamp, String phoneNumber);

    /**
     * 将用户加到排队队列
     *
     * @param userQueueUpDTO 排队信息
     * @return 是否成功
     */
    Boolean addUserLevelQueue(UserQueueUpDTO userQueueUpDTO);

    /**
     * 保存分机实时状态
     *
     * @param extStatusDTO 分机实时状态
     */
    void updateActualExtStatus(ExtStatusDTO extStatusDTO);

    /**
     * 坐席状态变更-通话状态改变使用
     * SDK-Interface dubbo接口
     *
     * @param agentStatusTransferBO 坐席状态迁移实体
     * @return true
     */
    Boolean agentStatusChangeTransfer(AgentStatusTransferBO agentStatusTransferBO);

    /**
     * 保存通话uuid之间关联关系
     *
     * @param callUuidContext uuid上下文
     */
    void saveCallUuidContext(CallUuidContext callUuidContext);

    /**
     * 外呼失败-删除uuid上下文
     *
     * @param companyCode 企业id
     * @param uuid        uuid
     */
    void delCallUuidContext(String companyCode, String uuid);

    /**
     * 转技能-分配坐席
     *
     * @param callInIvrActionDTO 分配坐席信息
     * @return result
     */
    ResultVO<CallInIvrActionVO> distributeAgent(CallInIvrActionDTO callInIvrActionDTO);

    /**
     * 操作结果通知前端
     *
     * @param message 消息
     */
    void notifyClient(Object message);

    /**
     * 坐席示忙
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     */
    void makeAgentBusy(String companyCode, String agentId);

    /**
     * 保存uuid挂断标志
     *
     * @param companyCode 企业id
     * @param uuid        通话id
     * @param timestamp   挂断时间戳
     */
    void saveUuidHangupFlag(String companyCode, String uuid, Long timestamp);

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
     * 保存通道变量
     *
     * @param companyCode 企业id
     * @param uuid        话单id
     * @param channelData 通道变量json
     */
    void saveCdrChannelData(String companyCode, String uuid, String channelData);

    /**
     * 移除排队人员
     *
     * @param userQueueUpDTO userQueueUpDTO
     * @return true/false
     */
    boolean removeQueueUp(UserQueueUpDTO userQueueUpDTO);

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

    /***
     * 保存话单生成标志
     * @param companyCode 企业id
     * @param mainCallId  主通话id
     */
    void saveCdrGenerateFlag(String companyCode, String mainCallId);

    /**
     * 取消事后处理任务
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 是否成功
     */
    Boolean cancelArrangeTask(String companyCode, String agentId);

    /**
     * 判断 三方通话是否挂断其他人
     *
     * @param companyCode 企业id
     * @param mainCallId  通话id
     * @param uuid        当前挂断时的uuid
     * @return 三方通话是否挂断其他人
     */
    Boolean threeWayHangupAll(String companyCode, String mainCallId, String uuid);

    void answerIvrNotice(String companyCode, String taskId, String value);

    void answerPredictNotice(String companyCode, String taskId, String member);

    void saveAgentConsultFlag(String uuid);

    void delAgentConsultFlag(String uuid);

    boolean isAgentConsult(String bridgeUUID);

    boolean isOnlyOneCalling(String companyCode, String mainCallId);

    void unlockOriginate(String extId);

    Boolean unlockOriginate(String companyCode, String number);

    Boolean lockOriginate(String companyCode, String number, String uuid);

    /**
     * 设置 号码上一次外呼的时间戳
     *
     * @param taskId 任务id
     * @param item   号码
     */
    void setCallTimestamp(String taskId, String item);
}
