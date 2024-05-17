package com.cqt.hmyc.web.bind.service.strategy.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.common.enums.ModelEnum;
import com.cqt.common.util.IvrUtil;
import com.cqt.hmyc.web.bind.service.ax.AxBindConverter;
import com.cqt.hmyc.web.bind.service.ax.AxBindService;
import com.cqt.hmyc.web.bind.service.strategy.BindInfoQueryService;
import com.cqt.hmyc.web.bind.service.strategy.BindInfoQueryStrategy;
import com.cqt.hmyc.web.blacklist.service.CallerNumberBlacklistService;
import com.cqt.hmyc.web.cache.LocalCacheService;
import com.cqt.model.bind.ax.entity.PrivateBindInfoAx;
import com.cqt.model.bind.query.BindInfoQuery;
import com.cqt.model.bind.vo.BindInfoVO;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2021/10/25 11:19
 * AX 绑定关系查询
 */
@Service
@Slf4j
@AllArgsConstructor
public class AxBindInfoQueryStrategyImpl implements BindInfoQueryStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AX.name();

    private final AxBindConverter bindConverter;

    private final BindInfoQueryService bindInfoQueryService;

    private final HideProperties hideProperties;

    private final AxBindService axBindService;

    private final CallerNumberBlacklistService callerNumberBlacklistService;

    @Override
    public Optional<BindInfoVO> query(BindInfoQuery bindInfoQuery, Optional<PrivateCorpBusinessInfoDTO> corpBusinessInfoOptional) {
        String vccId = bindInfoQuery.getVccId();
        String caller = bindInfoQuery.getCaller();

        //X号码的绑定关系
        Optional<PrivateBindInfoAx> optional = bindInfoQueryService.getAxBindInfo(vccId, BUSINESS_TYPE, bindInfoQuery.getCalled());
        if (!optional.isPresent()) {
            return Optional.empty();
        }
        PrivateBindInfoAx bindInfoAx = optional.get();
        String telA = bindInfoAx.getTelA();
        // 来电黑名单校验
        boolean isInBlacklist = callerNumberBlacklistService.checker(vccId, BUSINESS_TYPE, caller, telA);
        if (isInBlacklist) {
            log.info("AX, vccId: {}, 来电黑名单校验, 主叫: {}, 被叫: {}", vccId, caller, telA);
            String notBindIvr = bindInfoQueryService.getNotBindIvr(corpBusinessInfoOptional);
            return Optional.of(new BindInfoVO(ErrorCodeEnum.CALLER_IN_BLACKLIST.getCode(), ErrorCodeEnum.CALLER_IN_BLACKLIST.getMessage(), notBindIvr));
        }

        // B号码为106 10010.. 拒绝呼叫
        if (!telA.equals(caller)) {
            String industrySmsNumberRegex = hideProperties.getIndustrySmsNumberRegex();
            if (ReUtil.isMatch(industrySmsNumberRegex, caller)) {
                return Optional.empty();
            }
        }

        return getBindInfoVO(bindInfoQuery, bindInfoAx);
    }

    private Optional<BindInfoVO> getBindInfoVO(BindInfoQuery bindInfoQuery, PrivateBindInfoAx bindInfoAx) {

        String telA = bindInfoAx.getTelA();
        String telB = bindInfoAx.getTelB();
        String caller = bindInfoQuery.getCaller();
        String calledNum;
        String callerIvr;
        String calledIvr;
        String callerIvrBefore;
        String displayNum = bindInfoAx.getTelX();
        // 主叫是A号码, 找被叫B, 没有B返回异常音
        if (telA.equals(caller)) {
            if (StrUtil.isEmpty(telB)) {
                return Optional.empty();
            }
            calledNum = telB;
            callerIvr = bindInfoAx.getAudioACallX();
            calledIvr = bindInfoAx.getAudioBCalledX();
            callerIvrBefore = bindInfoAx.getAudioACallXBefore();
        } else {
            calledNum = telA;
            callerIvr = bindInfoAx.getAudioBCallX();
            calledIvr = bindInfoAx.getAudioACalledX();
            callerIvrBefore = bindInfoAx.getAudioBCallXBefore();
            if (!caller.equals(telB)) {
                if (ObjectUtil.isEmpty(bindInfoQuery.getIndustrySms()) || bindInfoQuery.getIndustrySms() != 1) {
                    bindInfoQueryService.setupTelB(bindInfoQuery, bindInfoAx);
                }
            }
            displayNum = getDisplayNum(bindInfoAx, caller);
        }
        BindInfoVO bindInfoVO = bindConverter.bindInfoAx2BindInfoVO(bindInfoAx);
        bindInfoVO.setCode(0);
        bindInfoVO.setMessage("AX查询成功!");
        bindInfoVO.setCalledNum(calledNum);
        bindInfoVO.setCalledIvr(IvrUtil.getIvrName(calledIvr, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setCallerIvr(IvrUtil.getIvrName(callerIvr, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setCallerIvrBefore(IvrUtil.getIvrName(callerIvrBefore, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setDisplayNum(displayNum);
        bindInfoVO.setCallNum(bindInfoQuery.getCalled());
        bindInfoVO.setNumType(BUSINESS_TYPE);
        bindInfoVO.setTransferData(bindInfoQueryService.removeAudio(bindInfoAx));

        return Optional.of(bindInfoVO);
    }

    private String getDisplayNum(PrivateBindInfoAx bindInfoAx, String caller) {
        // 真实号码
        if (ModelEnum.REAL_TEL.getCode().equals(bindInfoAx.getModel())) {
            return caller;
        }

        // 系统随机分配虚号Y
        if (ModelEnum.TEL_Y.getCode().equals(bindInfoAx.getModel())) {
            String randomDistributeY = randomDistributeY(bindInfoAx.getVccId(), bindInfoAx.getAreaCode());
            if (Objects.nonNull(randomDistributeY)) {
                return randomDistributeY;
            }
        }

        // tel_x, 默认值
        return bindInfoAx.getTelX();
    }

    private String randomDistributeY(String vccId, String areaCode) {

        return axBindService.randomDistributeY(vccId, areaCode);
    }

    @Override
    public String getBusinessType() {

        return BUSINESS_TYPE;
    }
}
