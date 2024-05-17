package com.cqt.hmyc.web.bind.service.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.util.IvrUtil;
import com.cqt.hmyc.web.bind.service.axbn.AxbnBindConverter;
import com.cqt.hmyc.web.bind.service.strategy.BindInfoQueryService;
import com.cqt.hmyc.web.bind.service.strategy.BindInfoQueryStrategy;
import com.cqt.hmyc.web.cache.LocalCacheService;
import com.cqt.model.bind.axbn.entity.PrivateBindInfoAxbn;
import com.cqt.model.bind.query.BindInfoQuery;
import com.cqt.model.bind.vo.BindInfoVO;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2021/10/25 11:19
 * axbn 绑定关系查询
 */
@Service
@Slf4j
@AllArgsConstructor
public class AxbnBindInfoQueryStrategyImpl implements BindInfoQueryStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AXBN.name();

    private final BindInfoQueryService bindInfoQueryService;

    private final AxbnBindConverter bindConverter;

    @Override
    public Optional<BindInfoVO> query(BindInfoQuery bindInfoQuery, Optional<PrivateCorpBusinessInfoDTO> corpBusinessInfoOptional) {

        Optional<PrivateBindInfoAxbn> axbOptional = bindInfoQueryService.getAxbnBindInfo(bindInfoQuery.getVccId(), BUSINESS_TYPE,
                bindInfoQuery.getCalled(), bindInfoQuery.getCaller());
        return axbOptional.map(bindInfoAxbn -> getBindInfoByAxb(bindInfoAxbn, bindInfoQuery));
    }

    private BindInfoVO getBindInfoByAxb(PrivateBindInfoAxbn infoAxbn, BindInfoQuery bindInfoQuery) {
        String caller = bindInfoQuery.getCaller();
        String called = bindInfoQuery.getCalled();
        String telA = infoAxbn.getTelA();
        String telB = infoAxbn.getTelB();
        String telY = infoAxbn.getTelY();
        String telX = infoAxbn.getTelX();
        String otherTelB = infoAxbn.getOtherTelB();
        String calledNum = "";
        String callerIvr = "";
        String calledIvr = "";
        String callerIvrBefore = "";
        // 呼叫号码是X号码
        if (telX.equals(called)) {
            if (caller.equals(telA)) {
                calledNum = telB;
            }
            if (caller.equals(telB) || otherTelB.contains(caller)) {
                calledNum = telA;
                callerIvr = infoAxbn.getAudioBCallX();
            }
        }
        if (telY.contains(called)) {
            callerIvr = infoAxbn.getAudioACallX();
            Map<String, String> otherByMap = JSON.parseObject(infoAxbn.getOtherBy(), Map.class);
            for (Map.Entry<String, String> entry : otherByMap.entrySet()) {
                if (called.equals(entry.getValue())) {
                    calledNum = entry.getKey();
                    break;
                }
            }
        }
        BindInfoVO bindInfoVO = bindConverter.bindInfoAxbn2BindInfoVO(infoAxbn);
        bindInfoVO.setCode(0);
        bindInfoVO.setMessage("AXBN查询成功!");
        bindInfoVO.setCalledNum(calledNum);
        bindInfoVO.setCalledIvr(IvrUtil.getIvrName(calledIvr, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setCallerIvr(IvrUtil.getIvrName(callerIvr, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setCallerIvrBefore(IvrUtil.getIvrName(callerIvrBefore, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setDisplayNum(infoAxbn.getDisplayNumber());
        bindInfoVO.setCallNum(bindInfoQuery.getCalled());
        bindInfoVO.setNumType(BUSINESS_TYPE);
        bindInfoVO.setTransferData(infoAxbn);

        return bindInfoVO;
    }

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }

}
