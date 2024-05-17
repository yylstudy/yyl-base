package com.cqt.hmyc.web.bind.service.strategy.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.ModelEnum;
import com.cqt.common.util.IvrUtil;
import com.cqt.hmyc.web.bind.service.axb.AxbBindConverter;
import com.cqt.hmyc.web.bind.service.strategy.BindInfoQueryService;
import com.cqt.hmyc.web.bind.service.strategy.BindInfoQueryStrategy;
import com.cqt.hmyc.web.cache.LocalCacheService;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.query.BindInfoQuery;
import com.cqt.model.bind.vo.BindInfoVO;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2021/10/25 11:19
 * axb 绑定关系查询
 */
@Service
@Slf4j
@AllArgsConstructor
public class AxbBindInfoQueryStrategyImpl implements BindInfoQueryStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AXB.name();

    private final BindInfoQueryService bindInfoQueryService;

    private final AxbBindConverter bindConverter;

    @Override
    public Optional<BindInfoVO> query(BindInfoQuery bindInfoQuery, Optional<PrivateCorpBusinessInfoDTO> corpBusinessInfoOptional) {

        Optional<PrivateBindInfoAxb> axbOptional = bindInfoQueryService.getAxbBindInfo(bindInfoQuery.getVccId(), BUSINESS_TYPE, bindInfoQuery.getCalled(), bindInfoQuery.getCaller());
        return axbOptional.map(bindInfoAxb -> getBindInfoByAxb(bindInfoAxb, bindInfoQuery));
    }

    private BindInfoVO getBindInfoByAxb(PrivateBindInfoAxb infoAxb, BindInfoQuery bindInfoQuery) {
        String caller = bindInfoQuery.getCaller();
        String telA = infoAxb.getTelA();
        String telB = infoAxb.getTelB();
        String calledNum;
        String callerIvr;
        String calledIvr;
        String callerIvrBefore;
        if (caller.equals(telA)) {
            calledNum = telB;
            callerIvr = infoAxb.getAudioACallX();
            calledIvr = infoAxb.getAudioBCalledX();
            callerIvrBefore = infoAxb.getAudioACallXBefore();
        } else {
            calledNum = telA;
            callerIvr = infoAxb.getAudioBCallX();
            calledIvr = infoAxb.getAudioACalledX();
            callerIvrBefore = infoAxb.getAudioBCallXBefore();
        }
        String displayNum = bindInfoQuery.getCalled();
        if (ObjectUtil.isNotEmpty(infoAxb.getModel()) && ModelEnum.REAL_TEL.getCode().equals(infoAxb.getModel())) {
            displayNum = caller;
        }
        BindInfoVO bindInfoVO = bindConverter.bindInfoAxb2BindInfoVO(infoAxb);
        bindInfoVO.setCode(0);
        bindInfoVO.setMessage("AXB查询成功!");
        bindInfoVO.setCalledNum(calledNum);
        bindInfoVO.setCalledIvr(IvrUtil.getIvrName(calledIvr, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setCallerIvr(IvrUtil.getIvrName(callerIvr, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setCallerIvrBefore(IvrUtil.getIvrName(callerIvrBefore, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setDisplayNum(displayNum);
        bindInfoVO.setCallNum(bindInfoQuery.getCalled());
        bindInfoVO.setNumType(BUSINESS_TYPE);
        bindInfoVO.setTransferData(bindInfoQueryService.removeAudio(infoAxb));

        if (StrUtil.isNotEmpty(infoAxb.getSourceBindId())) {
            String axeTelX = infoAxb.getAxeybTelX();
            Integer aybOtherShow = infoAxb.getAybOtherShow();
            if (aybOtherShow == 2) {
                bindInfoVO.setDisplayNum(axeTelX);
            }
            if (aybOtherShow == 1) {
                // Y号码
                bindInfoVO.setDisplayNum(bindInfoQuery.getCalled());
            }
        }

        return bindInfoVO;
    }

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }

}
