package com.cqt.call.strategy.client.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.contants.CommonConstant;
import com.cqt.base.enums.*;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.base.enums.trans.TransModeEnum;
import com.cqt.base.enums.trans.TransTypeEnum;
import com.cqt.base.model.ResultVO;
import com.cqt.call.service.DataQueryService;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.client.ClientRequestStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.agent.vo.CallUuidRelationDTO;
import com.cqt.model.cdr.dto.CallCdrDTO;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientTransDTO;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.freeswitch.dto.api.*;
import com.cqt.model.freeswitch.vo.ExecuteLuaVO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.cqt.model.queue.dto.CallInIvrActionDTO;
import com.cqt.model.queue.entity.IvrServiceInfo;
import com.cqt.model.queue.vo.CallInIvrActionVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 转接
 * uuid  string  是  需要转接的通话uuid
 * type  string  转接类型 1盲转 2咨询转  转接类型
 * trans_type  string  是  转接人员: 1坐席 2技能 3ivr 4外线 5满意度
 * trans_number  string  否  转接号码（坐席ID、技能ID、ivrID、不填为默认满意度）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransClientRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final ObjectMapper objectMapper;

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    private final DataStoreService dataStoreService;

    private final DataQueryService dataQueryService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.trans;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientTransDTO clientTransDTO = convert(requestBody, ClientTransDTO.class);
        String companyCode = clientTransDTO.getCompanyCode();
        String uuid = clientTransDTO.getUuid();
        // 查询发起转接坐席uuid信息
        CallUuidContext callUuidContext = dataQueryService.getCallUuidContext(companyCode, clientTransDTO.getUuid());
        if (Objects.isNull(callUuidContext)) {
            log.info("[SDK-挂断请求] 企业id: {}, uuid: {}, 未查询到上下文信息", companyCode, uuid);
            return ClientResponseBaseVO.response(clientTransDTO, "1", "uuid不存在!");
        }
        CallUuidRelationDTO current = callUuidContext.getCurrent();
        // 验证坐席参数
        CheckAgentAvailableVO checkAgentAvailableVO = checkAgentAvailable(ExtStatusEnum.CALLING,
                Lists.newArrayList(AgentStatusEnum.CALLING),
                companyCode, clientTransDTO.getExtId(), clientTransDTO.getAgentId(), getOutlineNumber(clientTransDTO));
        if (!checkAgentAvailableVO.getAvailable()) {
            return ClientResponseBaseVO.response(clientTransDTO, "1", checkAgentAvailableVO.getMessage());
        }
        Integer type = clientTransDTO.getType();
        Optional<TransModeEnum> transModeEnumOptional = TransModeEnum.of(type);
        if (!transModeEnumOptional.isPresent()) {
            return ClientResponseBaseVO.response(clientTransDTO, "1", "转接类型不支持!");
        }
        TransModeEnum transModeEnum = transModeEnumOptional.get();

        // 取消咨询转
        if (Objects.equals(TransModeEnum.CANCEL_TRANS, transModeEnum)) {
            return cancelConsultTrans(clientTransDTO, callUuidContext);
        }

        // 转接人员: 1坐席 2技能 3ivr 4外线 5满意度
        Integer transType = clientTransDTO.getTransType();
        Optional<TransTypeEnum> transTypeEnumOptional = TransTypeEnum.of(transType);
        if (!transTypeEnumOptional.isPresent()) {
            return ClientResponseBaseVO.response(clientTransDTO, "1", "转接人员类型不支持!");
        }
        TransTypeEnum transTypeEnum = transTypeEnumOptional.get();
        log.info("[转接] 企业: {}, 坐席: {}, 转接模式: {}, 转接人员类型: {}, 转接号码: {}",
                companyCode, clientTransDTO.getAgentId(), clientTransDTO.getType(),
                transTypeEnum.getName(), clientTransDTO.getTransNumber());

        // 是否满意度
        boolean satisfaction = isSatisfaction(transTypeEnum);
        if (TransTypeEnum.TRANS_IVR.equals(transTypeEnum) || satisfaction) {
            return transIvrOrSatisfaction(clientTransDTO, callUuidContext, satisfaction);
        }

        // 客户号码
        String callerNumber = current.getMainCdrFlag() ? current.getCalleeNumber() : current.getCallerNumber();
        // 转接技能
        if (TransTypeEnum.TRANS_SKILL.equals(transTypeEnum)) {
            return transSkill(clientTransDTO, callUuidContext, callerNumber);
        }

        CallRoleEnum callRoleEnum = CallRoleEnum.TRANS_AGENT;
        CallTypeEnum callTypeEnum = CallTypeEnum.AGENT;
        int outline = OutLineEnum.IN_LINE.getCode();
        // 转接主叫号码, 与发起转接坐席通话的号码
        String displayNumber = callerNumber;
        String calleeNumber = "";
        String transAgentId = "";
        boolean isTransAgent = false;

        if (TransTypeEnum.TRANS_AGENT.equals(transTypeEnum)) {
            transAgentId = clientTransDTO.getTransNumber();
            isTransAgent = true;
        }
        if (TransTypeEnum.TRANS_OUT_LINE.equals(transTypeEnum)) {
            outline = OutLineEnum.OUT_LINE.getCode();
            callRoleEnum = CallRoleEnum.TRANS_CLIENT;
            callTypeEnum = CallTypeEnum.CLIENT;
            calleeNumber = clientTransDTO.getTransNumber();
            displayNumber = dataQueryService.getAgentDisplayNumber(checkAgentAvailableVO.getCompanyInfo(),
                    checkAgentAvailableVO.getAgentInfo());
            log.info("[转接] 外线, 企业id: {}, 坐席id: {}, 外线号码: {}", companyCode, clientTransDTO.getAgentId(), calleeNumber);
        }
        CheckAgentAvailableVO transCheckAgentAvailableVO = null;
        if (isTransAgent) {
            // 检测转接坐席是否可用
            transCheckAgentAvailableVO = checkAgentAvailable(ExtStatusEnum.ONLINE,
                    Lists.newArrayList(AgentStatusEnum.FREE),
                    companyCode, "", transAgentId);
            if (!transCheckAgentAvailableVO.getAvailable()) {
                return ClientResponseBaseVO.response(clientTransDTO, "1", transCheckAgentAvailableVO.getMessage());
            }
            calleeNumber = transCheckAgentAvailableVO.getAgentInfo().getSysExtId();
            log.info("[转接] 内线, 企业id: {}, 坐席id: {}, 转接坐席id: {}, 内线号码: {}",
                    companyCode, clientTransDTO.getAgentId(), transAgentId, calleeNumber);
        }
        log.info("[转接] 企业: {}, 主叫号码: {}, 外显号码: {}, 被叫号码: {}", companyCode, callerNumber, displayNumber, calleeNumber);

        // 盲转-直接挂断原坐席, 接续坐席B与客户
        if (Objects.equals(transModeEnum, TransModeEnum.BLIND_TRANS)) {
            // 1. 先挂断发起转接坐席 - 在挂断事件不能挂断所有关联坐席
            callUuidContext.getCurrent().setTransModeEnum(TransModeEnum.BLIND_TRANS);
            dataStoreService.saveCallUuidContext(callUuidContext);
            HangupDTO hangupDTO = HangupDTO.build(companyCode, uuid, HangupCauseEnum.BLIND_TRANS_HANGUP);
            freeswitchRequestService.hangup(hangupDTO);
            log.info("[转接-盲转] 企业id: {}, 挂断原坐席: {}", companyCode, clientTransDTO.getAgentId());
            // 客户uuid上下文
            String relationUuid = current.getRelationUuid();
            CallUuidContext clientCallUuidContext = dataQueryService.getCallUuidContext(companyCode, relationUuid);

            // 2. callBridge
            CallBridgeDTO callBridgeDTO = CallBridgeDTO.build(clientTransDTO);
            String oriUuid = getMsgType() + StrUtil.DASHED + IdUtil.fastSimpleUUID();
            callBridgeDTO.setSUuid(relationUuid);
            callBridgeDTO.setOriUuid(oriUuid);
            // 主叫号码
            callBridgeDTO.setCallerNumber(callerNumber);
            // 被转接人的号码, 坐席-分机id,
            callBridgeDTO.setCalleeNumber(calleeNumber);
            callBridgeDTO.setDisplayNumber(displayNumber);
            callBridgeDTO.setOutLine(outline);
            callBridgeDTO.setServerId(current.getServerId());
            // 可选参数未填-呼入的一致
            callBridgeDTO.initTransProperties(clientTransDTO);
            // 写入转接坐席uuid
            writeBlindTransCallUuidRelationDTO(clientTransDTO, callUuidContext, callBridgeDTO,
                    callRoleEnum, callTypeEnum, clientCallUuidContext, isTransAgent, transCheckAgentAvailableVO);
            FreeswitchApiVO callBridgeVO = freeswitchRequestService.callBridge(callBridgeDTO);
            log.info("[转接-盲转] 企业id: {}, 桥接转接人员: {}", companyCode, calleeNumber);
            if (callBridgeVO.getResult()) {
                return ClientResponseBaseVO.response(clientTransDTO, "0", "发起盲转成功!");
            }
            return ClientResponseBaseVO.response(clientTransDTO, "1", "发起盲转失败!");
        }

        // 发起咨询转
        if (Objects.equals(transModeEnum, TransModeEnum.CONSULT_TRANS)) {
            // 客户uuid上下文
            CallUuidContext clientCallUuidContext = dataQueryService.getCallUuidContext(companyCode, current.getRelationUuid());
            CallUuidRelationDTO clientCurrent = clientCallUuidContext.getCurrent();
            // 先外呼转接人员, 在接通事件xfer
            OriginateDTO originateDTO = OriginateDTO.build(clientTransDTO);
            String oriUuid = getMsgType() + StrUtil.DASHED + IdUtil.fastSimpleUUID();
            originateDTO.setOriUuid(oriUuid);
            // 主叫号码
            originateDTO.setCallerNumber(callerNumber);
            originateDTO.setCalleeNumber(calleeNumber);
            originateDTO.setDisplayNumber(displayNumber);
            originateDTO.setOutLine(outline);
            originateDTO.setServerId(clientCurrent.getServerId());
            // 初始化音视频属性
            originateDTO.initTransProperties(clientTransDTO);
            // 保存转接人员context
            writeConsultTransCallUuidContext(clientTransDTO, callUuidContext, clientCallUuidContext, originateDTO,
                    callRoleEnum, callTypeEnum, isTransAgent, transCheckAgentAvailableVO);
            FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.originate(originateDTO);
            log.info("[转接-咨询转] 企业id: {}, 内外线: {}, 外呼转接人员: {}", companyCode, outline, calleeNumber);
            if (freeswitchApiVO.getResult()) {
                return ClientResponseBaseVO.response(clientTransDTO, "0", "发起咨询转成功!");
            }
            return ClientResponseBaseVO.response(clientTransDTO, "1", "发起咨询转失败!");
        }
        return ClientResponseBaseVO.response(clientTransDTO, "1", "发起转接失败!");
    }

    private String getOutlineNumber(ClientTransDTO clientTransDTO) {
        if (TransTypeEnum.TRANS_OUT_LINE.getCode().equals(clientTransDTO.getTransType())) {
            return clientTransDTO.getTransNumber();
        }
        return null;
    }

    /**
     * 转接技能
     *
     * @param clientTransDTO  转接信息
     * @param callUuidContext 呼叫uuid上下文
     * @param callerNumber    客户号码
     * @return ClientResponseBaseVO 返回的客户端响应
     */
    private ClientResponseBaseVO transSkill(ClientTransDTO clientTransDTO,
                                            CallUuidContext callUuidContext,
                                            String callerNumber) throws JsonProcessingException {
        String companyCode = clientTransDTO.getCompanyCode();
        String uuid = clientTransDTO.getUuid();
        // 只挂断自己
        callUuidContext.getCurrent().setHangupAll(false);
        dataStoreService.saveCallUuidContext(callUuidContext);

        // 挂断坐席
        HangupDTO hangupDTO = HangupDTO.build(companyCode, uuid, HangupCauseEnum.TRANS_SKILL);
        freeswitchRequestService.hangup(hangupDTO);
        // 调用排队接口, 获取坐席id
        CallInIvrActionDTO callInIvrActionDTO = new CallInIvrActionDTO();
        callInIvrActionDTO.setCompanyCode(companyCode);
        callInIvrActionDTO.setSkillId(clientTransDTO.getTransNumber());
        callInIvrActionDTO.setAudio(callUuidContext.getAudio());
        callInIvrActionDTO.setVideo(callUuidContext.getVideo());
        callInIvrActionDTO.setMaxRetry(1);
        callInIvrActionDTO.setCallerNumber(callerNumber);
        callInIvrActionDTO.setCurrentTimes(1);
        callInIvrActionDTO.setUuid(callUuidContext.findRelationUUID());
        callInIvrActionDTO.setType(CallInIvrActionEnum.TRANS_SKILl.getCode());
        if (log.isInfoEnabled()) {
            log.info("[转接] 转接技能参数: {}", objectMapper.writeValueAsString(callInIvrActionDTO));
        }
        ResultVO<CallInIvrActionVO> resultVO = dataStoreService.distributeAgent(callInIvrActionDTO);
        if (!resultVO.success()) {
            return ClientResponseBaseVO.response(clientTransDTO, "1", "发起转接技能失败!");
        }
        return ClientResponseBaseVO.response(clientTransDTO, "0", "发起转接技能成功!");
    }

    /**
     * 转接-咨询转
     *
     * @param clientTransDTO  转接信息
     * @param callUuidContext 呼叫uuid上下文
     * @return ClientResponseBaseVO 返回的客户端响应
     */
    private ClientResponseBaseVO cancelConsultTrans(ClientTransDTO clientTransDTO, CallUuidContext callUuidContext) {
        String companyCode = clientTransDTO.getCompanyCode();
        String consulTransUUID = callUuidContext.getRelateUuidDTO().getConsulTransUUID();
        log.info("[转接] 取消咨询转 企业id: {}, consulTransUUID: {}", companyCode, consulTransUUID);
        CallUuidContext consultContext = dataQueryService.getCallUuidContext(companyCode, consulTransUUID);
        consultContext.getCurrent().setHangupAll(false);
        dataStoreService.saveCallUuidContext(consultContext);

        callUuidContext.getCurrent().setHangupAll(null);
        dataStoreService.saveCallUuidContext(callUuidContext);

        HangupDTO hangupDTO = HangupDTO.build(companyCode, consulTransUUID, HangupCauseEnum.CANCEL_CONSULT_TRANS);
        FreeswitchApiVO apiVO = freeswitchRequestService.hangup(hangupDTO);
        if (apiVO.getResult()) {
            return ClientResponseBaseVO.response(clientTransDTO, "0", "咨询转取消成功!");
        }
        return ClientResponseBaseVO.response(clientTransDTO, "1", "咨询转取消失败!");
    }

    /**
     * 转接ivr或满意度
     *
     * @param clientTransDTO  转接信息
     * @param callUuidContext 呼叫uuid上下文
     * @param satisfaction    是否为满意度转接
     * @return ClientResponseBaseVO 返回的客户端响应
     */
    private ClientResponseBaseVO transIvrOrSatisfaction(ClientTransDTO clientTransDTO,
                                                        CallUuidContext callUuidContext,
                                                        boolean satisfaction) {
        String companyCode = callUuidContext.getCompanyCode();
        String uuid = clientTransDTO.getUuid();
        // 不挂断所有
        callUuidContext.getCurrent().setHangupAll(false);
        // IVR逻辑-是给被叫侧执行ivr
        String relationUuid = callUuidContext.getCurrent().getRelationUuid();
        CallUuidContext clientCallUuidContext = dataQueryService.getCallUuidContext(companyCode, relationUuid);
        DateTime dateTime = DateUtil.date();
        if (satisfaction) {
            clientCallUuidContext.setSatisfaction(true);
            clientCallUuidContext.setStartSatisfactionTime(dateTime);
            callUuidContext.setSatisfaction(true);
            callUuidContext.setStartSatisfactionTime(dateTime);
        } else {
            clientCallUuidContext.setTransIVR(true);
            clientCallUuidContext.setStartTransIvrTime(dateTime);
            callUuidContext.setTransIVR(true);
            callUuidContext.setStartTransIvrTime(dateTime);
        }
        dataStoreService.saveCallUuidContext(callUuidContext);
        dataStoreService.saveCallUuidContext(clientCallUuidContext);

        log.info("[转接] satisfaction: {}, 企业id: {}, ivrId: {}", satisfaction, companyCode, clientTransDTO.getTransNumber());
        ExecuteLuaVO executeLuaVO = executeIvr(clientTransDTO, relationUuid, satisfaction);
        String transName = satisfaction ? "转满意度" : "转IVR";
        if (executeLuaVO.getResult()) {
            if (satisfaction) {
                // 满意度录制 结束录制
                CompanyInfo companyInfo = commonDataOperateService.getCompanyInfoDTO(companyCode);
                if (!CommonConstant.ENABLE_Y.equals(companyInfo.getSatisfactionRecord())) {
                    String recordId = clientCallUuidContext.getRecordId();
                    StopRecordDTO stopRecordDTO = StopRecordDTO.build(companyCode, recordId, relationUuid);
                    freeswitchRequestService.stopRecord(stopRecordDTO);
                }
            }
            // lua脚本执行成功, 挂断坐席
            HangupDTO hangupDTO = HangupDTO.build(companyCode, uuid,
                    satisfaction ? HangupCauseEnum.TRANS_SATISFACTION : HangupCauseEnum.TRANS_IVR);
            freeswitchRequestService.hangup(hangupDTO);
            return ClientResponseBaseVO.response(clientTransDTO, "0", StrFormatter.format("{}成功!", transName));
        }
        return ClientResponseBaseVO.response(clientTransDTO, "1", StrFormatter.format("{}失败!", transName));
    }

    /**
     * 是否为转接满意度
     *
     * @param transTypeEnum 转接类型枚举
     * @return 是否为转接满意度
     */
    private boolean isSatisfaction(TransTypeEnum transTypeEnum) {
        return TransTypeEnum.TRANS_SATISFACTION.equals(transTypeEnum);
    }

    /**
     * 执行ivr-满意度
     */
    private ExecuteLuaVO executeIvr(ClientTransDTO clientTransDTO, String uuid, boolean satisfaction) {
        String companyCode = clientTransDTO.getCompanyCode();
        String ivrId = "";
        if (!satisfaction) {
            IvrServiceInfo ivrServiceInfo = commonDataOperateService.getIvrServiceInfo(clientTransDTO.getTransNumber());
            ivrId = ivrServiceInfo.getIvrId();
        }
        String ivrName = dataQueryService.getIvrName(companyCode, ivrId, satisfaction);
        ExecuteLuaDTO executeLuaDTO = ExecuteLuaDTO.build(clientTransDTO, uuid, ivrName);
        return freeswitchRequestService.trans2lua(executeLuaDTO);
    }

    /**
     * 咨询转uuid上下文
     *
     * @param clientTransDTO             转接请求参数
     * @param agentCallUuidContext       发起转接坐席uuid上下文
     * @param clientCallUuidContext      与原坐席通话的人员uuid上下文
     * @param originateDTO               外呼转接人员参数
     * @param callRoleEnum               呼叫角色
     * @param callTypeEnum               呼叫类型
     * @param isTransAgent               是否转接坐席
     * @param transCheckAgentAvailableVO 转接坐席的校验结果
     */
    private void writeConsultTransCallUuidContext(ClientTransDTO clientTransDTO,
                                                  CallUuidContext agentCallUuidContext,
                                                  CallUuidContext clientCallUuidContext,
                                                  OriginateDTO originateDTO,
                                                  CallRoleEnum callRoleEnum,
                                                  CallTypeEnum callTypeEnum,
                                                  boolean isTransAgent,
                                                  CheckAgentAvailableVO transCheckAgentAvailableVO) {
        // 保存通话uuid之间关联关系, 这步必须在通话事件来之前设置!
        CallUuidRelationDTO callUuidRelationDTO = new CallUuidRelationDTO();
        callUuidRelationDTO.setUuid(originateDTO.getOriUuid());
        callUuidRelationDTO.setMainCallId(agentCallUuidContext.getMainCallId());
        callUuidRelationDTO.setMainCdrFlag(false);
        callUuidRelationDTO.setRelationUuid(clientCallUuidContext.getUUID());
        // 发起坐席
        callUuidRelationDTO.setXferUUID(agentCallUuidContext.getUUID());
        callUuidRelationDTO.setCallRoleEnum(callRoleEnum);
        callUuidRelationDTO.setCallTypeEnum(callTypeEnum);
        callUuidRelationDTO.setCompanyCode(clientTransDTO.getCompanyCode());
        if (isTransAgent) {
            callUuidRelationDTO.setExtId(transCheckAgentAvailableVO.getExtStatusDTO().getExtId());
            callUuidRelationDTO.setExtIp(transCheckAgentAvailableVO.getExtStatusDTO().getExtIp());
            callUuidRelationDTO.setNumber(transCheckAgentAvailableVO.getAgentInfo().getSysExtId());
            callUuidRelationDTO.setAgentId(transCheckAgentAvailableVO.getAgentInfo().getSysAgentId());
            // 呼入属性
            callUuidRelationDTO.setVideo(clientTransDTO.getVideo());
            callUuidRelationDTO.setAudio(clientTransDTO.getAudio());
            callUuidRelationDTO.setCallInFlag(true);
            callUuidRelationDTO.setCallInChannel(CallInChannelEnum.TRANS);
        } else {
            callUuidRelationDTO.setNumber(clientTransDTO.getTransNumber());
        }
        callUuidRelationDTO.setReqId(clientTransDTO.getReqId());
        callUuidRelationDTO.setCallerNumber(originateDTO.getCallerNumber());
        callUuidRelationDTO.setDisplayNumber(originateDTO.getCallerNumber());
        callUuidRelationDTO.setCalleeNumber(originateDTO.getCalleeNumber());
        callUuidRelationDTO.setServerId(clientCallUuidContext.getServerId());
        callUuidRelationDTO.setOriginateAfterActionEnum(OriginateAfterActionEnum.XFER);
        callUuidRelationDTO.setXferActionEnum(XferActionEnum.TRANS);
        callUuidRelationDTO.setTransModeEnum(TransModeEnum.CONSULT_TRANS);
        callUuidRelationDTO.setOs(agentCallUuidContext.getCurrent().getOs());
        callUuidRelationDTO.setCallDirectionEnum(agentCallUuidContext.getCallDirection());
        callUuidRelationDTO.setAudio(originateDTO.getAudio());
        callUuidRelationDTO.setVideo(originateDTO.getVideo());
        callUuidRelationDTO.setRecordNode(RecordNodeEnum.CALL_OUT_B);
        CallCdrDTO callCdrDTO = new CallCdrDTO();
        callCdrDTO.setUuid(originateDTO.getOriUuid());
        callUuidRelationDTO.setCallCdrDTO(callCdrDTO);
        CallUuidContext callUuidContext = CallUuidContext.builder()
                .current(callUuidRelationDTO)
                .relationUuid(Sets.newHashSet(clientCallUuidContext.getUUID()))
                .build();
        dataStoreService.saveCallUuidContext(callUuidContext);

        // 与原坐席通话的人员uuid上下文-客户
        clientCallUuidContext.fillRelationUuidSet(originateDTO.getOriUuid());
        clientCallUuidContext.getCurrent().setRelationUuid(originateDTO.getOriUuid());
        dataStoreService.saveCallUuidContext(clientCallUuidContext);

        // 发起转接的坐席
        agentCallUuidContext.fillRelateUuidDtoByConsultTrans(originateDTO.getOriUuid());
        // 咨询转, 在桥接事件挂断自己
        agentCallUuidContext.getCurrent().setTransHangup(true);
        agentCallUuidContext.getCurrent().setHangupAll(false);
        dataStoreService.saveCallUuidContext(agentCallUuidContext);

        commonDataOperateService.saveCdrLink(clientTransDTO.getCompanyCode(), agentCallUuidContext.getMainCallId(),
                agentCallUuidContext.getUUID(), originateDTO.getOriUuid());
    }

    /**
     * 盲转uuid上下文
     *
     * @param clientTransDTO             转接请求参数
     * @param callUuidContext            发起转接坐席uuid上下文
     * @param callBridgeDTO              发起桥接参数
     * @param callRoleEnum               呼叫角色
     * @param callTypeEnum               呼叫类型
     * @param clientContext              与原坐席通话的人员uuid上下文
     * @param isTransAgent               是否转接坐席
     * @param transCheckAgentAvailableVO 转接坐席的校验结果
     */
    private void writeBlindTransCallUuidRelationDTO(ClientTransDTO clientTransDTO,
                                                    CallUuidContext callUuidContext,
                                                    CallBridgeDTO callBridgeDTO,
                                                    CallRoleEnum callRoleEnum,
                                                    CallTypeEnum callTypeEnum,
                                                    CallUuidContext clientContext,
                                                    boolean isTransAgent,
                                                    CheckAgentAvailableVO transCheckAgentAvailableVO) {
        CallUuidRelationDTO current = callUuidContext.getCurrent();
        CallUuidRelationDTO transCallUuidRelationDTO = new CallUuidRelationDTO();
        transCallUuidRelationDTO.setUuid(callBridgeDTO.getOriUuid());
        transCallUuidRelationDTO.setMainCallId(callUuidContext.getCurrent().getMainCallId());
        transCallUuidRelationDTO.setMainCdrFlag(false);
        transCallUuidRelationDTO.setRelationUuid(current.getRelationUuid());
        transCallUuidRelationDTO.setXferUUID(callUuidContext.getUUID());
        transCallUuidRelationDTO.setNumber(clientTransDTO.getTransNumber());
        // 待判定
        transCallUuidRelationDTO.setCallRoleEnum(callRoleEnum);
        transCallUuidRelationDTO.setCallTypeEnum(callTypeEnum);
        transCallUuidRelationDTO.setCompanyCode(clientTransDTO.getCompanyCode());
        if (isTransAgent) {
            transCallUuidRelationDTO.setExtId(transCheckAgentAvailableVO.getExtStatusDTO().getExtId());
            transCallUuidRelationDTO.setExtIp(transCheckAgentAvailableVO.getExtStatusDTO().getExtIp());
            transCallUuidRelationDTO.setNumber(transCheckAgentAvailableVO.getAgentInfo().getSysExtId());
            transCallUuidRelationDTO.setAgentId(transCheckAgentAvailableVO.getAgentInfo().getSysAgentId());
            // 呼入属性
            transCallUuidRelationDTO.setVideo(clientTransDTO.getVideo());
            transCallUuidRelationDTO.setAudio(clientTransDTO.getAudio());
            transCallUuidRelationDTO.setCallInFlag(true);
            transCallUuidRelationDTO.setCallInChannel(CallInChannelEnum.TRANS);
        } else {
            transCallUuidRelationDTO.setNumber(clientTransDTO.getTransNumber());
        }

        transCallUuidRelationDTO.setReqId(clientTransDTO.getReqId());
        transCallUuidRelationDTO.setCallerNumber(clientContext.getNumber());
        transCallUuidRelationDTO.setDisplayNumber(clientContext.getNumber());
        transCallUuidRelationDTO.setCalleeNumber(clientTransDTO.getTransNumber());
        transCallUuidRelationDTO.setServerId(current.getServerId());
        transCallUuidRelationDTO.setOriginateAfterActionEnum(OriginateAfterActionEnum.NONE);
        transCallUuidRelationDTO.setOs(callUuidContext.getCurrent().getOs());
        transCallUuidRelationDTO.setCallCdrDTO(new CallCdrDTO());
        transCallUuidRelationDTO.setCallDirectionEnum(callUuidContext.getCurrent().getCallDirectionEnum());
        transCallUuidRelationDTO.setAudio(callBridgeDTO.getAudio());
        transCallUuidRelationDTO.setVideo(callBridgeDTO.getVideo());
        transCallUuidRelationDTO.setRecordNode(RecordNodeEnum.CALL_OUT_B);
        // 转接uuid上下文
        CallUuidContext transCallUuidContext = CallUuidContext.builder()
                .current(transCallUuidRelationDTO)
                // 客户, 保存关联坐席uuid
                .relationUuid(Sets.newHashSet(current.getRelationUuid()))
                .build();
        dataStoreService.saveCallUuidContext(transCallUuidContext);

        // 客户上下文
        clientContext.getCurrent().setRelationUuid(callBridgeDTO.getOriUuid());
        dataStoreService.saveCallUuidContext(clientContext);

        // 发起转接的坐席
        callUuidContext.fillRelateUuidDtoByBlindTrans(callBridgeDTO.getOriUuid());
        callUuidContext.getCurrent().setHangupAll(false);
        dataStoreService.saveCallUuidContext(callUuidContext);

        commonDataOperateService.saveCdrLink(clientTransDTO.getCompanyCode(), callUuidContext.getMainCallId(),
                callUuidContext.getUUID(), callBridgeDTO.getOriUuid());
    }

}
