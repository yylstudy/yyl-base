package com.cqt.sdk.client.strategy.client.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.base.contants.CommonConstant;
import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.base.enums.SdkErrCode;
import com.cqt.base.enums.agent.AgentServiceModeEnum;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.base.enums.ext.ExtCallModeEnum;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.base.enums.ext.ExtStatusTransferActionEnum;
import com.cqt.cloudcc.manager.cache.AgentCheckinCache;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.dto.AgentCheckInRecord;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.dto.AgentStatusTransferDTO;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.client.dto.ClientCheckinDTO;
import com.cqt.model.client.validategroup.AgentIdGroup;
import com.cqt.model.client.validategroup.OsGroup;
import com.cqt.model.client.vo.ClientAgentStatusChangeVO;
import com.cqt.model.client.vo.ClientCheckinVO;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.ext.entity.ExtInfo;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.vo.DisExtensionRegAddrVO;
import com.cqt.model.freeswitch.vo.GetExtensionRegVO;
import com.cqt.model.queue.dto.AgentCheckinCacheDTO;
import com.cqt.model.queue.dto.AgentWeightInfoDTO;
import com.cqt.sdk.client.converter.ModelConverter;
import com.cqt.sdk.client.event.agentstatus.FreeAgentQueueEvent;
import com.cqt.sdk.client.event.agentstatus.OfflineAgentQueueEvent;
import com.cqt.sdk.client.event.arrange.CallStopArrangeCancelEvent;
import com.cqt.sdk.client.event.arrange.CallStopArrangeEvent;
import com.cqt.sdk.client.event.mq.AgentStatusLogStoreEvent;
import com.cqt.sdk.client.service.DataQueryService;
import com.cqt.sdk.client.service.DataStoreService;
import com.cqt.sdk.client.strategy.client.ClientRequestStrategy;
import com.cqt.sdk.client.util.AESUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 迁入操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinClientRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final AESUtil aesUtil;

    private final ObjectMapper objectMapper;

    private final DataQueryService dataQueryService;

    private final DataStoreService dataStoreService;

    private final ApplicationContext applicationContext;

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.checkin;
    }

    @Override
    public ClientCheckinVO deal(String requestBody) throws Exception {
        ClientCheckinDTO clientCheckinDTO = convert(requestBody, ClientCheckinDTO.class, OsGroup.class, AgentIdGroup.class);
        try {
            return checkin(clientCheckinDTO);
        } catch (Exception e) {
            log.error("[签入] requestBody: {}, 异常: ", requestBody, e);
        }
        return new ClientCheckinVO(clientCheckinDTO, SdkErrCode.SYSTEM_EXCEPTION, "签入失败!");
    }

    private ClientCheckinVO checkin(ClientCheckinDTO clientCheckinDTO) throws Exception {
        String companyCode = clientCheckinDTO.getCompanyCode();
        String agentId = clientCheckinDTO.getAgentId();
        Long currentTimestamp = System.currentTimeMillis();
        // 迁入逻辑
        // 先校验sdk必填参数
        // 校验系统资源
        GetExtensionRegVO copyGetExtensionRegVO = new GetExtensionRegVO();
        ClientCheckinVO clientCheckinVO = checkInHandle(clientCheckinDTO, copyGetExtensionRegVO);
        if (!"0".equals(clientCheckinVO.getCode())) {
            return clientCheckinVO;
        }
        // 检测分机状态
        ExtStatusDTO extStatusDTO = commonDataOperateService.getActualExtStatus(companyCode, clientCheckinDTO.getExtId());
        dataStoreService.compareExtStatus(extStatusDTO, copyGetExtensionRegVO);

        // 坐席实时状态
        Optional<AgentStatusDTO> agentStatusOptional = commonDataOperateService.getActualAgentStatus(companyCode, agentId);
        AgentStatusDTO agentStatusDTO = new AgentStatusDTO();
        if (agentStatusOptional.isPresent()) {
            agentStatusDTO = agentStatusOptional.get();
            agentStatusDTO.setSourceStatus(agentStatusDTO.getTargetStatus());
            agentStatusDTO.setSourceSubStatus(agentStatusDTO.getTargetSubStatus());
            agentStatusDTO.setSourceDuration(agentStatusDTO.getTargetDuration());
            agentStatusDTO.setSourceTimestamp(agentStatusDTO.getTargetTimestamp());
        }
        agentStatusDTO.setTransferAction(AgentStatusTransferActionEnum.CHECKIN.name());
        if (Boolean.TRUE.equals(clientCheckinDTO.getReset())) {
            agentStatusDTO.setTargetStatus(AgentStatusEnum.FREE.name());
            agentStatusDTO.setNull();
        } else {
            agentStatusDTO.setTargetStatus(getTargetStatus(clientCheckinDTO));
        }
        agentStatusDTO.setAgentIp(clientCheckinDTO.getAgentIp());
        agentStatusDTO.setExtIp(copyGetExtensionRegVO.getRegAddr());
        agentStatusDTO.setTargetTimestamp(currentTimestamp);
        agentStatusDTO.setCompanyCode(companyCode);
        agentStatusDTO.setAgentId(agentId);
        agentStatusDTO.setExtId(clientCheckinVO.getExtId());
        agentStatusDTO.setOs(clientCheckinDTO.getOs());
        agentStatusDTO.setCheckinTime(currentTimestamp);
        agentStatusDTO.setServiceMode(clientCheckinVO.getServiceMode());
        dataStoreService.updateActualAgentStatus(agentStatusDTO);

        // 迁入操作完成, 发送迁入日志记录事件
        AgentStatusTransferDTO agentStatusTransferDTO = ModelConverter.INSTANCE.status2transfer(agentStatusDTO);
        applicationContext.publishEvent(new AgentStatusLogStoreEvent(this, agentStatusTransferDTO));

        // 排队队列操作
        queueOperate(clientCheckinDTO, agentStatusDTO, currentTimestamp);

        // 通知SDK状态变化
        dataStoreService.notifySdkAgentStatus(ClientAgentStatusChangeVO.build(agentStatusDTO));

        // 移除事后处理任务
        applicationContext.publishEvent(new CallStopArrangeCancelEvent(this, companyCode, agentId));

        if (AgentStatusEnum.ARRANGE.name().equals(agentStatusDTO.getTargetStatus())) {
            // send arrange task
            AgentInfo agentInfo = commonDataOperateService.getAgentInfo(companyCode, agentId);
            Integer processTime = agentInfo.getProcessTime();
            applicationContext.publishEvent(new CallStopArrangeEvent(this, processTime, clientCheckinDTO));
        }
        
        if (Boolean.TRUE.equals(clientCheckinDTO.getReset())) {
            //  复位挂断分机通话
            hangup(clientCheckinVO, extStatusDTO);
            // 复位分机
            extStatusDTO.setSourceStatus(extStatusDTO.getTargetStatus());
            extStatusDTO.setSourceTimestamp(extStatusDTO.getSourceTimestamp());
            extStatusDTO.setTransferAction(ExtStatusTransferActionEnum.RESET.name());
            extStatusDTO.setTargetStatus(ExtStatusEnum.ONLINE.name());
            extStatusDTO.setTargetTimestamp(System.currentTimeMillis());
            dataStoreService.updateActualExtStatus(extStatusDTO);
            if (log.isInfoEnabled()) {
                log.info("[签入-复位] 分机状态: {}", objectMapper.writeValueAsString(extStatusDTO));
            }
        }
        clientCheckinVO.setToken(commonDataOperateService.createToken(companyCode, agentId, clientCheckinDTO.getOs()));
        return clientCheckinVO;
    }

    private String getTargetStatus(ClientCheckinDTO clientCheckinDTO) {
        String startStatus = clientCheckinDTO.getStartStatus();
        if (StrUtil.isEmpty(startStatus)) {
            return AgentStatusEnum.FREE.name();
        }
        return startStatus.toUpperCase();
    }

    private void hangup(ClientCheckinVO clientCheckinVO, ExtStatusDTO extStatusDTO) {
        String companyCode = extStatusDTO.getCompanyCode();
        String uuid = extStatusDTO.getUuid();

        if (ExtCallModeEnum.LONG_CALL.getCode().equals(clientCheckinVO.getExtCallMode())) {
            String extId = extStatusDTO.getExtId();
            HangupDTO hangupDTO = HangupDTO.buildReset(companyCode, extId, HangupCauseEnum.RESET);
            freeswitchRequestService.hangupAlwaysExt(hangupDTO);
            return;
        }
        HangupDTO hangupDTO = HangupDTO.build(companyCode, true, uuid, HangupCauseEnum.RESET);
        freeswitchRequestService.hangup(hangupDTO);
    }

    private void queueOperate(ClientCheckinDTO clientCheckinDTO, AgentStatusDTO agentStatusDTO, Long currentTimestamp) {
        Integer serviceMode = agentStatusDTO.getServiceMode();
        // 签入默认服务模式
        AgentServiceModeEnum serviceModeEnum = AgentServiceModeEnum.parse(serviceMode);
        String companyCode = clientCheckinDTO.getCompanyCode();
        String agentId = clientCheckinDTO.getAgentId();
        String startStatus = agentStatusDTO.getTargetStatus();

        // 发送签入坐席信息到topic
        AgentWeightInfoDTO agentWeightInfoDTO = commonDataOperateService.getAgentWithWeightInfo(agentId);
        AgentInfo agentInfo = commonDataOperateService.getAgentInfo(companyCode, agentId);
        AgentCheckinCacheDTO agentCheckinCacheDTO = AgentCheckinCacheDTO.buildNew(agentInfo, agentWeightInfoDTO);
        AgentCheckinCache.put(companyCode, agentId, agentCheckinCacheDTO);
        agentCheckinCacheDTO.setMsg("checkin");
        // topicName:tags
        Message<AgentCheckinCacheDTO> message = MessageBuilder.withPayload(agentCheckinCacheDTO)
                .setHeader(MessageConst.PROPERTY_KEYS, agentCheckinCacheDTO.getAgentId())
                .build();
        rocketMQTemplate.syncSend("cloudcc_broadcast_topic:agentWeight", message);
        // 初始化当天通话统计信息
        dataQueryService.initCallStats(companyCode, agentId, agentWeightInfoDTO);

        // old
        AgentServiceModeEnum oldMode = getAgentServiceMode(serviceModeEnum);
        applicationContext.publishEvent(new OfflineAgentQueueEvent(this,
                companyCode, agentId, oldMode, currentTimestamp, OperateTypeEnum.DELETE));
        applicationContext.publishEvent(new FreeAgentQueueEvent(this,
                companyCode, agentId, oldMode, currentTimestamp, OperateTypeEnum.DELETE));

        // 企业坐席离线队列-删除
        applicationContext.publishEvent(new OfflineAgentQueueEvent(this,
                companyCode, agentId, serviceModeEnum, currentTimestamp, OperateTypeEnum.DELETE));
        log.info("[签入] 企业坐席离线队列-删除, 企业: {}, 坐席: {}", companyCode, agentId);
        if (AgentStatusEnum.FREE.name().equals(startStatus)) {
            // 企业坐席空闲队列-添加
            applicationContext.publishEvent(new FreeAgentQueueEvent(this,
                    companyCode, agentId, serviceModeEnum, currentTimestamp, OperateTypeEnum.INSERT));
            log.info("[签入-空闲] 企业坐席空闲队列-添加, 企业: {}, 坐席: {}", companyCode, agentId);
        }
    }

    private AgentServiceModeEnum getAgentServiceMode(AgentServiceModeEnum serviceModeEnum) {
        if (AgentServiceModeEnum.CUSTOMER.equals(serviceModeEnum)) {
            return AgentServiceModeEnum.OUTBOUND;
        }
        if (AgentServiceModeEnum.OUTBOUND.equals(serviceModeEnum)) {
            return AgentServiceModeEnum.CUSTOMER;
        }
        return serviceModeEnum;
    }

    private ClientCheckinVO checkInHandle(ClientCheckinDTO param, GetExtensionRegVO copyGetExtensionRegVO) throws Exception {
        String agentId = param.getAgentId();
        String companyCode = param.getCompanyCode();

        // 先判断企业是否存在
        CompanyInfo companyInfo = commonDataOperateService.getCompanyInfoDTO(companyCode);
        if (companyInfo == null) {
            return new ClientCheckinVO(param, SdkErrCode.COMPANY_NOT_EXIST);
        }
        // 然后判断坐席是否存在
        AgentInfo agentInfo = commonDataOperateService.getAgentInfo(companyCode, agentId);
        if (agentInfo == null) {
            return new ClientCheckinVO(param, SdkErrCode.AGENT_NOT_EXIST);
        }
        // 判断坐席密码是否一致
        String encryptUserPwd = aesUtil.encrypt(param.getAgentPwd());
        if (!encryptUserPwd.equals(agentInfo.getPassword())) {
            return new ClientCheckinVO(param, SdkErrCode.AGENT_PWD_ERROR);
        }
        // 判断坐席账号是否被禁用
        if (CommonConstant.ENABLE_N.equals(agentInfo.getState())) {
            return new ClientCheckinVO(param, SdkErrCode.AGENT_DISABLE);
        }
        // 判断企业是否已被禁用
        if (CommonConstant.ENABLE_N.equals(companyInfo.getState())) {
            return new ClientCheckinVO(param, SdkErrCode.COMPANY_DISABLE);
        }

        // 坐席分机是否已绑定
        if (StrUtil.isEmpty(agentInfo.getSysExtId())) {
            return new ClientCheckinVO(param, SdkErrCode.AGENT_NOT_BIND_EXT);
        }
        String extId = agentInfo.getSysExtId();
        // 坐席绑定分机与传入分机是否一致
        if (!agentInfo.getSysExtId().equals(param.getExtId())) {
            return new ClientCheckinVO(param, SdkErrCode.AGENT_BIND_EXT_ERROR);
        }

        ExtInfo extInfo = commonDataOperateService.getExtInfo(param.getCompanyCode(), extId);
        if (extInfo == null) {
            return new ClientCheckinVO(param, SdkErrCode.EXT_ID_NOT_EXT);
        }
        // 这里要判断下用户指定的分机号是否已被其他坐席绑定了
        String bindAgentId = commonDataOperateService.getAgentIdRelateExtId(companyCode, extId);
        if (bindAgentId != null && !agentId.equals(bindAgentId)) {
            return new ClientCheckinVO(param, SdkErrCode.EXT_ID_BINDING, "分机已被坐席: " + bindAgentId + "绑定");
        }

        GetExtensionRegVO getExtensionRegVO = dataQueryService.getExtRealRegStatus(companyCode, extId);
        copyGetExtensionRegVO.copy(getExtensionRegVO, copyGetExtensionRegVO);
        boolean realRegStatus = Boolean.TRUE.equals(getExtensionRegVO.getRegStatus());

        // 如果是软终端的话, 要判断分机状态,
        if (CommonConstant.EXT_REG_MODE_OTHER.equals(agentInfo.getExtRegMode())) {
            // 如果分机不在线, 则不能签入
            if (!realRegStatus) {
                return new ClientCheckinVO(param, SdkErrCode.EXT_CLIENT_OFFLINE);
            }
        }

        // 这里要查下当前的分机状态. 如果已签入则按互顶配置
        AgentCheckInRecord checkInRecord = dataQueryService.getAgentCheckInRecord(agentId, param.getOs());
        if (Objects.nonNull(checkInRecord)) {
            log.info("坐席账号: {}, 分机号: {}, 上次签入记录: {}", agentId, extId, JSON.toJSONString(checkInRecord));
            // 这里再判断下分机当前的真实注册状态, 防止出现误判
            if (realRegStatus) {
                // 这里判断企业是否允许互顶
                if (CommonConstant.ENABLE_N.equals(companyInfo.getAgentAccountKick())) {
                    log.info("企业: {} 坐席账号不允许互顶", companyCode);
                    Set<String> clientIps = new HashSet<>(StrUtil.split(param.getAgentIp(), StrUtil.COMMA));
                    // 如果当前客户端ip与上一次签入客户端ip不匹配, 则大概率说明是不同客户端,
                    // 移动客户端或者DHCP可能会造成相同客户端, 不同ip的情况。依赖ip来唯一标识客户端不完全可靠，后续可以考虑其他方案
                    if (!clientIps.containsAll(checkInRecord.getClientIps())) {
                        String errorMsg = String.format("坐席账号已在其他地方签入 %s", String.join(StrUtil.COMMA,
                                checkInRecord.getClientIps()));
                        return new ClientCheckinVO(param, SdkErrCode.AGENT_CHECKED_IN, errorMsg);
                    }
                    log.info("坐席账号: {}, 分机号: {}, 为相同客户端签入, 本次允许互顶", agentId, extId);
                    // 这里通知netty将对应的sdk客户端踢下线，后续不能再重连
                    dataStoreService.agentKickoffNotify(param, checkInRecord);
                }
            }
        }

        ClientCheckinVO clientCheckinVO = new ClientCheckinVO(param, SdkErrCode.OK);
        clientCheckinVO.setExtId(extId);
        clientCheckinVO.setExtPwd(aesUtil.decrypt(extInfo.getPassword()));
        clientCheckinVO.setExtCallMode(extInfo.getCallMode());
        // 获取分机注册地址
        DisExtensionRegAddrVO apiVO = dataStoreService.disExtRegAddr(companyCode, extId);
        log.info("企业编号: {}, 分机: {}, 请求fs分配分机注册地址, {}", companyCode, extId, apiVO);
        if (apiVO == null || Boolean.FALSE.equals(apiVO.getResult())) {
            return new ClientCheckinVO(param, SdkErrCode.GET_REG_ADDR_FAILED);
        }
        clientCheckinVO.setServiceMode(agentInfo.getServiceMode());
        clientCheckinVO.setPbx(apiVO.getRegAddr());
        clientCheckinVO.setRtcAddress(apiVO.getRegAddr());
        clientCheckinVO.setLoginType(String.valueOf(agentInfo.getExtRegMode()));
        // 添加签入记录
        dataStoreService.addCheckInRecord(param);

        // 如果当前坐席没有绑定分机, 则设置分机与坐席进行绑定
        if (StrUtil.isBlank(agentInfo.getSysExtId())) {
            log.info("坐席账号: {}, 当前未绑定分机, 用户设置分机: {}, 设置绑定关系", agentId, extId);
            dataStoreService.extBindAgent(extId, agentId);
        }

        return clientCheckinVO;
    }

}
