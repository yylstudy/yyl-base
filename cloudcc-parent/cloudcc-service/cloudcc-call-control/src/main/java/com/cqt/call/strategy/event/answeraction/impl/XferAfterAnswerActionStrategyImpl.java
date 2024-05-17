package com.cqt.call.strategy.event.answeraction.impl;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.IdUtil;
import com.cqt.base.enums.*;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.event.answeraction.AfterAnswerActionStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.client.vo.ClientCallbackVO;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.dto.api.XferDTO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-10-20 9:32
 * 接通后 咨询、转接、三方通话、耳语、监听 xfer等接通事件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XferAfterAnswerActionStrategyImpl implements AfterAnswerActionStrategy {

    private final CommonDataOperateService commonDataOperateService;

    private final DataStoreService dataStoreService;

    private final FreeswitchRequestService freeswitchRequestService;

    @Override
    public OriginateAfterActionEnum getOriginateAfterAction() {
        return OriginateAfterActionEnum.XFER;
    }

    @Override
    public void execute(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        xferAgent(callStatusEventDTO, callUuidContext);
    }

    /**
     * xfer操作
     *
     * @param callStatusEventDTO 通话事件消息
     * @param callUuidContext    当前事件 uuid上下文
     */
    private void xferAgent(CallStatusEventDTO callStatusEventDTO, CallUuidContext callUuidContext) {
        String companyCode = callStatusEventDTO.getCompanyCode();
        XferDTO xferDTO = new XferDTO();
        String reqId = IdUtil.fastSimpleUUID();
        xferDTO.setReqId(reqId);
        xferDTO.setCompanyCode(companyCode);
        xferDTO.setServerId(callStatusEventDTO.getServerId());
        XferActionEnum xferActionEnum = callUuidContext.getXferAction();
        xferDTO.setXferAction(xferActionEnum.getName());
        boolean isAdminOperate = XferActionEnum.EAVESDROP.equals(xferActionEnum) || XferActionEnum.WHISPER.equals(xferActionEnum);
        if (isAdminOperate) {
            // 管理员
            xferDTO.setUuid(callStatusEventDTO.getUuid());
            xferDTO.setXUuid(callUuidContext.getXferUUID());
        } else {
            // 发起xfer的坐席
            xferDTO.setUuid(callUuidContext.getXferUUID());
            // 咨询的人(坐席/外线)
            xferDTO.setXUuid(callStatusEventDTO.getUuid());
        }

        commonDataOperateService.saveCallUuidContext(callUuidContext);
        // 发起xfer或被xfer的坐席
        checkXferCdrLink(callStatusEventDTO, callUuidContext, xferActionEnum, xferDTO);

        boolean xferSuccess = false;
        String resultMsg = xferActionEnum.getDesc() + "成功!";
        try {
            setConsultFlag(xferActionEnum, callStatusEventDTO.getUuid());
            FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.xfer(xferDTO);
            log.info("[接通事件-xfer] 调用成功. 企业: {}, xfer_action: {}", xferDTO.getCompanyCode(), xferDTO.getXferAction());
            if (freeswitchApiVO.getResult()) {
                xferSuccess = true;
            } else {
                resultMsg = freeswitchApiVO.getMsg();
            }
        } catch (Exception e) {
            log.error("[接通事件-xfer] xferDTO: {}, 调用异常: ", xferDTO, e);
        }
        if (!xferSuccess) {
            HangupDTO hangupDTO = HangupDTO.build(companyCode, callStatusEventDTO.getUuid(), HangupCauseEnum.XFER_FAIL);
            // 桥接失败挂断当前坐席通话
            freeswitchRequestService.hangup(hangupDTO);
        }
        // 发起xfer结果回调
        xferCallback(callUuidContext, xferSuccess, resultMsg);
    }

    /**
     * xfer 保存话单连接
     *
     * @param callStatusEventDTO 通话事件消息
     * @param callUuidContext    当前事件 uuid上下文
     * @param xferActionEnum     xfer类型
     * @param xferDTO            xfer接口参数
     */
    private void checkXferCdrLink(CallStatusEventDTO callStatusEventDTO,
                                  CallUuidContext callUuidContext,
                                  XferActionEnum xferActionEnum,
                                  XferDTO xferDTO) {
        String startXferUUID = callUuidContext.getCurrent().getRelationUuid();
        String companyCode = callStatusEventDTO.getCompanyCode();
        // 发起xfer或被xfer的坐席上下文
        CallUuidContext agentCallUuidContext = commonDataOperateService.getCallUuidContext(companyCode, startXferUUID);
        agentCallUuidContext.getRelationUuid().add(callStatusEventDTO.getUuid());
        String mainCallId = callUuidContext.getMainCallId();
        commonDataOperateService.saveCallUuidContext(agentCallUuidContext);
        switch (xferActionEnum) {
            case TRANS:
                String sourceUUID = callUuidContext.getUUID();
                String destUUId = callUuidContext.getCurrent().getRelationUuid();
                if (CallDirectionEnum.INBOUND.equals(callUuidContext.getCallDirection())) {
                    commonDataOperateService.saveCdrLink(companyCode, mainCallId, destUUId, sourceUUID);
                } else {
                    commonDataOperateService.saveCdrLink(companyCode, mainCallId, sourceUUID, destUUId);
                }
                return;
            case FORCE_CALL:
            case THREE_WAY:
            case CONSULT:
                commonDataOperateService.saveCdrLink(companyCode, mainCallId, xferDTO.getUuid(), xferDTO.getXUuid());
                return;
            case EAVESDROP:
            case WHISPER:
                commonDataOperateService.saveCdrLink(companyCode, mainCallId, xferDTO.getXUuid(), xferDTO.getUuid());
                return;
            default:
                break;
        }
    }

    /**
     * 咨询标志, 在咨询中转接使用
     *
     * @param xferActionEnum xfer类型
     * @param uuid           被咨询方uuid
     */
    private void setConsultFlag(XferActionEnum xferActionEnum, String uuid) {
        if (XferActionEnum.CONSULT.equals(xferActionEnum)) {
            commonDataOperateService.saveConsultFlag(uuid);
        }
    }

    /**
     * xfer开始回调通知
     *
     * @param callUuidContext uuid上下文
     */
    private void xferCallback(CallUuidContext callUuidContext, Boolean xferSuccess, String msg) {
        String companyCode = callUuidContext.getCompanyCode();
        String xferUUID = callUuidContext.getXferUUID();
        CallRoleEnum callRoleEnum = callUuidContext.getCallRoleEnum();
        CallUuidContext xferContext = commonDataOperateService.getCallUuidContext(callUuidContext.getCompanyCode(), xferUUID);
        log.info("[接通事件]  企业: {}, 坐席角色: {}, 开始xfer,回调通知SDK", companyCode, callRoleEnum);
        CallbackActionEnum callbackActionEnum = null;
        boolean notifyPassive = true;
        boolean admin = false;
        switch (callRoleEnum) {
            case WHISPER_AGENT:
                admin = true;
                callbackActionEnum = CallbackActionEnum.WHISPER;
                break;
            case EAVESDROP_AGENT:
                admin = true;
                callbackActionEnum = CallbackActionEnum.EAVESDROP;
                break;
            case TRANS_CLIENT:
                notifyPassive = false;
                callbackActionEnum = CallbackActionEnum.TRANS;
                break;
            case TRANS_AGENT:
                callbackActionEnum = CallbackActionEnum.TRANS;
                break;
            case CONSULT_CLIENT:
                notifyPassive = false;
                callbackActionEnum = CallbackActionEnum.CONSULT;
                break;
            case CONSULT_AGENT:
                callbackActionEnum = CallbackActionEnum.CONSULT;
                break;
            case THREE_WAY_CLIENT:
                notifyPassive = false;
                callbackActionEnum = CallbackActionEnum.THREE_WAY;
                break;
            case THREE_WAY_AGENT:
                callbackActionEnum = CallbackActionEnum.THREE_WAY;
                break;
            case FORCE_CALL_AGENT:
                admin = true;
                callbackActionEnum = CallbackActionEnum.FORCE_CALL;
                break;
            default:
        }
        XferActionEnum xferAction = callUuidContext.getXferAction();
        if (Objects.nonNull(callbackActionEnum)) {
            if (admin) {
                // 通知主动方
                dataStoreService.notifyClient(ClientCallbackVO.buildInit(callUuidContext, callbackActionEnum, xferSuccess, msg));
                // 通知被动方
                if (xferSuccess) {
                    String resultMsg = StrFormatter.format("正在被{}!", xferAction.getDesc());
                    dataStoreService.notifyClient(ClientCallbackVO.buildInit(xferContext, callbackActionEnum, true, resultMsg));
                }
                return;
            }
            // 通知主动方
            dataStoreService.notifyClient(ClientCallbackVO.buildInit(xferContext, callbackActionEnum, xferSuccess, msg));
            // 通知被动方
            if (xferSuccess && notifyPassive) {
                String resultMsg = StrFormatter.format("正在被{}!", xferAction.getDesc());
                dataStoreService.notifyClient(ClientCallbackVO.buildInit(callUuidContext, callbackActionEnum, true, resultMsg));
            }
        }
    }
}
