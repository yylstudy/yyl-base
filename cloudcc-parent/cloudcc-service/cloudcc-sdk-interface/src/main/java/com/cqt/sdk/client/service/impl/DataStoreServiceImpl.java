package com.cqt.sdk.client.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.base.enums.SdkErrCode;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.base.enums.ext.ExtCallStatusEnum;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.base.enums.ext.ExtStatusTransferActionEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.service.AgentInfoService;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.mapper.AgentInfoMapper;
import com.cqt.model.agent.dto.AgentCheckInRecord;
import com.cqt.model.agent.dto.AgentInfoEditDTO;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientCheckinDTO;
import com.cqt.model.client.dto.ClientCheckoutDTO;
import com.cqt.model.client.vo.ClientAgentStatusChangeVO;
import com.cqt.model.client.vo.ClientKickOffVO;
import com.cqt.model.client.vo.ClientRequestVO;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.freeswitch.dto.api.DisExtensionRegAddrDTO;
import com.cqt.model.freeswitch.vo.DisExtensionRegAddrVO;
import com.cqt.model.freeswitch.vo.GetExtensionRegVO;
import com.cqt.model.queue.dto.AgentWeightInfoDTO;
import com.cqt.rpc.client.ClientServerRemoteService;
import com.cqt.sdk.client.service.DataQueryService;
import com.cqt.sdk.client.service.DataStoreService;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

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

    private final RedissonUtil redissonUtil;

    private final ObjectMapper objectMapper;

    private final AgentInfoMapper agentInfoMapper;

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    private final AgentInfoService agentInfoService;

    private final DataQueryService dataQueryService;

    @DubboReference
    private ClientServerRemoteService clientServerRemoteService;

    @Override
    public void updateActualExtStatus(ExtStatusDTO extStatusDTO) {

        commonDataOperateService.updateActualExtStatus(extStatusDTO);
    }

    @Override
    public void updateActualAgentStatus(AgentStatusDTO agentStatusDTO) {
        // if (!dataQueryService.isDealAgentStatus(agentStatusDTO.getTargetStatus(), agentStatusDTO.getSourceStatus())) {
        //     return;
        // }
        agentInfoService.updateActualAgentStatus(agentStatusDTO);
    }

    @Override
    public void deleteActualAgentStatus(String companyCode, String agentId) {
        String agentStatusKey = CacheUtil.getAgentStatusKey(companyCode, agentId);
        redissonUtil.delKey(agentStatusKey);
        String allAgentStatusKey = CacheUtil.getAllAgentStatusKey(companyCode);
        redissonUtil.removeHashItem(allAgentStatusKey, agentId);
    }

    @Override
    public void deleteAgentQueue(String companyCode, String agentId, AgentServiceModeEnum serviceMode, boolean free) {
        AgentWeightInfoDTO agentWeightInfoDTO = commonDataOperateService.getAgentWithWeightInfo(companyCode, agentId);
        Optional<Set<String>> skillIdSetOptional = commonDataOperateService.getSkillIdFromAgentWeight(agentWeightInfoDTO);
        if (skillIdSetOptional.isPresent()) {
            Set<String> skillIdSet = skillIdSetOptional.get();
            for (String skillId : skillIdSet) {
                String skillAgentQueueKey = CacheUtil.getSkillAgentQueueKey(companyCode, skillId, serviceMode, free);
                redissonUtil.removeZsetObject(skillAgentQueueKey, agentId);
                log.info("[技能-坐席队列-删除] 空闲: {}, 企业id: {}, 坐席id: {}, key: {}",
                        free, companyCode, agentId, skillAgentQueueKey);
            }
        }

        String companyAgentQueueKey = CacheUtil.getCompanyAgentQueueKey(companyCode, serviceMode, free);
        redissonUtil.removeZsetObject(companyAgentQueueKey, agentId);
        log.info("[企业-坐席队列-删除] 是否空闲: {}, 企业id: {}, 坐席id: {}, key: {},", free, companyCode, agentId, companyAgentQueueKey);
    }

    @Override
    public void addAgentQueue(String companyCode, String agentId, AgentServiceModeEnum serviceMode, boolean free,
                              Long timestamp, String phoneNumber) {
        String companyAgentQueueKey = CacheUtil.getCompanyAgentQueueKey(companyCode, serviceMode, free);
        AgentWeightInfoDTO agentWeightInfoDTO = commonDataOperateService.getAgentWithWeightInfo(companyCode, agentId);
        // change zset  ts agentId
        boolean set = redissonUtil.addZset(companyAgentQueueKey, agentId, timestamp);
        log.info("[企业离线坐席队列-新增] 企业id: {}, 坐席id: {}, key: {}, result: {}",
                companyCode, agentId, companyAgentQueueKey, set);
        Optional<Set<String>> skillIdSetOptional = commonDataOperateService.getSkillIdFromAgentWeight(agentWeightInfoDTO);
        if (skillIdSetOptional.isPresent()) {
            Set<String> skillIdSet = skillIdSetOptional.get();
            for (String skillId : skillIdSet) {
                String skillAgentQueueKey = CacheUtil.getSkillAgentQueueKey(companyCode, skillId, serviceMode, free);
                boolean set1 = redissonUtil.addZset(skillAgentQueueKey, agentId, timestamp);
                log.info("[技能空闲坐席队列-新增] 企业id: {}, 坐席id: {}, key: {}, result: {}",
                        companyCode, agentId, skillAgentQueueKey, set1);
            }
        }
    }

    @Override
    public void notifySdkAgentStatus(ClientAgentStatusChangeVO agentStatusChangeVO) {
        try {
            if (Objects.nonNull(agentStatusChangeVO)) {
                String agentStatusJson = objectMapper.writeValueAsString(agentStatusChangeVO);
                log.info("[坐席状态变化-通知SDK] 参数: {}", agentStatusJson);
                ClientResponseBaseVO clientResponseBaseVO = clientServerRemoteService.request(agentStatusJson);
                log.info("[坐席状态变化-通知SDK] 发送成功, 结果: {}", objectMapper.writeValueAsString(clientResponseBaseVO));
            }
        } catch (JsonProcessingException e) {
            log.info("[坐席状态变化-通知SDK] 发送成功, params: {}, error: ", agentStatusChangeVO, e);
        }
    }

    @Override
    public void addCheckInRecord(ClientCheckinDTO clientCheckinDTO) {
        // 这里需要设置缓存信息
        AgentCheckInRecord currentRecord = new AgentCheckInRecord();
        currentRecord.setTs(new Date());
        currentRecord.setAgentId(clientCheckinDTO.getAgentId());
        currentRecord.setClientType(clientCheckinDTO.getOs());
        currentRecord.setClientIps(StrUtil.split(clientCheckinDTO.getAgentIp(), StrUtil.COMMA));
        String key = CacheUtil.getCheckInRecordKey(clientCheckinDTO.getAgentId());
        redissonUtil.setHash(key, clientCheckinDTO.getOs(), JSON.toJSONString(currentRecord));
        redissonUtil.setTTL(key, 24 * 60 * 60);
    }

    @Override
    public void extBindAgent(String extId, String agentId) {
        String extBindAgentKey = CacheUtil.getExtBindAgentKey(null, extId);
        String agentBindExtKey = CacheUtil.getAgentBindExtKey(null, agentId);

        String shortExtId = extId.substring(extId.indexOf("_") + 1);

        AgentInfo updateEntity = new AgentInfo();
        updateEntity.setSysAgentId(agentId);
        updateEntity.setSysExtId(extId);
        updateEntity.setExtId(shortExtId);

        agentInfoMapper.updateById(updateEntity);

        redissonUtil.set(extBindAgentKey, agentId);
        redissonUtil.set(agentBindExtKey, extId);
    }

    @Override
    public void removeCheckInRecord(ClientCheckoutDTO clientCheckoutDTO) {
        String key = CacheUtil.getCheckInRecordKey(clientCheckoutDTO.getAgentId());
        redissonUtil.removeHashItem(key, clientCheckoutDTO.getOs());
    }

    @Override
    public void agentKickoffNotify(ClientCheckinDTO checkinDTO, AgentCheckInRecord lastRecord) throws JsonProcessingException {
        ClientKickOffVO clientKickVO = new ClientKickOffVO();
        clientKickVO.setReqId(IdUtil.objectId());
        clientKickVO.setReply(true);
        clientKickVO.setCode(SdkErrCode.OK.getCode());
        clientKickVO.setOs(lastRecord.getClientType());
        clientKickVO.setMsgType(MsgTypeEnum.kick_off.name());
        clientKickVO.setCompanyCode(clientKickVO.getCompanyCode());
        if (CollUtil.isNotEmpty(lastRecord.getClientIps())) {
            clientKickVO.setAgentIp(String.join("", lastRecord.getClientIps()));
        }
        String msg = String.format("当前坐席已在其他设备签入，设备IP：" + clientKickVO.getAgentIp());
        clientKickVO.setMsg(msg);

        String clientKickoffJson = objectMapper.writeValueAsString(clientKickVO);
        log.info("[坐席互顶下线-通知SDK] 参数: {}", clientKickoffJson);
        ClientResponseBaseVO clientResponseBaseVO = clientServerRemoteService.request(clientKickoffJson);
        log.info("[坐席互顶下线-通知SDK] 发送成功, 结果: {}", objectMapper.writeValueAsString(clientResponseBaseVO));
    }

    /**
     * 请求底层分配分机注册地址
     */
    @Override
    public DisExtensionRegAddrVO disExtRegAddr(String companyCode, String extId) {
        DisExtensionRegAddrDTO disExtensionRegAddrDTO = new DisExtensionRegAddrDTO();
        disExtensionRegAddrDTO.setExtId(extId);
        disExtensionRegAddrDTO.setCompanyCode(companyCode);
        disExtensionRegAddrDTO.setReqId(IdUtil.objectId());
        return freeswitchRequestService.disExtensionRegAddr(disExtensionRegAddrDTO);
    }

    @Override
    public void compareExtStatus(ExtStatusDTO extStatusDTO, GetExtensionRegVO copyGetExtensionRegVO) {
        if (Objects.isNull(extStatusDTO)) {
            return;
        }
        String targetStatus = extStatusDTO.getTargetStatus();
        String callStatus = copyGetExtensionRegVO.getCallStatus();
        // 分机IP
        extStatusDTO.setExtIp(copyGetExtensionRegVO.getRegAddr());
        if (StrUtil.isEmpty(callStatus)) {
            return;
        }
        // 自己存的和底层查的不一致, 以底层为准
        ExtCallStatusEnum extCallStatusEnum = ExtCallStatusEnum.valueOf(callStatus);
        switch (extCallStatusEnum) {
            case ANSWER:
                if (!ExtStatusEnum.CALLING.name().equals(targetStatus)) {
                    // 修改为通话中
                    updateExtStatus(extStatusDTO, ExtStatusEnum.CALLING.name());
                }
                return;
            case IDLE:
                if (!ExtStatusEnum.ONLINE.name().equals(targetStatus)) {
                    // 修改为在线
                    updateExtStatus(extStatusDTO, ExtStatusEnum.ONLINE.name());
                }
                return;
            case RING:
                if (!ExtStatusEnum.RINGING.name().equals(targetStatus)) {
                    // 修改为振铃
                    updateExtStatus(extStatusDTO, ExtStatusEnum.RINGING.name());
                }
                return;
            default:
                updateActualExtStatus(extStatusDTO);
        }
    }

    private void updateExtStatus(ExtStatusDTO extStatusDTO, String targetStatus) {
        try {
            extStatusDTO.setSourceStatus(extStatusDTO.getTargetStatus());
            extStatusDTO.setSourceTimestamp(extStatusDTO.getSourceTimestamp());
            extStatusDTO.setTransferAction(ExtStatusTransferActionEnum.RESET.name());
            extStatusDTO.setTargetStatus(targetStatus);
            extStatusDTO.setTargetTimestamp(System.currentTimeMillis());
            updateActualExtStatus(extStatusDTO);
        } catch (Exception e) {
            log.error("[保存分机状态] 异常: ", e);
        }
    }

    @Override
    public ClientRequestVO<Void> updateAgentInfo(AgentInfoEditDTO agentInfoEditDTO) {
        try {
            AgentInfo agentInfo = agentInfoMapper.selectById(agentInfoEditDTO.getSysAgentId());
            if (Objects.isNull(agentInfo)) {
                return ClientRequestVO.response(SdkErrCode.AGENT_NOT_EXIST);
            }
            // 离线手机号是否已被使用
            String phoneNumber = agentInfoEditDTO.getPhoneNumber();
            if (StrUtil.isNotEmpty(phoneNumber)) {
                LambdaQueryWrapper<AgentInfo> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.select(AgentInfo::getSysAgentId);
                queryWrapper.eq(AgentInfo::getPhoneNumber, phoneNumber);
                List<String> agentIdList = agentInfoMapper.selectList(queryWrapper)
                        .stream()
                        .map(AgentInfo::getSysAgentId)
                        .collect(Collectors.toList());
                if (CollUtil.isNotEmpty(agentIdList)) {
                    if (!agentIdList.contains(agentInfoEditDTO.getSysAgentId())) {
                        return ClientRequestVO.response(SdkErrCode.AGENT_OFFLINE_PHONE_IN_USED);
                    }
                }
            }
            agentInfo.setAgentAnswerMode(agentInfoEditDTO.getAgentAnswerMode());
            agentInfo.setEnterKeyCallMode(agentInfoEditDTO.getEnterKeyCallMode());
            agentInfo.setExtRegMode(agentInfoEditDTO.getExtRegMode());
            agentInfo.setOfflineAgent(agentInfoEditDTO.getOfflineAgent());
            agentInfo.setPhoneNumber(agentInfoEditDTO.getPhoneNumber());
            agentInfo.setServiceMode(agentInfoEditDTO.getServiceMode());
            agentInfo.setVideoCallAnswerMode(agentInfoEditDTO.getVideoCallAnswerMode());
            agentInfo.setVideoCallTurnOffCamera(agentInfoEditDTO.getVideoCallTurnOffCamera());
            agentInfo.setVoiceCallAnswerMode(agentInfoEditDTO.getVoiceCallAnswerMode());
            agentInfoMapper.updateById(agentInfo);
            String agentInfoKey = CacheUtil.getAgentInfoKey(agentInfo.getSysAgentId());
            redissonUtil.set(agentInfoKey, objectMapper.writeValueAsString(agentInfo));
        } catch (Exception e) {
            log.error("[更新坐席信息] data: {}, 异常: ", agentInfoEditDTO, e);
            return ClientRequestVO.response(SdkErrCode.AGENT_INFO_UPDATE_FAIL);
        }
        return ClientRequestVO.response(SdkErrCode.OK);
    }
}
