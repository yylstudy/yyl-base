package com.cqt.rpc.sdk;

import com.cqt.model.agent.bo.AgentStatusTransferBO;
import com.cqt.model.agent.dto.AgentInfoEditDTO;
import com.cqt.model.agent.dto.AgentNotifyDTO;
import com.cqt.model.agent.dto.SkillAgentDTO;
import com.cqt.model.agent.vo.AgentInfoVO;
import com.cqt.model.agent.vo.SkillAgentVO;
import com.cqt.model.client.base.ClientBase;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientGetTokenDTO;
import com.cqt.model.client.vo.ClientRequestVO;
import com.cqt.model.queue.entity.IvrServiceInfo;
import com.cqt.model.skill.entity.SkillInfo;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-06-29 10:18
 * SDK Interface服务 rpc接口
 */
public interface SdkInterfaceRemoteService {

    /**
     * 前端SDK -> ws - > sdk-interface
     *
     * @param requestBody json参数
     * @return SdkResponseBaseVO 异步返回
     */
    ClientBase request(String requestBody) throws Exception;

    /**
     * 坐席被删除通知SDK-Interface
     *
     * @param agentNotifyList 通知数据
     */
    void agentChangeNotify(List<AgentNotifyDTO> agentNotifyList);

    /**
     * 坐席状态变更-通话状态改变使用
     */
    Boolean agentStatusChangeTransfer(AgentStatusTransferBO agentStatusTransferBO) throws Exception;

    /**
     * 从企业离线坐席队列删除坐席
     * 提供话务控制调用
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     */
    void deleteOfflineAgentQueue(String companyCode, String agentId);

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
     * 从企业空闲坐席队列删除坐席
     * 提供话务控制调用
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     */
    Boolean deleteFreeAgentQueue(String companyCode, String agentId);

    /**
     * 从企业空闲坐席队列新增坐席
     * 提供话务控制调用
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @param timestamp   空闲的时间戳
     */
    Boolean addFreeAgentQueue(String companyCode, String agentId, Long timestamp);

    /**
     * 取消事后处理任务
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 是否成功
     */
    Boolean cancelArrangeTask(String companyCode, String agentId);

    /**
     * 取消事后处理任务-rpc
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 是否成功
     */
    Boolean cancelArrangeTaskRpc(String companyCode, String agentId);

    /**
     * 示忙
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     */
    void makeAgentBusy(String companyCode, String agentId);

    ClientRequestVO<List<SkillInfo>> getSkillServiceList(String companyCode, String serviceName);

    ClientRequestVO<List<IvrServiceInfo>> getIvrServiceList(String companyCode, String serviceName);

    ClientRequestVO<AgentInfoVO> getAgentInfo(String companyCode, String agentId);

    ClientRequestVO<Void> updateAgentInfo(AgentInfoEditDTO agentInfoEditDTO);

    ClientRequestVO<List<SkillAgentVO>> getAgentList(SkillAgentDTO skillAgentDTO);

    ClientResponseBaseVO getToken(ClientGetTokenDTO clientGetTokenDTO) throws Exception;
}
