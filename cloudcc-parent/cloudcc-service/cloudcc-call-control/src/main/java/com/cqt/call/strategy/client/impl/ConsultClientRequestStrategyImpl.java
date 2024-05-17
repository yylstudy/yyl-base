package com.cqt.call.strategy.client.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.*;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.call.service.DataQueryService;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.client.ClientRequestStrategy;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.agent.vo.RelateUuidDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientConsultDTO;
import com.cqt.model.client.vo.ClientConsultVO;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.dto.api.OriginateDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 咨询
 * uuid    string    是    需要咨询的通话uuid
 * type    string    是    咨询类型（1发起2取消）
 * consult_type    string    是    咨询人员1、坐席2、外线
 * consult_number    string    是    咨询号码（坐席ID 或者外线号码）
 * os    string    是    设备系统
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultClientRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final FreeswitchRequestService freeswitchRequestService;

    private final DataStoreService dataStoreService;

    private final DataQueryService dataQueryService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.consult;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientConsultDTO clientConsultDTO = convert(requestBody, ClientConsultDTO.class);
        String companyCode = clientConsultDTO.getCompanyCode();
        String consultNumber = clientConsultDTO.getConsultNumber();
        // 咨询人员 0:内线, 1:外线
        Integer consultType = clientConsultDTO.getConsultType();
        // 咨询类型（1-发起, 2-取消）
        Integer type = clientConsultDTO.getType();
        if (type == 2) {
            return cancelConsult(clientConsultDTO);
        }

        // 验证发起咨询坐席参数
        CheckAgentAvailableVO checkAgentAvailableVO = checkAgentAvailable(ExtStatusEnum.CALLING,
                Lists.newArrayList(AgentStatusEnum.CALLING),
                companyCode, clientConsultDTO.getExtId(), clientConsultDTO.getAgentId(),
                getOutlineNumber(clientConsultDTO));
        if (!checkAgentAvailableVO.getAvailable()) {
            return ClientResponseBaseVO.response(clientConsultDTO, "1", checkAgentAvailableVO.getMessage());
        }
        AgentInfo consultAgentInfo = null;
        if (consultType == 0) {
            // 被咨询人员  若是内线 要判断是否绑定分机, 分机是在线且坐席时空闲状态
            CheckAgentAvailableVO consultCheckAgentAvailableVO = checkAgentAvailable(ExtStatusEnum.ONLINE,
                    Lists.newArrayList(AgentStatusEnum.FREE),
                    companyCode, "", consultNumber);
            if (!consultCheckAgentAvailableVO.getAvailable()) {
                return ClientResponseBaseVO.response(clientConsultDTO, "1", consultCheckAgentAvailableVO.getMessage());
            }
            consultAgentInfo = consultCheckAgentAvailableVO.getAgentInfo();
        }

        // 查询发起咨询坐席uuid信息
        CallUuidContext callUuidContext = dataQueryService.getCallUuidContext(companyCode, clientConsultDTO.getUuid());

        // 发起咨询
        if (1 == type) {
            OriginateDTO originateDTO = new OriginateDTO();
            String oriUuid = getMsgType() + StrUtil.DASHED + IdUtil.fastSimpleUUID();
            originateDTO.setOriUuid(oriUuid);
            originateDTO.setReqId(clientConsultDTO.getReqId());
            originateDTO.setCompanyCode(companyCode);
            originateDTO.setCallerNumber(clientConsultDTO.getExtId());
            // 咨询号码（坐席ID 或者外线号码） 内线要根据坐席id查询分机id
            String calleeNumber = getCalleeNumber(consultType, consultNumber, consultAgentInfo);
            if (StrUtil.isEmpty(calleeNumber)) {
                return ClientResponseBaseVO.response(clientConsultDTO, "1", "咨询号码不可用!");
            }
            // 外线号码
            if (consultType == 1) {
                String displayNumber = dataQueryService.getAgentDisplayNumber(checkAgentAvailableVO.getCompanyInfo(),
                        checkAgentAvailableVO.getAgentInfo());
                originateDTO.setDisplayNumber(displayNumber);
            }
            originateDTO.setCalleeNumber(calleeNumber);
            originateDTO.setOutLine(consultType);
            originateDTO.setServerId(callUuidContext.getCurrent().getServerId());
            // 初始化音视频属性
            originateDTO.initConsultProperties(clientConsultDTO);
            // 先写入redis
            writeCallUuidContext(clientConsultDTO, checkAgentAvailableVO, callUuidContext, originateDTO, consultAgentInfo);
            FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.originate(originateDTO);
            if (freeswitchApiVO.getResult()) {
                // save consult flag
                dataStoreService.saveAgentConsultFlag(callUuidContext.getUUID());
                return ClientConsultVO.response(clientConsultDTO, oriUuid, "0", "发起咨询成功!");
            }
            return ClientResponseBaseVO.response(clientConsultDTO, "1", "发起咨询失败!");
        }
        return ClientResponseBaseVO.response(clientConsultDTO, "1", "咨询类型错误!");
    }

    private ClientResponseBaseVO cancelConsult(ClientConsultDTO clientConsultDTO) {
        String companyCode = clientConsultDTO.getCompanyCode();
        // 查询发起咨询坐席uuid信息
        CallUuidContext callUuidContext = dataQueryService.getCallUuidContext(companyCode, clientConsultDTO.getUuid());
        // 取消咨询
        RelateUuidDTO relateUuidDTO = callUuidContext.getRelateUuidDTO();
        String consultUuid = relateUuidDTO.getConsultUUID();
        log.info("[取消咨询] 企业: {}, uuid: {}", companyCode, consultUuid);
        // 挂断
        HangupDTO hangupDTO = HangupDTO.build(companyCode, consultUuid, HangupCauseEnum.CANCEL_CONSULT_HANGUP);
        FreeswitchApiVO hangup = freeswitchRequestService.hangup(hangupDTO);
        log.info("[取消咨询] 企业: {}, uuid: {}, 挂断结果: {}", companyCode, consultUuid, hangup);
        dataStoreService.saveCallUuidContext(callUuidContext);
        dataStoreService.delAgentConsultFlag(callUuidContext.getUUID());
        return ClientResponseBaseVO.response(clientConsultDTO, "0", "取消咨询成功!");
    }

    private String getOutlineNumber(ClientConsultDTO clientConsultDTO) {
        if (OutLineEnum.OUT_LINE.getCode().equals(clientConsultDTO.getConsultType())) {
            return clientConsultDTO.getConsultNumber();
        }
        return null;
    }

    /**
     * 内线, consultNumber是坐席id, 根据坐席id查询分机id
     */
    private String getCalleeNumber(Integer consultType, String consultNumber, AgentInfo agentInfo) {
        // 外线
        if (consultType == 1) {
            return consultNumber;
        }
        // 内线
        return agentInfo.getSysExtId();
    }

    /**
     * 保存uuid上下文
     *
     * @param clientConsultDTO      SKD咨询请求参数
     * @param checkAgentAvailableVO 坐席校验结果
     * @param callUuidContext       查询发起咨询坐席uuid信息
     * @param originateDTO          外呼被咨询方接口参数
     * @param consultAgentInfo      被咨询坐席的信息
     */
    private void writeCallUuidContext(ClientConsultDTO clientConsultDTO,
                                      CheckAgentAvailableVO checkAgentAvailableVO,
                                      CallUuidContext callUuidContext,
                                      OriginateDTO originateDTO,
                                      AgentInfo consultAgentInfo) {
        Integer consultType = clientConsultDTO.getConsultType();
        CallTypeEnum callType = 0 == consultType ? CallTypeEnum.AGENT : CallTypeEnum.CLIENT;
        CallRoleEnum callRole = 0 == consultType ? CallRoleEnum.CONSULT_AGENT : CallRoleEnum.CONSULT_CLIENT;
        // 保存通话uuid之间关联关系, 这步必须在通话事件来之前设置!
        CallUuidRelationDTO callUuidRelationDTO = new CallUuidRelationDTO();
        callUuidRelationDTO.setMainCdrFlag(false);
        callUuidRelationDTO.setMainCallId(callUuidContext.getMainCallId());
        callUuidRelationDTO.setMainUuid(callUuidContext.getMainUUID());
        callUuidRelationDTO.setUuid(originateDTO.getOriUuid());
        callUuidRelationDTO.setXferUUID(callUuidContext.getUUID());
        callUuidRelationDTO.setRelationUuid(callUuidContext.getUUID());
        callUuidRelationDTO.setCallRoleEnum(callRole);
        callUuidRelationDTO.setCallTypeEnum(callType);
        if (Objects.nonNull(consultAgentInfo)) {
            callUuidRelationDTO.setExtIp("");
            callUuidRelationDTO.setExtId(consultAgentInfo.getSysExtId());
        }
        callUuidRelationDTO.setAgentId(0 == consultType ? clientConsultDTO.getConsultNumber() : "");
        callUuidRelationDTO.setReqId(clientConsultDTO.getReqId());
        callUuidRelationDTO.setOs(callUuidContext.getCurrent().getOs());
        callUuidRelationDTO.setCallerNumber(originateDTO.getCallerNumber());
        callUuidRelationDTO.setDisplayNumber(originateDTO.getCallerNumber());
        callUuidRelationDTO.setCalleeNumber(originateDTO.getCalleeNumber());
        callUuidRelationDTO.setNumber(originateDTO.getCalleeNumber());
        callUuidRelationDTO.setCompanyCode(clientConsultDTO.getCompanyCode());
        callUuidRelationDTO.setServerId(callUuidContext.getServerId());
        callUuidRelationDTO.setOriginateAfterActionEnum(OriginateAfterActionEnum.XFER);
        callUuidRelationDTO.setXferActionEnum(XferActionEnum.CONSULT);
        callUuidRelationDTO.setCallDirectionEnum(callUuidContext.getCallDirection());
        // 呼入属性
        callUuidRelationDTO.setVideo(clientConsultDTO.getVideo());
        callUuidRelationDTO.setAudio(clientConsultDTO.getAudio());
        callUuidRelationDTO.setCallInFlag(0 == consultType);
        callUuidRelationDTO.setCallInChannel(CallInChannelEnum.CONSULT);
        callUuidRelationDTO.setHangupAll(false);
        callUuidRelationDTO.setRecordNode(RecordNodeEnum.CALL_OUT_B);
        CallCdrDTO callCdrDTO = new CallCdrDTO();
        callUuidRelationDTO.setCallCdrDTO(callCdrDTO);
        // 被咨询坐席
        CallUuidContext consultCallUuidContext = CallUuidContext.builder()
                .current(callUuidRelationDTO)
                .build();
        consultCallUuidContext.fillRelationUuidSet(callUuidContext.getUUID());
        dataStoreService.saveCallUuidContext(consultCallUuidContext);

        // 发起咨询坐席
        callUuidContext.fillRelateUuidDtoByConsult(originateDTO.getOriUuid());
        callUuidContext.getRelationUuid().add(originateDTO.getOriUuid());
        log.info("[咨询] 发起咨询坐席: {}, 关联uuid: {}", callUuidContext.getCurrent().getAgentId(), callUuidContext.getRelationUuid());
        dataStoreService.saveCallUuidContext(callUuidContext);
    }

}
