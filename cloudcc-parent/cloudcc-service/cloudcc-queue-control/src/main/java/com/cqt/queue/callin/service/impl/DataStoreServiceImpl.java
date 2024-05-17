package com.cqt.queue.callin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.contants.CommonConstant;
import com.cqt.base.enums.*;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.base.util.FreeswitchUtil;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.freeswitch.dto.api.CallBridgeDTO;
import com.cqt.model.freeswitch.dto.api.CallIvrExitDTO;
import com.cqt.model.freeswitch.dto.api.CallQueueToAgentDTO;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.cqt.model.queue.dto.AgentWeightInfoDTO;
import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.model.queue.vo.MatchAgentWeightVO;
import com.cqt.queue.callin.cache.UserQueueCache;
import com.cqt.queue.callin.manager.UserQueueSyncDTO;
import com.cqt.queue.callin.service.DataQueryService;
import com.cqt.queue.callin.service.DataStoreService;
import com.cqt.rpc.sdk.SdkInterfaceRemoteService;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-07-12 15:58
 * 数据存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataStoreServiceImpl implements DataStoreService {

    private final DataQueryService dataQueryService;

    private final ObjectMapper objectMapper;

    private final RedissonUtil redissonUtil;

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    private final RocketMQTemplate rocketMQTemplate;

    @DubboReference
    private SdkInterfaceRemoteService sdkInterfaceRemoteService;

    @Override
    public Boolean addUserLevelQueue(UserQueueUpDTO userQueueUpDTO) {
        try {
            String companyCode = userQueueUpDTO.getCompanyCode();
            String uuid = userQueueUpDTO.getUuid();
            // 排队超时的不重新进队列
            // 开始排队时的时间戳-调用转技能接口时
            Long queueTimestamp = userQueueUpDTO.getCurrentTimestamp();
            // 排队超时时间
            Integer maxQueueTime = userQueueUpDTO.getMaxQueueTime();
            // 当前时间
            long currentTimeMillis = System.currentTimeMillis();
            long duration = (currentTimeMillis - queueTimestamp) / 1000L;
            if (duration > maxQueueTime) {
                if (log.isInfoEnabled()) {
                    log.info("[进排队队列] 已超时, maxQueueTime: {}, duration: {},  data: {}",
                            maxQueueTime, duration, objectMapper.writeValueAsString(userQueueUpDTO));
                }
                callIvrExit(userQueueUpDTO);
                return false;
            }
            String userQueueLevelKey = CacheUtil.getUserQueueLevelKey(userQueueUpDTO.getCompanyCode());
            // userQueueUpDTO.setCurrentTimestamp(System.currentTimeMillis());
            // String value = objectMapper.writeValueAsString(userQueueUpDTO);
            // TODO send mq double room?
            UserQueueSyncDTO userQueueSyncDTO = new UserQueueSyncDTO(userQueueUpDTO, OperateTypeEnum.INSERT);
            Message<UserQueueSyncDTO> message = MessageBuilder.withPayload(userQueueSyncDTO)
                    .setHeader(MessageConst.PROPERTY_KEYS, userQueueUpDTO.getUuid())
                    .build();
            rocketMQTemplate.syncSend("cloudcc_broadcast_topic:userQueue", message);
            String userQueueLevelSkillKey = CacheUtil.getUserQueueLevelSkillKey(companyCode, userQueueUpDTO.getSkillId());
            redissonUtil.setHash(userQueueLevelSkillKey, userQueueUpDTO.getUuid(), userQueueUpDTO.getCurrentTimestamp());
            return redissonUtil.setHash(userQueueLevelKey, userQueueUpDTO.getUuid(), userQueueUpDTO.getCurrentTimestamp());
        } catch (JsonProcessingException e) {
            log.error("[进排队队列] 企业: {}, 来电号码: {}, err: ", userQueueUpDTO.getCompanyCode(), userQueueUpDTO.getCallerNumber(), e);
        }
        return false;
    }

    @Override
    public boolean removeQueueUp(UserQueueUpDTO userQueueUpDTO) {
        String companyCode = userQueueUpDTO.getCompanyCode();
        String callerNumber = userQueueUpDTO.getCallerNumber();
        String uuid = userQueueUpDTO.getUuid();
        try {
            // remove self
            UserQueueCache.remove(companyCode, uuid);
            // TODO send mq
            UserQueueSyncDTO userQueueSyncDTO = new UserQueueSyncDTO(companyCode, uuid);
            Message<UserQueueSyncDTO> message = MessageBuilder.withPayload(userQueueSyncDTO)
                    .setHeader(MessageConst.PROPERTY_KEYS, uuid)
                    .build();
            rocketMQTemplate.syncSend("cloudcc_broadcast_topic:userQueue", message);
            log.info("[移除排队] 企业: {}, 来电号码: {}, uuid: {}", companyCode, callerNumber, uuid);
            String userQueueLevelSkillKey = CacheUtil.getUserQueueLevelSkillKey(companyCode, userQueueUpDTO.getSkillId());
            redissonUtil.removeHashItem(userQueueLevelSkillKey, uuid);
            String userQueueLevelKey = CacheUtil.getUserQueueLevelKey(companyCode);
            return redissonUtil.removeHashItem(userQueueLevelKey, uuid);
        } catch (Exception e) {
            log.error("[移除排队] 企业: {}, 来电号码: {}, err: ", companyCode, callerNumber, e);
        }
        return false;
    }

    public void callIvrExit(UserQueueUpDTO userQueueUpDTO) {
        // retry 3 times?
        String companyCode = userQueueUpDTO.getCompanyCode();
        String uuid = userQueueUpDTO.getUuid();
        Integer type = userQueueUpDTO.getType();
        if (Objects.nonNull(type) && CallInIvrActionEnum.TRANS_SKILl.getCode().equals(type)) {
            HangupDTO hangupDTO = HangupDTO.build(companyCode, uuid, HangupCauseEnum.TRANS_SKILL_TIMEOUT);
            freeswitchRequestService.hangup(hangupDTO);
            return;
        }
        CallIvrExitDTO callIvrExitDTO = CallIvrExitDTO.build(companyCode, uuid);
        freeswitchRequestService.callIvrExit(callIvrExitDTO);
    }

    @Override
    public void noneFreeAgentAndEnterQueue(UserQueueUpDTO userQueueUpDTO) {
        try {
            String companyCode = userQueueUpDTO.getCompanyCode();
            String callerNumber = userQueueUpDTO.getCallerNumber();
            String uuid = userQueueUpDTO.getUuid();
            initCallerNumberLevel(userQueueUpDTO);
            boolean add = addUserLevelQueue(userQueueUpDTO);
            log.info("[呼入-转技能-noneFreeAgent] 企业id: {}, 来电uuid: {}, 来电号码: {}, 加入排队队列: {}",
                    companyCode, uuid, callerNumber, add);
        } catch (Exception e) {
            log.error("[noneFreeAgentAndEnterQueue] data: {}, error: ", userQueueUpDTO, e);
        }
    }

    private void initCallerNumberLevel(UserQueueUpDTO userQueueUpDTO) {
        String companyCode = userQueueUpDTO.getCompanyCode();
        String callerNumber = userQueueUpDTO.getCallerNumber();
        userQueueUpDTO.setLevel(Integer.MAX_VALUE);
        // 排队策略-组合策略 是否启用客户优先级
        if (QueueStrategyEnum.COMBINE.getCode().equals(userQueueUpDTO.getQueueStrategy())) {
            Integer priorityEnable = userQueueUpDTO.getPriorityEnable();
            if (Objects.equals(CommonConstant.ENABLE_Y, priorityEnable)) {
                Integer priorityDatasource = userQueueUpDTO.getPriorityDatasource();
                if (Objects.equals(2, priorityDatasource)) {
                    // 客户来电优先级
                    Integer clientPriority = commonDataOperateService.getClientPriority(companyCode, callerNumber);
                    if (Objects.nonNull(clientPriority)) {
                        userQueueUpDTO.setLevel(clientPriority);
                    }
                }
                // TODO 外部接口
            }
        }
    }

    @Override
    public void deleteOfflineAgentQueue(String companyCode, String agentId) {
        try {
            sdkInterfaceRemoteService.deleteOfflineAgentQueue(companyCode, agentId);
        } catch (Exception e) {
            log.error("[deleteOfflineAgentQueue] companyCode: {}, agentId: {}, error: ", companyCode, agentId, e);
        }
    }

    @Override
    public Boolean deleteFreeAgentQueue(String companyCode, String agentId) {
        // 技能队列也要移除
        try {
            return sdkInterfaceRemoteService.deleteFreeAgentQueue(companyCode, agentId);
        } catch (Exception e) {
            log.error("[deleteFreeAgentQueue] companyCode: {}, agentId: {}, error: ", companyCode, agentId, e);
        }
        return false;
    }

    @Override
    public Boolean addFreeAgentQueue(String companyCode, String agentId, Long timestamp) {
        try {
            return sdkInterfaceRemoteService.addFreeAgentQueue(companyCode, agentId, timestamp);
        } catch (Exception e) {
            log.error("[addFreeAgentQueue] companyCode: {}, agentId: {}, error: ", companyCode, agentId, e);
        }
        return false;
    }

    @Override
    public MatchAgentWeightVO matchAgentWeightBySkillId(List<TransferAgentQueueDTO> freeAgents,
                                                        String companyCode,
                                                        String skillId,
                                                        Boolean free) {
        if (CollUtil.isEmpty(freeAgents)) {
            return MatchAgentWeightVO.notMatch();
        }
        MatchAgentWeightVO matchAgentWeightVO = MatchAgentWeightVO.notMatch();
        List<MatchAgentWeightVO.MatchAgent> matchAgentList = null;
        for (TransferAgentQueueDTO freeAgent : freeAgents) {
            AgentWeightInfoDTO agentWeightInfoDTO = freeAgent.getAgentWeightInfoDTO();
            if (Objects.isNull(agentWeightInfoDTO)) {
                continue;
            }
            Map<String, Integer> agentWeightMap = agentWeightInfoDTO.getAgent();
            if (CollUtil.isEmpty(agentWeightMap)) {
                continue;
            }
            Integer agentWeight = agentWeightMap.get(skillId);
            if (Objects.nonNull(agentWeight)) {
                MatchAgentWeightVO.MatchAgent matchAgent = MatchAgentWeightVO.MatchAgent.builder()
                        .agentId(freeAgent.getAgentId())
                        .phoneNumber(freeAgent.getPhoneNumber())
                        .agentWeight(agentWeight)
                        .freeTimestamp(freeAgent.getTimestamp())
                        .build();
                if (CollUtil.isEmpty(matchAgentList)) {
                    matchAgentList = new ArrayList<>();
                }
                matchAgentList.add(matchAgent);
            }
        }
        matchAgentWeightVO.setMatchAgentList(matchAgentList);
        if (CollUtil.isEmpty(matchAgentList)) {
            log.info("[查坐席权值] 空闲: {} 企业: {}, 技能: {}, 没有一个匹配的坐席", free, companyCode, skillId);
            matchAgentWeightVO.setExistMatchAgent(false);
            return matchAgentWeightVO;
        }
        log.info("[查坐席权值] 空闲: {} 企业: {}, 技能: {}, 技能id匹配的坐席数: {}", free, companyCode, skillId, matchAgentList.size());

        if (CollUtil.isNotEmpty(matchAgentList)) {
            int size = matchAgentList.size();
            for (int i = 0; i < size; i++) {
                matchAgentWeightVO.setExistMatchAgent(false);
                matchAgentWeightVO.setExistMax(false);
                // 匹配处理
                matchAgentAction(matchAgentWeightVO, freeAgents);
                // 判断坐席是否在线且空闲, 绑定分机是否在线可用
                String matchAgentId = matchAgentWeightVO.getMatchAgentId();
                if (matchAgentWeightVO.getExistMatchAgent()) {
                    if (Boolean.TRUE.equals(free)) {
                        return matchAgentWeightVO;
                    }
                    if (dataQueryService.checkAgentAndExtStatus(companyCode, matchAgentId)) {
                        return matchAgentWeightVO;
                    }
                }
            }
        }
        log.info("[查坐席权值] 空闲: {} 企业: {}, 技能: {}, 技能id匹配的坐席数: {}, 但没有可用的坐席",
                free, companyCode, skillId, matchAgentList.size());
        return matchAgentWeightVO;
    }

    private void matchAgentAction(MatchAgentWeightVO matchAgentWeightVO, List<TransferAgentQueueDTO> freeAgents) {
        List<MatchAgentWeightVO.MatchAgent> matchAgentList = matchAgentWeightVO.getMatchAgentList();
        if (CollUtil.isEmpty(matchAgentList)) {
            return;
        }
        // 根据坐席权值分组
        Map<Integer, List<MatchAgentWeightVO.MatchAgent>> weightGroup = matchAgentList.stream()
                .collect(Collectors.groupingBy(MatchAgentWeightVO.MatchAgent::getAgentWeight));
        Optional<Integer> first = weightGroup.keySet().stream().sorted().findFirst();
        if (first.isPresent()) {
            List<MatchAgentWeightVO.MatchAgent> matchAgents = weightGroup.get(first.get());
            // 只有一个, 是最大的
            if (matchAgents.size() == 1) {
                MatchAgentWeightVO.MatchAgent matchAgent = matchAgents.get(0);
                matchAgentWeightVO.setExistMatchAgent(true);
                matchAgentWeightVO.setExistMax(true);
                matchAgentWeightVO.setMatchAgentId(matchAgent.getAgentId());
                matchAgentWeightVO.setMatchPhoneNumber(matchAgent.getPhoneNumber());
                matchAgentWeightVO.getMatchAgentList().remove(matchAgent);
                removeNotAvailable(freeAgents, matchAgent.getAgentId());
                return;
            }
            // 存在多个坐席权值一样的坐席. 根据进入空闲队列的时间戳, 找最早的
            Optional<MatchAgentWeightVO.MatchAgent> min = matchAgents.stream()
                    .min(Comparator.comparingDouble(MatchAgentWeightVO.MatchAgent::getFreeTimestamp));
            if (min.isPresent()) {
                MatchAgentWeightVO.MatchAgent matchAgent = min.get();
                matchAgentWeightVO.setExistMatchAgent(true);
                matchAgentWeightVO.setExistMax(true);
                matchAgentWeightVO.setMatchAgentId(min.get().getAgentId());
                matchAgentWeightVO.setMatchPhoneNumber(min.get().getPhoneNumber());
                matchAgentWeightVO.getMatchAgentList().remove(matchAgent);
                removeNotAvailable(freeAgents, matchAgent.getAgentId());
            }
        }
    }

    private void removeNotAvailable(List<TransferAgentQueueDTO> freeAgents, String agentId) {
        Iterator<TransferAgentQueueDTO> iterator = freeAgents.iterator();
        while (iterator.hasNext()) {
            if (agentId.equals(iterator.next().getAgentId())) {
                iterator.remove();
                return;
            }
        }
    }

    @Override
    public FreeswitchApiVO callBridgeAgent(UserQueueUpDTO userQueueUpDTO, String matchAgentId, String phoneNumber)
            throws Exception {
        if (log.isInfoEnabled()) {
            String data = objectMapper.writeValueAsString(userQueueUpDTO);
            log.info("[呼入-转技能-callBridge] 坐席: {}, 客户排队信息: {}", matchAgentId, data);
        }
        String companyCode = userQueueUpDTO.getCompanyCode();
        String callerNumber = userQueueUpDTO.getCallerNumber();
        String uuid = userQueueUpDTO.getUuid();

        // 查询下客户是否挂断
        Boolean isHangup = commonDataOperateService.isHangup(companyCode, uuid);
        if (Boolean.TRUE.equals(isHangup)) {
            boolean removed = removeQueueUp(userQueueUpDTO);
            log.info("[呼入-转技能-callBridge] 企业: {}, 来电: {}, 已挂断, 移除排队: {}", companyCode, callerNumber, removed);
            return FreeswitchApiVO.fail("来电已经挂断", FreeswitchResultCode.API_ERROR_UUID);
        }

        // 离线坐席手机号码
        if (StrUtil.isNotEmpty(phoneNumber)) {
            FreeswitchApiVO freeswitchApiVO = callBridgeToAgent(userQueueUpDTO, matchAgentId, phoneNumber);
            if (log.isInfoEnabled()) {
                log.info("[呼入-转技能-callBridge] 企业id: {}, 来电号码: {}, callBridge结果: {}",
                        companyCode, callerNumber, objectMapper.writeValueAsString(freeswitchApiVO));
            }
            checkFreeswitchResult(userQueueUpDTO, freeswitchApiVO);
            if (freeswitchApiVO.getResult()) {
                deleteOfflineAgentQueue(companyCode, matchAgentId);
                log.info("[呼入-转技能-callBridge] 企业id: {}, 来电uuid: {}, 来电号码: {}, 移除坐席离线队列",
                        companyCode, uuid, callerNumber);
            }
            return freeswitchApiVO;
        }

        // TODO lock originate
        Boolean setNx = lockOriginate(companyCode, matchAgentId, uuid);
        if (!Boolean.TRUE.equals(setNx)) {
            log.info("[呼入-转技能-callBridge] 企业id: {}, 来电号码: {}, 坐席: {}, 已经被呼叫, 未挂断.",
                    companyCode, callerNumber, matchAgentId);
            return FreeswitchApiVO.fail(StrFormatter.format("坐席: {}, 已经被呼叫, 未挂断, 请稍候再试", matchAgentId));
        }
        
        // check agent status
        Optional<AgentStatusDTO> agentStatusOptional = commonDataOperateService.getActualAgentStatus(companyCode, matchAgentId);
        if (!agentStatusOptional.isPresent()) {
            return FreeswitchApiVO.error(StrFormatter.format("坐席: {}, 未签入, 呼叫失败!", matchAgentId));
        }
        AgentStatusDTO agentStatusDTO = agentStatusOptional.get();
        if (!AgentStatusEnum.FREE.name().equals(agentStatusDTO.getTargetStatus())) {
            return FreeswitchApiVO.error(StrFormatter.format("坐席: {}, 未处于空闲状态, 呼叫失败!", matchAgentId));
        }

        String extId = commonDataOperateService.getExtIdRelateAgentId(companyCode, matchAgentId);
        // 分配到坐席，呼叫前 调用call_queue_to_agent
        CallQueueToAgentDTO callQueueToAgentDTO = CallQueueToAgentDTO.build(IdUtil.fastUUID(), companyCode, uuid, extId);
        FreeswitchApiVO apiVO = freeswitchRequestService.callQueueToAgent(callQueueToAgentDTO);
        checkFreeswitchResult(userQueueUpDTO, apiVO);
        if (Objects.nonNull(apiVO) && apiVO.getResult()) {
            // 调用话务控制接口
            FreeswitchApiVO freeswitchApiVO = callBridgeToAgent(userQueueUpDTO, matchAgentId, "");
            if (log.isInfoEnabled()) {
                log.info("[呼入-转技能-callBridge] 企业id: {}, 来电uuid: {}, 来电号码: {}, callBridge结果: {}",
                        companyCode, uuid, callerNumber, objectMapper.writeValueAsString(freeswitchApiVO));
            }
            if (Objects.nonNull(freeswitchApiVO) && freeswitchApiVO.getResult()) {
                Boolean delete = deleteFreeAgentQueue(companyCode, matchAgentId);
                log.info("[呼入-转技能-callBridge] 企业id: {}, 来电uuid: {}, 来电号码: {}, 移除坐席空闲队列: {}",
                        companyCode, uuid, callerNumber, delete);
            }
            return freeswitchApiVO;
        }
        return apiVO;
    }

    private void checkFreeswitchResult(UserQueueUpDTO userQueueUpDTO, FreeswitchApiVO apiVO) {
        // 操作失败,未找到uuid所在的acd信息, 这个错 说明来电已挂断, 把客户移除排队
        if (Objects.nonNull(apiVO) && FreeswitchUtil.isInvalidUuid(apiVO.getMsg())) {
            String companyCode = userQueueUpDTO.getCompanyCode();
            String callerNumber = userQueueUpDTO.getCallerNumber();
            boolean removed = removeQueueUp(userQueueUpDTO);
            log.info("[呼入-转技能-callBridge] 企业: {}, 来电: {}, 已挂断, 移除排队: {}", companyCode, callerNumber, removed);
        }
    }

    private FreeswitchApiVO callBridgeToAgent(UserQueueUpDTO userQueueUpDTO, String matchAgentId, String phoneNumber)
            throws Exception {
        String companyCode = userQueueUpDTO.getCompanyCode();
        // 坐席可能没接, 在坐席挂断事件判断若没有桥接标志, 则坐席重新进入排队队列
        // uuid上下文-在callin事件设置
        String uuid = userQueueUpDTO.getUuid();
        CallUuidContext clientCallUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
        if (Objects.isNull(clientCallUuidContext)) {
            return FreeswitchApiVO.response(userQueueUpDTO.getUuid(), "uuid未查询到通话", uuid, false);
        }
        String oriUuid = "callin-" + IdUtil.fastUUID();
        int outline = OutLineEnum.IN_LINE.getCode();
        // 客户来电号码
        String callerNumber = userQueueUpDTO.getCallerNumber();
        String displayNumber = callerNumber;
        String calleeNumber;
        // 坐席离线手机号-坐席主显号
        if (StrUtil.isNotEmpty(phoneNumber)) {
            outline = OutLineEnum.OUT_LINE.getCode();
            calleeNumber = phoneNumber;
            displayNumber = commonDataOperateService.getAgentDisplayNumber(userQueueUpDTO.getCompanyCode(), matchAgentId);
        } else {
            calleeNumber = commonDataOperateService.getExtIdRelateAgentId(companyCode, matchAgentId);
        }

        log.info("[客户呼入分配坐席callBridge] 来电: {}, 分配坐席: {}, 手机号: {}", callerNumber, matchAgentId, phoneNumber);
        // 外呼并桥接参数
        CallBridgeDTO callBridgeDTO = new CallBridgeDTO();
        callBridgeDTO.setServerId(clientCallUuidContext.getCurrent().getServerId());
        callBridgeDTO.setSUuid(clientCallUuidContext.getCurrent().getUuid());
        callBridgeDTO.setOriUuid(oriUuid);
        callBridgeDTO.setCompanyCode(clientCallUuidContext.getCurrent().getCompanyCode());
        callBridgeDTO.setReqId(IdUtil.fastUUID());
        callBridgeDTO.setCallerNumber(callerNumber);
        // 呼叫坐席, 显示客户的号码
        callBridgeDTO.setDisplayNumber(displayNumber);
        callBridgeDTO.setCalleeNumber(calleeNumber);
        callBridgeDTO.setOutLine(outline);
        callBridgeDTO.setAudio(userQueueUpDTO.getAudio());
        callBridgeDTO.setVideo(userQueueUpDTO.getVideo());
        config(callBridgeDTO);
        // 保存坐席uuid信息
        if (OutLineEnum.IN_LINE.getCode().equals(outline)) {
            writeAgentCallUuidContext(clientCallUuidContext, userQueueUpDTO, callBridgeDTO, matchAgentId);
        } else {
            writeOfflinePhoneCallUuidContext(clientCallUuidContext, userQueueUpDTO, callBridgeDTO, matchAgentId);
        }
        commonDataOperateService.saveCdrLink(companyCode, clientCallUuidContext.getMainCallId(),
                clientCallUuidContext.getUUID(), oriUuid);
        return freeswitchRequestService.callBridge(callBridgeDTO);
    }

    private void config(CallBridgeDTO callBridgeDTO) {
        CompanyInfo companyInfo = commonDataOperateService.getCompanyInfoDTO(callBridgeDTO.getCompanyCode());
        if (Objects.nonNull(companyInfo)) {
            callBridgeDTO.setMaxRingTime(companyInfo.getCallInRingTimeout());
        }
    }

    private void writeAgentCallUuidContext(CallUuidContext clientCallUuidContext,
                                           UserQueueUpDTO userQueueUpDTO,
                                           CallBridgeDTO callBridgeDTO,
                                           String matchAgentId) {
        // 保存通话uuid之间关联关系, 这步必须在通话事件来之前设置!
        CallUuidRelationDTO agentUuidRelationDTO = new CallUuidRelationDTO();
        // 主话单id-自定义
        agentUuidRelationDTO.setMainCallId(clientCallUuidContext.getMainCallId());
        agentUuidRelationDTO.setMainCdrFlag(false);
        agentUuidRelationDTO.setRelationUuid(callBridgeDTO.getSUuid());
        agentUuidRelationDTO.setUuid(callBridgeDTO.getOriUuid());
        // 呼入属性
        agentUuidRelationDTO.setAudio(userQueueUpDTO.getAudio());
        agentUuidRelationDTO.setVideo(userQueueUpDTO.getVideo());
        agentUuidRelationDTO.setCallInChannel(CallInChannelEnum.CALL_IN);
        agentUuidRelationDTO.setCallInFlag(true);
        agentUuidRelationDTO.setServerId(clientCallUuidContext.getServerId());
        // 呼入客户uuid
        agentUuidRelationDTO.setMainUuid(callBridgeDTO.getSUuid());
        agentUuidRelationDTO.setNumber(callBridgeDTO.getCalleeNumber());
        agentUuidRelationDTO.setCallRoleEnum(CallRoleEnum.CALLIN_TRANSFER_AGENT);
        agentUuidRelationDTO.setCallTypeEnum(CallTypeEnum.AGENT);
        agentUuidRelationDTO.setReqId(callBridgeDTO.getReqId());
        agentUuidRelationDTO.setCallerNumber(clientCallUuidContext.getCallerNumber());
        agentUuidRelationDTO.setDisplayNumber(clientCallUuidContext.getDisplayNumber());
        // 坐席分机
        agentUuidRelationDTO.setCalleeNumber(clientCallUuidContext.getCalleeNumber());
        agentUuidRelationDTO.setRecordNode(RecordNodeEnum.CALL_IN_B);
        String companyCode = clientCallUuidContext.getCompanyCode();
        Optional<AgentStatusDTO> statusOptional = commonDataOperateService.getActualAgentStatus(companyCode, matchAgentId);
        if (statusOptional.isPresent()) {
            AgentStatusDTO agentStatusDTO = statusOptional.get();
            agentUuidRelationDTO.setExtId(agentStatusDTO.getExtId());
            agentUuidRelationDTO.setOs(agentStatusDTO.getOs());
        }
        agentUuidRelationDTO.setAgentId(matchAgentId);
        agentUuidRelationDTO.setCompanyCode(clientCallUuidContext.getCompanyCode());
        agentUuidRelationDTO.setCallDirectionEnum(CallDirectionEnum.INBOUND);
        // 外呼请求参数
        agentUuidRelationDTO.setCallCdrDTO(new CallCdrDTO());
        CallUuidContext agentCallUuidContext = CallUuidContext.builder()
                .current(agentUuidRelationDTO)
                .userQueueUpDTO(userQueueUpDTO)
                .checkAgentBridgeStatus(true)
                .relationUuid(Sets.newHashSet(callBridgeDTO.getSUuid()))
                .build();
        agentCallUuidContext.setClientUUID(callBridgeDTO.getSUuid());
        commonDataOperateService.saveCallUuidContext(agentCallUuidContext);

        // 来电uuid
        clientCallUuidContext.setUserQueueUpDTO(userQueueUpDTO);
        clientCallUuidContext.getRelationUuid().add(callBridgeDTO.getOriUuid());
        clientCallUuidContext.getCurrent().setRelationUuid(callBridgeDTO.getOriUuid());
        clientCallUuidContext.getCurrent().setCalleeNumber(callBridgeDTO.getCalleeNumber());
        clientCallUuidContext.getCurrent().setCallinTransferAgentId(matchAgentId);
        clientCallUuidContext.getUserQueueUpDTO().setSuccessTimestamp(System.currentTimeMillis());
        clientCallUuidContext.fillRelateUuidDtoByCallBridge(callBridgeDTO.getOriUuid());
        commonDataOperateService.saveCallUuidContext(clientCallUuidContext);
    }

    private void writeOfflinePhoneCallUuidContext(CallUuidContext clientCallUuidContext,
                                                  UserQueueUpDTO userQueueUpDTO,
                                                  CallBridgeDTO callBridgeDTO,
                                                  String matchAgentId) {
        long currentTimeMillis = System.currentTimeMillis();
        // 保存通话uuid之间关联关系, 这步必须在通话事件来之前设置!
        CallUuidRelationDTO uuidRelationDTO = new CallUuidRelationDTO();
        // 主话单id-自定义
        uuidRelationDTO.setMainCallId(clientCallUuidContext.getMainCallId());
        uuidRelationDTO.setMainCdrFlag(false);
        uuidRelationDTO.setRelationUuid(callBridgeDTO.getSUuid());
        uuidRelationDTO.setUuid(callBridgeDTO.getOriUuid());
        // 呼入属性
        uuidRelationDTO.setAudio(userQueueUpDTO.getAudio());
        uuidRelationDTO.setVideo(userQueueUpDTO.getVideo());
        uuidRelationDTO.setCallInChannel(CallInChannelEnum.CALL_IN);
        uuidRelationDTO.setCallinTransferAgentId(matchAgentId);
        uuidRelationDTO.setHangupAll(true);
        // 呼入客户uuid
        uuidRelationDTO.setMainUuid(callBridgeDTO.getSUuid());
        uuidRelationDTO.setNumber(callBridgeDTO.getCalleeNumber());
        uuidRelationDTO.setCallRoleEnum(CallRoleEnum.CALLIN_TRANSFER_OFFLINE_AGENT_PHONE);
        uuidRelationDTO.setCallTypeEnum(CallTypeEnum.CLIENT);
        uuidRelationDTO.setReqId(callBridgeDTO.getReqId());
        uuidRelationDTO.setCallerNumber(callBridgeDTO.getCallerNumber());
        uuidRelationDTO.setDisplayNumber(callBridgeDTO.getDisplayNumber());
        // 坐席手机号
        uuidRelationDTO.setCalleeNumber(callBridgeDTO.getCalleeNumber());
        uuidRelationDTO.setCompanyCode(clientCallUuidContext.getCompanyCode());
        uuidRelationDTO.setCallDirectionEnum(CallDirectionEnum.INBOUND);
        // 外呼请求参数
        uuidRelationDTO.setCallCdrDTO(new CallCdrDTO());
        uuidRelationDTO.setRecordNode(RecordNodeEnum.CALL_IN_B);
        CallUuidContext agentCallUuidContext = CallUuidContext.builder()
                .current(uuidRelationDTO)
                .userQueueUpDTO(userQueueUpDTO)
                .relationUuid(Sets.newHashSet(callBridgeDTO.getSUuid()))
                .build();
        agentCallUuidContext.setClientUUID(callBridgeDTO.getSUuid());
        commonDataOperateService.saveCallUuidContext(agentCallUuidContext);

        // 来电uuid
        clientCallUuidContext.setUserQueueUpDTO(userQueueUpDTO);
        clientCallUuidContext.getRelationUuid().add(callBridgeDTO.getOriUuid());
        clientCallUuidContext.getCurrent().setRelationUuid(callBridgeDTO.getOriUuid());
        clientCallUuidContext.getCurrent().setCalleeNumber(callBridgeDTO.getCalleeNumber());
        clientCallUuidContext.getCurrent().setCallinTransferAgentId(matchAgentId);
        clientCallUuidContext.getUserQueueUpDTO().setSuccessTimestamp(currentTimeMillis);
        clientCallUuidContext.fillRelateUuidDtoByCallBridge(callBridgeDTO.getOriUuid());
        commonDataOperateService.saveCallUuidContext(clientCallUuidContext);
    }

    @Override
    public Boolean unlockOriginate(String companyCode, String number) {
        String originateLockKey = CacheUtil.getOriginateLockKey(companyCode + StrUtil.COLON + number);
        return redissonUtil.delKey(originateLockKey);
    }

    @Override
    public Boolean lockOriginate(String companyCode, String number, String uuid) {
        String originateLockKey = CacheUtil.getOriginateLockKey(companyCode + StrUtil.COLON + number);
        return redissonUtil.setNx(originateLockKey, uuid, Duration.ofSeconds(10));
    }

}
