package com.cqt.call.event.cdr;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.contants.RocketMqConstant;
import com.cqt.base.util.CacheUtil;
import com.cqt.call.service.DataQueryService;
import com.cqt.call.service.DataStoreService;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.cdr.dto.CdrGenerateDTO;
import com.cqt.model.cdr.dto.CdrMessageDTO;
import com.cqt.model.cdr.entity.CallCenterMainCdr;
import com.cqt.model.cdr.entity.CallCenterSubCdr;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * date:  2023-07-13 19:14
 * 在挂断事件, 话单生成
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CdrGenerateEventListener {

    private final RocketMQTemplate rocketMQTemplate;

    private final DataQueryService dataQueryService;

    private final DataStoreService dataStoreService;

    private final ObjectMapper objectMapper;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    private final RedissonClient redissonClient;

    private final CommonDataOperateService commonDataOperateService;

    /**
     * 监听
     */
    // @Async(CommonThreadPoolConfig.SAVE_POOL_NAME)
    @EventListener(classes = {CdrGenerateEvent.class})
    public void listener(CdrGenerateEvent event) {
        CdrGenerateDTO cdrGenerateDTO = event.getCdrGenerateDTO();
        // lock
        CallUuidContext callUuidContext = cdrGenerateDTO.getCallUuidContext();
        String companyCode = callUuidContext.getCompanyCode();
        String mainCallId = callUuidContext.getMainCallId();
        log.info("[话单生成] 企业: {}, 主通话id: {}", companyCode, mainCallId);
        // 查询是否已经生成
        if (isAlreadyGenerate(companyCode, mainCallId)) {
            return;
        }
        String cdrGenerateLockKey = CacheUtil.getCdrGenerateLockKey(companyCode, mainCallId);
        RLock fairLock = redissonClient.getFairLock(cdrGenerateLockKey);
        fairLock.lock(cloudCallCenterProperties.getLockTIme().toMillis(), TimeUnit.MILLISECONDS);
        try {
            if (isAlreadyGenerate(companyCode, mainCallId)) {
                return;
            }
            startGenerate(cdrGenerateDTO);
        } catch (Exception e) {
            log.error("[话单生成] companyCode: {}, mainCallId: {}, 异常: ", companyCode, mainCallId, e);
        } finally {
            try {
                fairLock.unlock();
            } catch (Exception e) {
                log.error("[话单生成] lock.unlock error: ", e);
            }
        }
    }

    private boolean isAlreadyGenerate(String companyCode, String mainCallId) {
        Boolean cdrGenerated = dataQueryService.isCdrGenerated(companyCode, mainCallId);
        if (Boolean.TRUE.equals(cdrGenerated)) {
            log.info("[话单生成] 企业: {}, 主通话id: {}, 此通话话单已经生成", companyCode, mainCallId);
            return true;
        }
        return false;
    }

    private void startGenerate(CdrGenerateDTO generateDTO) {
        final CallStatusEventDTO callStatusEventDTO = generateDTO.getCallStatusEventDTO();
        final CallUuidContext callUuidContext = generateDTO.getCallUuidContext();
        String companyCode = callUuidContext.getCompanyCode();
        String mainCallId = callUuidContext.getMainCallId();
        String platformNumber = commonDataOperateService.getPlatformNumber(companyCode, mainCallId);

        Set<String> cdrLink = dataQueryService.getCdrLink(companyCode, mainCallId);
        if (CollUtil.isEmpty(cdrLink)) {
            // 是否为留言
            // 若呼入ivr没有分配坐席 也要生成一条子话单
            if (Boolean.TRUE.equals(callUuidContext.getCallinIVR())) {
                //  呼入转IVR, 没有分配坐席
                List<CallCenterSubCdr> subCdrList = createSubCdr(callUuidContext, callStatusEventDTO, platformNumber);
                CdrMessageDTO cdrMessageDTO = new CdrMessageDTO();
                cdrMessageDTO.setSubCdr(subCdrList);
                cdrMessageDTO.setCallInIvrNoAgent(true);
                // 是否为留言
                cdrMessageDTO.setVoiceMailFlag(callUuidContext.getVoiceMailFlag());
                cdrMessageDTO.setServiceId(callStatusEventDTO.getServerId());
                // 主话单
                CallCenterMainCdr callCenterMainCdr = generateMainCdr(callStatusEventDTO, callUuidContext, platformNumber);
                cdrMessageDTO.setMainCdr(callCenterMainCdr);
                cdrMessageDTO.setChannelData(callStatusEventDTO.getChannelData());
                // 发送mq
                sendMessage(cdrMessageDTO, mainCallId, companyCode);
                return;
            }
            return;
        }
        Map<String, CallUuidContext> contextMap = getContextMap(companyCode, cdrLink);
        boolean checkAllHangup = checkAllHangup(contextMap);
        // 还有通话未挂断
        if (!checkAllHangup) {
            saveChannelData(callUuidContext, callStatusEventDTO);
            return;
        }
        CdrMessageDTO cdrMessageDTO = generateCdr(contextMap, callStatusEventDTO, callUuidContext, cdrLink, platformNumber);
        // 发送mq
        sendMessage(cdrMessageDTO, mainCallId, companyCode);
    }

    /**
     * 生成话单对象
     *
     * @param contextMap         uuid上下文map
     * @param callStatusEventDTO 通话状态-挂断
     * @param callUuidContext    上去uuid上下文
     */
    private CdrMessageDTO generateCdr(Map<String, CallUuidContext> contextMap,
                                      CallStatusEventDTO callStatusEventDTO,
                                      CallUuidContext callUuidContext,
                                      Set<String> cdrLink,
                                      String platformNumber) {
        CdrMessageDTO cdrMessageDTO = new CdrMessageDTO();
        cdrMessageDTO.setVoiceMailFlag(callUuidContext.getVoiceMailFlag());
        cdrMessageDTO.setServiceId(callStatusEventDTO.getServerId());
        // 主话单
        CallUuidContext mainContext = getMainContext(contextMap, callUuidContext);
        CallCenterMainCdr callCenterMainCdr = generateMainCdr(callStatusEventDTO, mainContext, platformNumber);
        cdrMessageDTO.setMainCdr(callCenterMainCdr);

        // 通道变量
        Map<String, Object> channelDataMap = getChannelData(contextMap, callStatusEventDTO, callUuidContext);
        cdrMessageDTO.setChannelData(channelDataMap);

        // 子话单
        cdrMessageDTO.setSubCdr(generateSubCdrList(contextMap, callStatusEventDTO, cdrLink, platformNumber));
        return cdrMessageDTO;
    }

    /**
     * 生成子话单
     *
     * @param contextMap         uuid上下文map
     * @param callStatusEventDTO 挂断事件消息
     * @param cdrLink            话单关联uuid set
     * @return 子话单list
     */
    private List<CallCenterSubCdr> generateSubCdrList(Map<String, CallUuidContext> contextMap,
                                                      CallStatusEventDTO callStatusEventDTO,
                                                      Set<String> cdrLink,
                                                      String platformNumber) {
        List<CallCenterSubCdr> subCdrList = new ArrayList<>();
        for (String uuids : cdrLink) {
            List<String> stringList = StrUtil.split(uuids, StrUtil.COLON);
            String sourceUUID = stringList.get(0);
            String destUUID = stringList.get(1);
            CallCenterSubCdr subCdr = createSubCdr(contextMap.get(sourceUUID), contextMap.get(destUUID),
                    callStatusEventDTO, platformNumber);
            subCdrList.add(subCdr);
        }
        return subCdrList;
    }

    /**
     * 组装子话单
     *
     * @param sourceContext 主叫uuid上下文
     * @param destContext   被叫uuid上下文
     * @param callStatusEventDTO 挂断事件
     * @return 子话单
     */
    private CallCenterSubCdr createSubCdr(CallUuidContext sourceContext,
                                          CallUuidContext destContext,
                                          CallStatusEventDTO callStatusEventDTO,
                                          String platformNumber) {
        CallCenterSubCdr subCdr = new CallCenterSubCdr();
        destContext.getCallCdrDTO().setRecordFileName(getRecordPath(destContext.getCompanyCode(), destContext.getUUID()));
        subCdr.convert(sourceContext, destContext, callStatusEventDTO, platformNumber);
        return subCdr;
    }

    /**
     * ivr呼入 未转接坐席挂断  子话单生成
     *
     * @param callUuidContext    通话uuid
     * @param callStatusEventDTO 事件
     * @param platformNumber       计费号码
     * @return 子话单
     */
    private List<CallCenterSubCdr> createSubCdr(CallUuidContext callUuidContext,
                                                CallStatusEventDTO callStatusEventDTO,
                                                String platformNumber) {
        List<CallCenterSubCdr> subCdrList = new ArrayList<>();
        CallCenterSubCdr subCdr = new CallCenterSubCdr();
        callUuidContext.getCallCdrDTO().setRecordFileName(getRecordPath(callUuidContext.getCompanyCode(), callUuidContext.getUUID()));
        subCdr.convertCaller(callUuidContext, callStatusEventDTO, platformNumber);
        subCdrList.add(subCdr);
        return subCdrList;
    }

    /**
     * 获取通道变量
     */
    private Map<String, Object> getChannelData(Map<String, CallUuidContext> contextMap,
                                               CallStatusEventDTO callStatusEventDTO,
                                               CallUuidContext callUuidContext) {
        // 当前是uuid有ivr
        if (isIVR(callUuidContext)) {
            return callStatusEventDTO.getChannelData();
        }

        for (Map.Entry<String, CallUuidContext> entry : contextMap.entrySet()) {
            CallUuidContext context = entry.getValue();
            if (isIVR(context)) {
                return dataQueryService.getCdrChannelData(context.getCurrent().getCompanyCode(), context.getCurrent().getUuid());
            }
        }
        return null;
    }

    /**
     * 生成主话单
     */
    private CallCenterMainCdr generateMainCdr(CallStatusEventDTO callStatusEventDTO,
                                              CallUuidContext mainContext,
                                              String platformNumber) {
        CallCenterMainCdr callCenterMainCdr = new CallCenterMainCdr();
        mainContext.getCallCdrDTO().setRecordFileName(getRecordPath(mainContext.getCompanyCode(), mainContext.getUUID()));
        callCenterMainCdr.convert(mainContext, callStatusEventDTO, platformNumber);
        return callCenterMainCdr;
    }

    private String getRecordPath(String companyCode, String uuid) {
        return commonDataOperateService.getRecordFile(companyCode, uuid);
    }

    /**
     * 获取主话单 uuid上下文
     */
    public CallUuidContext getMainContext(Map<String, CallUuidContext> contextMap,
                                          CallUuidContext callUuidContext) {
        if (Boolean.TRUE.equals(callUuidContext.getCurrent().getMainCdrFlag())) {
            return callUuidContext;
        }
        for (Map.Entry<String, CallUuidContext> entry : contextMap.entrySet()) {
            CallUuidContext context = entry.getValue();
            if (Boolean.TRUE.equals(context.getCurrent().getMainCdrFlag())) {
                return context;
            }
        }
        return null;
    }

    /**
     * 检查是否全部挂断
     */
    private boolean checkAllHangup(Map<String, CallUuidContext> contextMap) {
        boolean allHangup = true;
        for (Map.Entry<String, CallUuidContext> entry : contextMap.entrySet()) {
            CallUuidContext context = entry.getValue();
            CallCdrDTO callCdrDTO = context.getCurrent().getCallCdrDTO();
            if (!Boolean.TRUE.equals(callCdrDTO.getHangupFlag())) {
                allHangup = false;
                break;
            }
        }
        return allHangup;
    }

    /**
     * 获取uuid和上下文map
     */
    private Map<String, CallUuidContext> getContextMap(String companyCode, Set<String> cdrLink) {
        HashSet<String> uuidSet = new HashSet<>();
        for (String uuids : cdrLink) {
            List<String> stringList = StrUtil.split(uuids, StrUtil.COLON);
            uuidSet.add(stringList.get(0));
            uuidSet.add(stringList.get(1));
        }
        Map<String, CallUuidContext> contextMap = new HashMap<>();
        for (String uuid : uuidSet) {
            CallUuidContext callUuidContext = dataQueryService.getCallUuidContext(companyCode, uuid);
            if (Objects.nonNull(callUuidContext)) {
                contextMap.put(uuid, callUuidContext);
            }
        }
        return contextMap;
    }

    /**
     * 暂时保存通道变量
     */
    private void saveChannelData(CallUuidContext callUuidContext, CallStatusEventDTO callStatusEventDTO) {
        try {
            // 是客户侧有ivr和满意度
            if (isIVR(callUuidContext)) {
                String companyCode = callUuidContext.getCompanyCode();
                String uuid = callUuidContext.getUUID();
                Map<String, Object> channelDataMap = callStatusEventDTO.getChannelData();
                String channelDataJson = objectMapper.writeValueAsString(channelDataMap);
                dataStoreService.saveCdrChannelData(companyCode, uuid, channelDataJson);
            }
        } catch (Exception e) {
            log.error("[话单生成] redis保存通道变量异常", e);
        }
    }

    /**
     * 当前是否有ivr
     */
    private boolean isIVR(CallUuidContext callUuidContext) {
        Boolean callinIVR = callUuidContext.getCallinIVR();
        Boolean transIVR = callUuidContext.getTransIVR();
        Boolean satisfaction = callUuidContext.getSatisfaction();
        return Boolean.TRUE.equals(callinIVR) || Boolean.TRUE.equals(transIVR) || Boolean.TRUE.equals(satisfaction);
    }

    /**
     * mq消息发送
     */
    private void sendMessage(CdrMessageDTO cdrMessageDTO, String mainCallId, String companyCode) {
        try {
            Message<CdrMessageDTO> message = MessageBuilder.withPayload(cdrMessageDTO)
                    .setHeader(MessageConst.PROPERTY_KEYS, mainCallId)
                    .build();
            String destination = RocketMqConstant.CDR_STORE_TOPIC + StrUtil.COLON + companyCode;
            SendResult sendResult = rocketMQTemplate.syncSend(destination, message);
            log.info("[话单入库消息] companyCode: {}, mainCallId: {} 发送mq结果: {}", companyCode, mainCallId, sendResult.getSendStatus());
            if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                // 保存生成标志
                dataStoreService.saveCdrGenerateFlag(companyCode, mainCallId);
            }
        } catch (Exception e) {
            // TODO 发送失败
            log.error("[话单入库消息] companyCode: {}, mainCallId: {} 发送mq异常", companyCode, mainCallId, e);
        }
    }

}
