package com.cqt.call.strategy.event.callin.impl;

import com.cqt.base.enums.CallInIvrActionEnum;
import com.cqt.base.enums.CallInStrategyEnum;
import com.cqt.base.enums.MediaStreamEnum;
import com.cqt.base.model.ResultVO;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.event.callin.CallInEventStrategy;
import com.cqt.model.freeswitch.dto.event.CallInEventDTO;
import com.cqt.model.number.entity.NumberInfo;
import com.cqt.model.queue.dto.CallInIvrActionDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-07-21 10:20
 * 呼入转技能(人工服务)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallInTransferSkillStrategyImpl implements CallInEventStrategy {

    private final ObjectMapper objectMapper;

    private final DataStoreService dataStoreService;

    @Override
    public Boolean execute(CallInEventDTO callInEventDTO, NumberInfo numberInfo) throws Exception {
        String companyCode = callInEventDTO.getCompanyCode();
        String callerNumber = callInEventDTO.getData().getCallerNumber();
        // 查询技能信息
        String skillId = numberInfo.getServiceId();
        // 转技能
        CallInIvrActionDTO callInIvrActionDTO = new CallInIvrActionDTO();
        callInIvrActionDTO.setCompanyCode(companyCode);
        callInIvrActionDTO.setUuid(callInEventDTO.getUuid());
        callInIvrActionDTO.setCallerNumber(callerNumber);
        callInIvrActionDTO.setCurrentTimes(1);
        callInIvrActionDTO.setSkillId(skillId);
        callInIvrActionDTO.setType(CallInIvrActionEnum.TRANS_SKILl.getCode());
        callInIvrActionDTO.setAudio(MediaStreamEnum.valueOf(callInEventDTO.getData().getAudio()).getCode());
        callInIvrActionDTO.setVideo(MediaStreamEnum.valueOf(callInEventDTO.getData().getVideo()).getCode());
        if (log.isInfoEnabled()) {
            log.info("企业: {}, 来电号码: {}, 技能: {}, 呼入转技能, 参数: {}",
                    companyCode, callerNumber, skillId, objectMapper.writeValueAsString(callInIvrActionDTO));
        }
        ResultVO<CallInIvrActionVO> resultVO = dataStoreService.distributeAgent(callInIvrActionDTO);
        if (log.isInfoEnabled()) {
            log.info("企业: {}, 来电号码: {}, 技能: {}, 呼入转技能, 结果: {}",
                    companyCode, callerNumber, skillId, objectMapper.writeValueAsString(resultVO));
        }
        return resultVO.success();
    }

    @Override
    public CallInStrategyEnum getCallInStrategy() {
        return CallInStrategyEnum.SKILL;
    }
}
