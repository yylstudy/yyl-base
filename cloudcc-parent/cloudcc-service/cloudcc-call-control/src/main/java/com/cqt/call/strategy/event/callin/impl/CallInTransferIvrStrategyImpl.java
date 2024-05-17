package com.cqt.call.strategy.event.callin.impl;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.IdUtil;
import com.cqt.base.contants.SystemConstant;
import com.cqt.base.enums.CallInStrategyEnum;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.base.enums.cdr.ReleaseDirEnum;
import com.cqt.call.strategy.event.callin.CallInEventStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.freeswitch.dto.api.CallIvrLuaDTO;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.dto.event.CallInEventDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.cqt.model.number.entity.NumberInfo;
import com.cqt.model.queue.entity.IvrServiceInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-07-21 10:20
 * 呼入转IVR
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallInTransferIvrStrategyImpl implements CallInEventStrategy {

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    private final ObjectMapper objectMapper;

    @Override
    public Boolean execute(CallInEventDTO callInEventDTO, NumberInfo numberInfo) {
        try {
            String uuid = callInEventDTO.getUuid();
            String companyCode = callInEventDTO.getCompanyCode();
            // 要执行的ivr服务id
            String serviceId = numberInfo.getServiceId();
            // 查询ivr服务配置
            IvrServiceInfo ivrServiceInfo = commonDataOperateService.getIvrServiceInfo(serviceId);
            if (Objects.isNull(ivrServiceInfo)) {
                CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
                callUuidContext.getCurrent().getCallCdrDTO().setHangupCauseEnum(HangupCauseEnum.NUMBER_SERVICE_INVALID);
                callUuidContext.getCurrent().setReleaseDir(ReleaseDirEnum.PLATFORM);
                commonDataOperateService.saveCallUuidContext(callUuidContext);
                // 未查询到号码服务方式配置, 直接挂断hangup
                HangupDTO hangupDTO = HangupDTO.build(companyCode, uuid, HangupCauseEnum.NUMBER_SERVICE_INVALID);
                freeswitchRequestService.hangup(hangupDTO);
                log.info("[呼入策略-转IVR] 企业id: {}, uuid: {}, 未查询到号码服务方式配置, 直接挂断", companyCode, uuid);
                return false;
            }
            String luaName = StrFormatter.format(SystemConstant.LUA_TEMPLATE, ivrServiceInfo.getIvrId());
            log.info("[呼入转IVR] 企业: {}, 执行的IVR: {}", companyCode, luaName);
            // 调用底层 执行lua
            CallIvrLuaDTO ivrLuaDTO = CallIvrLuaDTO.build(IdUtil.fastUUID(), companyCode, uuid, luaName);
            FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.callIvrLua(ivrLuaDTO);
            log.info("[呼入策略-转IVR] 企业id: {}, uuid: {}, 执行Lua完成.", companyCode, uuid);
            return freeswitchApiVO.getResult();
        } catch (Exception e) {
            log.error("[呼入-转ivr] 异常: ", e);
        }
        return false;
    }

    @Override
    public CallInStrategyEnum getCallInStrategy() {
        return CallInStrategyEnum.IVR;
    }
}
