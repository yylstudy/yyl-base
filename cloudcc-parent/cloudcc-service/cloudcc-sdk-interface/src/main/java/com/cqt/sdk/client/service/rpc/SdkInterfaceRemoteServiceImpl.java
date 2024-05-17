package com.cqt.sdk.client.service.rpc;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.CallRoleEnum;
import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.base.enums.SdkErrCode;
import com.cqt.base.enums.agent.AgentCallingSubStatusEnum;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.base.util.TraceIdUtil;
import com.cqt.cloudcc.manager.cache.AgentCheckinCache;
import com.cqt.cloudcc.manager.context.AgentInfoContext;
import com.cqt.model.agent.bo.AgentStatusTransferBO;
import com.cqt.model.agent.dto.*;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.agent.vo.AgentInfoVO;
import com.cqt.model.agent.vo.SkillAgentVO;
import com.cqt.model.client.base.ClientBase;
import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientGetTokenDTO;
import com.cqt.model.client.vo.ClientAgentStatusChangeVO;
import com.cqt.model.client.vo.ClientRequestVO;
import com.cqt.model.queue.dto.AgentCheckinCacheDTO;
import com.cqt.model.queue.entity.IvrServiceInfo;
import com.cqt.model.skill.entity.SkillInfo;
import com.cqt.rpc.sdk.SdkInterfaceRemoteService;
import com.cqt.sdk.api.service.AgentRequestService;
import com.cqt.sdk.client.event.agentstatus.FreeAgentQueueEvent;
import com.cqt.sdk.client.event.agentstatus.OfflineAgentQueueEvent;
import com.cqt.sdk.client.event.arrange.ArrangeTaskCache;
import com.cqt.sdk.client.event.arrange.CallStopArrangeCancelEvent;
import com.cqt.sdk.client.event.arrange.CallStopArrangeEvent;
import com.cqt.sdk.client.event.mq.AgentStatusLogStoreEvent;
import com.cqt.sdk.client.service.DataQueryService;
import com.cqt.sdk.client.service.DataStoreService;
import com.cqt.sdk.client.strategy.client.ClientRequestStrategyFactory;
import com.cqt.sdk.client.strategy.client.impl.AbstractClientChecker;
import com.cqt.sdk.client.strategy.client.impl.GetTokenRequestStrategyImpl;
import com.cqt.sdk.config.ThreadPoolConfig;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * @author linshiqiang
 * date:  2023-06-29 10:43
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class SdkInterfaceRemoteServiceImpl extends AbstractClientChecker implements SdkInterfaceRemoteService {

    private final ClientRequestStrategyFactory clientRequestStrategyFactory;

    private final ObjectMapper objectMapper;

    private final ApplicationContext applicationContext;

    private final DataStoreService dataStoreService;

    private final DataQueryService dataQueryService;

    private final RedissonUtil redissonUtil;

    private final AgentRequestService agentRequestService;

    private final GetTokenRequestStrategyImpl getTokenRequestStrategyImpl;

    private final RocketMQTemplate rocketMQTemplate;

    @Resource(name = ThreadPoolConfig.SDK_POOL_NAME)
    private Executor sdkExecutor;

    /**
     * 前端SDK请求
     */
    @Override
    public ClientBase request(String requestBody) throws Exception {
        // 修改坐席id和分机id  企业id_坐席id, 企业id_分机id
        ClientRequestBaseDTO requestBaseDTO = convert(requestBody, ClientRequestBaseDTO.class);
        TraceIdUtil.setTraceId(getTraceId(requestBaseDTO));
        AgentInfoContext.set(dataQueryService.getAgentInfo(requestBaseDTO.getCompanyCode(), requestBaseDTO.getAgentId()));
        try {
            return clientRequestStrategyFactory.dealCallStatus(requestBaseDTO, requestBody);
        } finally {
            AgentInfoContext.remove();
            TraceIdUtil.remove();
        }
    }

    /**
     * 构造traceId
     */
    private String getTraceId(ClientRequestBaseDTO requestBaseDTO) {
        return TraceIdUtil.buildTraceId(requestBaseDTO.getReqId(),
                requestBaseDTO.getMsgType(),
                requestBaseDTO.getCompanyCode(),
                requestBaseDTO.getAgentId(),
                requestBaseDTO.getExtId());
    }

    /**
     * 坐席被删除通知
     */
    @Override
    public void agentChangeNotify(List<AgentNotifyDTO> agentNotifyList) {
        log.info("[agentChangeNotify] size: {}", agentNotifyList);
        for (AgentNotifyDTO agentNotifyDTO : agentNotifyList) {
            String companyCode = agentNotifyDTO.getCompanyCode();
            String agentId = agentNotifyDTO.getAgentId();

            Integer serviceMode = agentNotifyDTO.getServiceMode();
            AgentServiceModeEnum serviceModeEnum = AgentServiceModeEnum.parse(serviceMode);
            log.info("[坐席删除通知] 企业: {}, 坐席: {}", companyCode, agentId);
            try {
                // 删除坐席状态信息
                dataStoreService.deleteActualAgentStatus(companyCode, agentId);
                // 删除离线坐席 队列
                applicationContext.publishEvent(new OfflineAgentQueueEvent(this,
                        companyCode, agentId, serviceModeEnum, System.currentTimeMillis(), OperateTypeEnum.DELETE));
                // 删除空闲坐席 队列
                applicationContext.publishEvent(new FreeAgentQueueEvent(this, companyCode, agentId,
                        serviceModeEnum, System.currentTimeMillis(), OperateTypeEnum.DELETE));

                AgentCheckinCacheDTO agentCheckinCacheDTO = AgentCheckinCacheDTO.buildDel(companyCode, agentId);
                agentCheckinCacheDTO.setMsg("agent del");
                AgentCheckinCache.remove(companyCode, agentId);
                // topicName:tags
                Message<AgentCheckinCacheDTO> message = MessageBuilder.withPayload(agentCheckinCacheDTO)
                        .setHeader(MessageConst.PROPERTY_KEYS, agentCheckinCacheDTO.getAgentId())
                        .build();
                rocketMQTemplate.syncSend("cloudcc_broadcast_topic:agentWeight", message);
            } catch (Exception e) {
                log.error("[坐席删除通知] 企业: {}, 坐席: {}, 处理异常: ", companyCode, agentId, e);
            }
        }
    }

    /**
     * 坐席状态变更通知
     */
    @Override
    public Boolean agentStatusChangeTransfer(AgentStatusTransferBO agentStatusTransferBO) throws Exception {
        String companyCode = agentStatusTransferBO.getCompanyCode();
        String agentId = agentStatusTransferBO.getAgentId();
        log.info("[状态迁移] 企业id: {}, 坐席id: {}, data: {}",
                companyCode, agentId, objectMapper.writeValueAsString(agentStatusTransferBO));
        // 查询坐席状态
        Optional<AgentStatusDTO> agentStatusOptional = dataQueryService.getAgentStatusDTO(companyCode, agentId);
        if (!agentStatusOptional.isPresent()) {
            log.warn("[状态迁移] 企业id: {}, 坐席id: {}, 未查询到坐席实时状态!", companyCode, agentId);
            return false;
        }
        AgentStatusDTO agentStatusDTO = agentStatusOptional.get();
        AgentStatusTransferActionEnum transferAction = agentStatusTransferBO.getTransferAction();
        AgentStatusEnum targetStatus = agentStatusTransferBO.getTargetStatus();

        if (AgentStatusTransferActionEnum.BRIDGE.equals(transferAction)) {
            agentStatusDTO.setCallStartTime(agentStatusTransferBO.getEventTimestamp());
        }

        // 外呼事件 只修改通话前的状态
        if (AgentStatusTransferActionEnum.CALLOUT.equals(transferAction)) {
            agentStatusDTO.setUuid(agentStatusTransferBO.getUuid());
            agentStatusDTO.setExtId(agentStatusTransferBO.getExtId());
            agentStatusDTO.setBeforeCallStartAgentStatus(agentStatusDTO.getTargetStatus());
            dataStoreService.updateActualAgentStatus(agentStatusDTO);
            AgentServiceModeEnum serviceMode = AgentServiceModeEnum.parse(agentStatusDTO.getServiceMode());
            // 空闲队列移除
            applicationContext.publishEvent(new FreeAgentQueueEvent(this, companyCode, agentId,
                    serviceMode, System.currentTimeMillis(), OperateTypeEnum.DELETE));
            log.info("[状态迁移-外呼事件] 企业坐席空闲队列-删除, 企业: {}, 坐席: {}", companyCode, agentId);
            return true;
        }

        // 挂断事件
        /*
         * 1. 设置通话结束后状态 -> after状态
         * 2. 坐席未接或拒接,
         *   2.1 配置自动示忙 -> 忙碌
         *   2.2 未配置自动示忙 -> before状态
         * 3. 坐席接通挂断
         *   3.1 开启事后处理 -> 事后处理
         *   3.2 未开启事后处理 -> before状态
         */
        if (AgentStatusTransferActionEnum.HANGUP.equals(transferAction)) {
            AgentStatusDTO copyStatusDTO = new AgentStatusDTO();
            agentHangupStatus(agentStatusTransferBO, agentStatusDTO, copyStatusDTO);
            // 清空通话结束后状态设置 通话开始结束状态
            agentStatusDTO.hangupSetNull();
            transferAction = AgentStatusTransferActionEnum.valueOf(copyStatusDTO.getTransferAction());
            targetStatus = AgentStatusEnum.valueOf(copyStatusDTO.getTargetStatus());
        }

        // 构造坐席状态迁移实体
        AgentCallingSubStatusEnum targetSubStatus = agentStatusTransferBO.getTargetSubStatus();
        CallRoleEnum callRoleEnum = agentStatusTransferBO.getCallRoleEnum();
        AgentStatusTransferDTO agentStatusTransferDTO = AgentStatusTransferDTO.builder()
                .companyCode(companyCode)
                .uuid(agentStatusTransferBO.getUuid())
                .agentId(agentId)
                .extId(agentStatusDTO.getExtId())
                .os(agentStatusDTO.getOs())
                .sourceStatus(agentStatusDTO.getTargetStatus())
                .sourceSubStatus(agentStatusDTO.getTargetSubStatus())
                .sourceTimestamp(agentStatusDTO.getTargetTimestamp())
                .sourceDuration(agentStatusDTO.getTargetDuration())
                .transferAction(transferAction.name())
                .targetStatus(Objects.nonNull(targetStatus) ? targetStatus.name() : null)
                .targetSubStatus(Objects.nonNull(targetSubStatus) ? targetSubStatus.name() : null)
                .targetTimestamp(agentStatusTransferBO.getEventTimestamp())
                .callRole(Objects.nonNull(callRoleEnum) ? callRoleEnum.name() : null)
                .beforeCallStartAgentStatus(agentStatusDTO.getTargetStatus())
                .reason(agentStatusTransferBO.getReason())
                .build();

        // 更新状态实时状态-redis
        dataStoreService.updateActualAgentStatus(agentStatusDTO.build(agentStatusTransferDTO));

        // 若是变更为空闲状态, 添加空闲队列
        if (AgentStatusEnum.FREE.equals(targetStatus)) {
            applicationContext.publishEvent(new FreeAgentQueueEvent(this, companyCode, agentId,
                    System.currentTimeMillis(), OperateTypeEnum.INSERT));
            log.info("[状态迁移-空闲] 企业坐席空闲队列-新增, 企业: {}, 坐席: {}", companyCode, agentId);
        }

        // 坐席迁移日志发-mq
        applicationContext.publishEvent(new AgentStatusLogStoreEvent(this, agentStatusTransferDTO));

        ClientAgentStatusChangeVO agentStatusChangeVO = ClientAgentStatusChangeVO.build(agentStatusDTO);
        if (AgentStatusTransferActionEnum.ANSWER.equals(transferAction)) {
            Set<String> inCallNumbers = dataQueryService.getInCallNumbers(agentStatusDTO);
            agentStatusChangeVO.setInCallNumbers(inCallNumbers);
        }
        // 通知-netty
        dataStoreService.notifySdkAgentStatus(agentStatusChangeVO);
        return true;
    }

    private void agentHangupStatus(AgentStatusTransferBO agentStatusTransferBO,
                                   AgentStatusDTO agentStatusDTO,
                                   AgentStatusDTO copyStatusDTO) {
        String companyCode = agentStatusDTO.getCompanyCode();
        String agentId = agentStatusDTO.getAgentId();
        String afterCallStopAgentStatus = agentStatusDTO.getAfterCallStopAgentStatus();
        String afterCallStopAction = agentStatusDTO.getAfterCallStopAction();

        if (StrUtil.isNotEmpty(afterCallStopAgentStatus) && StrUtil.isNotEmpty(afterCallStopAction)) {
            log.info("[状态迁移-挂断事件] 企业: {}, 坐席: {}, 通话结束后: {}", companyCode, agentId, afterCallStopAgentStatus);
            copyStatusDTO.setTargetStatus(afterCallStopAgentStatus);
            copyStatusDTO.setTransferAction(afterCallStopAction);
            copyStatusDTO.setRestMin(agentStatusDTO.getAfterCallStopRestMin());
            return;
        }
        AgentInfo agentInfo = dataQueryService.getAgentInfo(companyCode, agentId);

        // 外呼失败, 呼入成功未接通, 回到通话前状态
        Boolean callInAgentClientHangUpFirst = agentStatusTransferBO.getCallInAgentClientHangUpFirst();
        Boolean agentCallOutFail = agentStatusTransferBO.getAgentCallOutFail();
        if (Boolean.TRUE.equals(callInAgentClientHangUpFirst) || Boolean.TRUE.equals(agentCallOutFail)) {
            String beforeCallStartAgentStatus = agentStatusDTO.getBeforeCallStartAgentStatus();
            log.info("[状态迁移-挂断事件] 企业: {}, 坐席: {}, 呼入: {}, 呼出: {}, 回到通话前状态: {}",
                    companyCode, agentId, callInAgentClientHangUpFirst, agentCallOutFail, beforeCallStartAgentStatus);
            if (StrUtil.isEmpty(beforeCallStartAgentStatus)) {
                log.warn("[状态迁移-挂断事件] 企业: {}, 坐席: {}, 通话开始前状态未配置为空!", companyCode, agentId);
                copyStatusDTO.setTargetStatus(AgentStatusEnum.BUSY.name());
            } else {
                copyStatusDTO.setTargetStatus(beforeCallStartAgentStatus);
            }
            copyStatusDTO.setTransferAction(AgentStatusTransferActionEnum.HANGUP.name());
            return;
        }

        // 坐席没有接通
        if (Boolean.TRUE.equals(agentStatusTransferBO.getAgentNoAnswerMakerBusy())) {
            // 呼入 未接, 原来状态
            if (Boolean.TRUE.equals(agentStatusTransferBO.getCheckAgentBridgeStatus())) {
                Integer autoShowBusy = agentInfo.getAutoShowBusy();
                log.info("[状态迁移-挂断事件] 企业: {}, 坐席: {}, 坐席未接, 自动示忙: {}",
                        companyCode, agentId, autoShowBusy);
                if (Objects.nonNull(autoShowBusy) && 1 == autoShowBusy) {
                    copyStatusDTO.setTargetStatus(AgentStatusEnum.BUSY.name());
                    copyStatusDTO.setTransferAction(AgentStatusTransferActionEnum.FORCE_MAKE_BUSY.name());
                    // 空闲队列移除
                    applicationContext.publishEvent(new FreeAgentQueueEvent(this, companyCode, agentId,
                            System.currentTimeMillis(), OperateTypeEnum.DELETE));
                    return;
                }
            }

            String beforeCallStartAgentStatus = agentStatusDTO.getBeforeCallStartAgentStatus();
            log.info("[状态迁移-挂断事件] 企业: {}, 坐席: {}, 坐席未接, 回到通话前状态: {}",
                    companyCode, agentId, beforeCallStartAgentStatus);
            if (StrUtil.isEmpty(beforeCallStartAgentStatus)) {
                log.warn("[状态迁移-挂断事件] 企业: {}, 坐席: {}, 坐席未接 通话开始前状态未配置为空!", companyCode, agentId);
                copyStatusDTO.setTargetStatus(AgentStatusEnum.BUSY.name());
            } else {
                copyStatusDTO.setTargetStatus(beforeCallStartAgentStatus);
            }
            copyStatusDTO.setTransferAction(AgentStatusTransferActionEnum.HANGUP.name());
            return;
        }

        // 事后处理 0：关闭 1：开启
        Integer postProcess = agentInfo.getPostProcess();
        if (Objects.nonNull(postProcess) && 1 == postProcess) {
            log.info("[状态迁移-挂断事件] 企业: {}, 坐席: {}, 开启事后处理", companyCode, agentId);
            copyStatusDTO.setTargetStatus(AgentStatusEnum.ARRANGE.name());
            copyStatusDTO.setTransferAction(AgentStatusTransferActionEnum.MAKE_ARRANGE.name());
            // 开启定时器
            startArrangeTask(agentStatusTransferBO, agentStatusDTO, agentInfo.getProcessTime());
            return;
        }

        String beforeCallStartAgentStatus = agentStatusDTO.getBeforeCallStartAgentStatus();
        log.info("[状态迁移-挂断事件] 企业: {}, 坐席: {}, 坐席已接, 回到通话前状态: {}",
                companyCode, agentId, beforeCallStartAgentStatus);
        if (StrUtil.isEmpty(beforeCallStartAgentStatus)) {
            log.warn("[状态迁移-挂断事件] 企业: {}, 坐席: {}, 坐席已接 通话开始前状态未配置为空!", companyCode, agentId);
            copyStatusDTO.setTargetStatus(AgentStatusEnum.BUSY.name());
        } else {
            copyStatusDTO.setTargetStatus(beforeCallStartAgentStatus);
        }
        copyStatusDTO.setTransferAction(AgentStatusTransferActionEnum.HANGUP.name());

    }

    private void startArrangeTask(AgentStatusTransferBO agentStatusTransferBO,
                                  AgentStatusDTO agentStatusDTO,
                                  Integer arrangeSecond) {
        applicationContext.publishEvent(new CallStopArrangeEvent(this,
                AgentStatusTransferBO.newArrangeBuild(agentStatusTransferBO, agentStatusDTO),
                arrangeSecond));
    }

    @Override
    public void deleteOfflineAgentQueue(String companyCode, String agentId) {
        applicationContext.publishEvent(new OfflineAgentQueueEvent(this,
                companyCode, agentId, System.currentTimeMillis(), OperateTypeEnum.DELETE));
    }

    @Override
    public void addOfflineAgentQueue(String companyCode, String agentId, Long timestamp, String phoneNumber) {
        applicationContext.publishEvent(new OfflineAgentQueueEvent(this,
                companyCode, agentId, phoneNumber, timestamp, OperateTypeEnum.INSERT));
    }

    @Override
    public Boolean deleteFreeAgentQueue(String companyCode, String agentId) {
        applicationContext.publishEvent(new FreeAgentQueueEvent(this, companyCode, agentId,
                System.currentTimeMillis(), OperateTypeEnum.DELETE));
        return true;
    }

    @Override
    public Boolean addFreeAgentQueue(String companyCode, String agentId, Long timestamp) {
        applicationContext.publishEvent(new FreeAgentQueueEvent(this,
                companyCode, agentId, System.currentTimeMillis(), OperateTypeEnum.INSERT));
        return true;
    }

    @Override
    public Boolean cancelArrangeTask(String companyCode, String agentId) {
        boolean cancel = ArrangeTaskCache.cancel(companyCode, agentId);
        log.info("[取消事后处理任务-feign] 企业: {}, 坐席: {}, 结果: {}", companyCode, agentId, cancel);
        if (cancel) {
            String arrangeTaskKey = CacheUtil.getArrangeTaskKey(companyCode, agentId);
            redissonUtil.delKey(arrangeTaskKey);
        }
        return cancel;
    }

    @Override
    public Boolean cancelArrangeTaskRpc(String companyCode, String agentId) {
        applicationContext.publishEvent(new CallStopArrangeCancelEvent(this, companyCode, agentId));
        return true;
    }

    @Override
    public void makeAgentBusy(String companyCode, String agentId) {

    }

    @Override
    public ClientRequestVO<List<SkillInfo>> getSkillServiceList(String companyCode, String serviceName) {
        return dataQueryService.getSkillServiceList(companyCode, serviceName);
    }

    @Override
    public ClientRequestVO<List<IvrServiceInfo>> getIvrServiceList(String companyCode, String serviceName) {
        return dataQueryService.getIvrServiceList(companyCode, serviceName);
    }

    @Override
    public ClientRequestVO<AgentInfoVO> getAgentInfo(String companyCode, String agentId) {
        AgentInfo agentInfo = dataQueryService.getAgentInfo(companyCode, agentId);
        AgentInfoVO agentInfoVO = AgentInfoVO.convert(agentInfo);
        return ClientRequestVO.response(companyCode, agentInfoVO, SdkErrCode.OK);
    }

    @Override
    public ClientRequestVO<Void> updateAgentInfo(AgentInfoEditDTO agentInfoEditDTO) {
        return dataStoreService.updateAgentInfo(agentInfoEditDTO);
    }

    @Override
    public ClientRequestVO<List<SkillAgentVO>> getAgentList(SkillAgentDTO skillAgentDTO) {
        List<SkillAgentVO> agentList = agentRequestService.getAgentList(skillAgentDTO.getCompanyCode(),
                skillAgentDTO.getPageSize(),
                skillAgentDTO.getPageNo(),
                skillAgentDTO.getAgentId(),
                skillAgentDTO.getSkillId(),
                skillAgentDTO.getKeyword());

        return ClientRequestVO.response(agentList, SdkErrCode.OK);
    }

    @Override
    public ClientResponseBaseVO getToken(ClientGetTokenDTO clientGetTokenDTO) throws Exception {
        clientGetTokenDTO.setMsgType(MsgTypeEnum.get_token.name());
        String value = objectMapper.writeValueAsString(clientGetTokenDTO);
        return getTokenRequestStrategyImpl.deal(value);
    }
}
