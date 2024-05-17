package com.cqt.hmyc.web.bind.service.strategy.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.common.enums.NumberTypeEnum;
import com.cqt.common.util.IvrUtil;
import com.cqt.hmyc.web.bind.service.axebn.AxebnBindConverter;
import com.cqt.hmyc.web.bind.service.strategy.BindInfoQueryService;
import com.cqt.hmyc.web.bind.service.strategy.BindInfoQueryStrategy;
import com.cqt.hmyc.web.cache.LocalCacheService;
import com.cqt.model.bind.axebn.dto.AxebnExtBindInfoDTO;
import com.cqt.model.bind.axebn.entity.PrivateBindInfoAxebn;
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
 * AXEBN 绑定关系查询
 * AXEBN此模式 废弃
 */
@Service
@Slf4j
@AllArgsConstructor
@Deprecated
public class AxebnBindInfoQueryStrategyImpl implements BindInfoQueryStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AXEBN.name();

    private final AxebnBindConverter axebnBindConverter;

    private final BindInfoQueryService bindInfoQueryService;

    @Override
    public Optional<BindInfoVO> query(BindInfoQuery bindInfoQuery, Optional<PrivateCorpBusinessInfoDTO> corpBusinessInfoOptional) {
        String digitInfo = bindInfoQuery.getDigitInfo();
        String vccId = bindInfoQuery.getVccId();
        String called = bindInfoQuery.getCalled();
        String caller = bindInfoQuery.getCaller();
        /*
         * 1. 没传分机号 查询ax/bx, 有绑定关系,则主叫是B 直接用X 打A
         * 2. 无绑定关系, 主叫是A, 需通知传分机号, 返回XE
         * 3. 传了分机号, 查询XE, 查不到 返回XE, 查到返回结果
         */

        Optional<PrivateBindInfoAxebn> infoAxebnOptional = bindInfoQueryService.getAxebnBindInfo(vccId, BUSINESS_TYPE, caller, called);
        if (infoAxebnOptional.isPresent()) {
            // 存在 主叫是B, 用X打A
            PrivateBindInfoAxebn bindInfoAxebn = infoAxebnOptional.get();

            return getBindInfoVoByTelB(bindInfoAxebn);
        }

        // 主叫是tel_a
        if (StrUtil.isEmpty(digitInfo)) {
            BindInfoVO bindInfoVO = BindInfoVO.builder()
                    .code(0)
                    .message("AXEBN第一次查询!")
                    .callerIvr(bindInfoQueryService.getDigitsIvr(corpBusinessInfoOptional))
                    .numType(NumberTypeEnum.XE.name())
                    .build();
            return Optional.of(bindInfoVO);
        }

        Optional<AxebnExtBindInfoDTO> extBindInfoDtoOptional = bindInfoQueryService.getAxebnExtBindInfo(vccId, BUSINESS_TYPE, called, digitInfo);
        String notBindIvr = bindInfoQueryService.getNotBindIvr(corpBusinessInfoOptional);
        if (!extBindInfoDtoOptional.isPresent()) {
            return Optional.of(new BindInfoVO(ErrorCodeEnum.EXT_NUM_NOT_VALID.getCode(),
                    ErrorCodeEnum.EXT_NUM_NOT_VALID.getMessage(), notBindIvr, NumberTypeEnum.XE.name()));
        }
        AxebnExtBindInfoDTO axebnExtBindInfoDTO = extBindInfoDtoOptional.get();

        return getBindInfoVoByTelA(axebnExtBindInfoDTO);
    }

    private Optional<BindInfoVO> getBindInfoVoByTelA(AxebnExtBindInfoDTO axebnExtBindInfoDTO) {
        BindInfoVO bindInfoVO = axebnBindConverter.axebnExtBindInfoDTO2bindInfoVO(axebnExtBindInfoDTO);
        bindInfoVO.setCode(0);
        bindInfoVO.setMessage("tel_a为主叫, 查询AXEBN成功!");
        bindInfoVO.setNumType(NumberTypeEnum.AXEBN_AXE.name());
        bindInfoVO.setCalledNum(axebnExtBindInfoDTO.getBindTelB());
        bindInfoVO.setDisplayNum(axebnExtBindInfoDTO.getTelX());
        bindInfoVO.setCallNum(axebnExtBindInfoDTO.getTelX());
        bindInfoVO.setExtNum(axebnExtBindInfoDTO.getBindExtNum());
        bindInfoVO.setCallerIvr("");
        bindInfoVO.setCalledIvr("");
        bindInfoVO.setTransferData(axebnExtBindInfoDTO);
        return Optional.of(bindInfoVO);
    }

    private Optional<BindInfoVO> getBindInfoVoByTelB(PrivateBindInfoAxebn bindInfoAxebn) {
        BindInfoVO bindInfoVO = axebnBindConverter.bindInfoAxebn2bindInfoVO(bindInfoAxebn);
        bindInfoVO.setCode(0);
        bindInfoVO.setMessage("tel_b为主叫, 查询AXEBN成功!");
        bindInfoVO.setNumType(NumberTypeEnum.AXEBN_AXB.name());
        bindInfoVO.setDisplayNum(bindInfoAxebn.getTelX());
        bindInfoVO.setCallNum(bindInfoAxebn.getTelX());
        bindInfoVO.setCalledNum(bindInfoAxebn.getTelA());
        String callerIvr = Convert.toStr(bindInfoAxebn.getAudio(), "");
        String calledIvr = Convert.toStr(bindInfoAxebn.getAudioCalled(), "");
        bindInfoVO.setCallerIvr(IvrUtil.getIvrName(callerIvr, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setCalledIvr(IvrUtil.getIvrName(calledIvr, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setTransferData(bindInfoAxebn);
        return Optional.of(bindInfoVO);
    }

    @Override
    public String getBusinessType() {

        return BUSINESS_TYPE;
    }
}
