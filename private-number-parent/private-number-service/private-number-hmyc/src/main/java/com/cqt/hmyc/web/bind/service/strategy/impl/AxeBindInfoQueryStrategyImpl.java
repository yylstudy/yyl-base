package com.cqt.hmyc.web.bind.service.strategy.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.enums.*;
import com.cqt.common.util.IvrUtil;
import com.cqt.hmyc.web.bind.service.axb.AxbBindConverter;
import com.cqt.hmyc.web.bind.service.axe.AxeAsyncService;
import com.cqt.hmyc.web.bind.service.axe.AxeBindConverter;
import com.cqt.hmyc.web.bind.service.push.BindPushService;
import com.cqt.hmyc.web.bind.service.strategy.BindInfoQueryService;
import com.cqt.hmyc.web.bind.service.strategy.BindInfoQueryStrategy;
import com.cqt.hmyc.web.cache.LocalCacheService;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axb.vo.AxbBindingVO;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.query.BindInfoQuery;
import com.cqt.model.bind.vo.BindInfoVO;
import com.cqt.model.common.Result;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2021/10/25 11:19
 * AXE 绑定关系查询
 * AEXYB 和AXE 合并成为AXE
 */
@Service
@Slf4j
@AllArgsConstructor
public class AxeBindInfoQueryStrategyImpl implements BindInfoQueryStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AXE.name();

    private final BindInfoQueryService bindInfoQueryService;

    private final AxbBindConverter axbBindConverter;

    private final AxeBindConverter axeBindConverter;

    private final BindPushService bindPushService;

    private final AxeAsyncService axeAsyncService;

    @Override
    public Optional<BindInfoVO> query(BindInfoQuery bindInfoQuery, Optional<PrivateCorpBusinessInfoDTO> corpBusinessInfoOptional) {
        String digitInfo = bindInfoQuery.getDigitInfo();

        if (StrUtil.isEmpty(digitInfo)) {
            // 判断AX是否存在绑定关系, 且是否存在tel_b, 满足返回AX绑定关系, AXE
            // 否则走正常AXE流程, 分机号输入正确后设置B号码
            Optional<PrivateBindInfoAxe> axeBindInfoOfAxOptional = bindInfoQueryService.getAxeBindInfoOfAx(bindInfoQuery.getVccId(),
                    BUSINESS_TYPE, bindInfoQuery.getCaller(), bindInfoQuery.getCalled());
            if (axeBindInfoOfAxOptional.isPresent()) {
                PrivateBindInfoAxe privateBindInfoAxe = axeBindInfoOfAxOptional.get();
                if (StrUtil.isEmpty(privateBindInfoAxe.getTelB())) {
                    return getBindInfoVoOfXe(corpBusinessInfoOptional);
                }
                return getBindInfoVoOfAx(bindInfoQuery, axeBindInfoOfAxOptional.get());
            }
            return getBindInfoVoOfXe(corpBusinessInfoOptional);
        }

        // 根据tel_x和tel_x_ext查分机号的绑定关系获取A
        // X号码和分机号的绑定关系string
        Optional<PrivateBindInfoAxe> axeOptional = bindInfoQueryService.getAxeBindInfoOfXe(bindInfoQuery.getVccId(),
                BUSINESS_TYPE, bindInfoQuery.getCalled(), digitInfo);

        if (!axeOptional.isPresent()) {
            return Optional.of(new BindInfoVO(ErrorCodeEnum.EXT_NUM_NOT_VALID.getCode(),
                    ErrorCodeEnum.EXT_NUM_NOT_VALID.getMessage(), bindInfoQueryService.getNotBindIvr(corpBusinessInfoOptional),
                    NumberTypeEnum.XE.name()));
        }
        PrivateBindInfoAxe bindInfo = axeOptional.get();
        // tel = caller
        if (bindInfo.getTel().equals(bindInfoQuery.getCaller())) {
            // 主被叫一样
            log.info("{}, 绑定关系tel 与主叫一样", bindInfoQuery.getCalled());
            return Optional.of(new BindInfoVO(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage(),
                    bindInfoQueryService.getNotBindIvr(corpBusinessInfoOptional)));
        }

        // 行业短信, 直接返回tel
        if (ObjectUtil.isNotNull(bindInfoQuery.getIndustrySms()) && bindInfoQuery.getIndustrySms() == 1) {
            return getBindInfoVoByIndustrySms(bindInfo);
        }

        // 生成AYB模式
        if (1 == bindInfo.getAybFlag()) {
            return Optional.of(getBindInfoByAyb(bindInfo, bindInfoQuery));
        }

        // 设置了AX回呼, 不是短信
        if (CallbackFlagEnum.CALLBACK.getCode().equals(bindInfo.getCallbackFlag()) && 1 != bindInfoQuery.getSmsFlag()) {
            // 设置最近联系人B号码
            axeAsyncService.setupTelB(bindInfo, bindInfoQuery.getCaller(), BUSINESS_TYPE);
        }
        return getAxeBindInfoVO(bindInfoQuery, bindInfo);

    }

    private Optional<BindInfoVO> getBindInfoVoOfXe(Optional<PrivateCorpBusinessInfoDTO> corpBusinessInfoOptional) {
        BindInfoVO bindInfoVO = BindInfoVO.builder()
                .code(0)
                .message("AXE第一次查询!")
                .callerIvr(bindInfoQueryService.getDigitsIvr(corpBusinessInfoOptional))
                .numType(NumberTypeEnum.XE.name())
                .build();
        return Optional.of(bindInfoVO);
    }

    private Optional<BindInfoVO> getBindInfoVoOfAx(BindInfoQuery bindInfoQuery, PrivateBindInfoAxe bindInfoAxe) {
        BindInfoVO bindInfoVO = axeBindConverter.bindInfo2BindInfoVO(bindInfoAxe);
        bindInfoVO.setCode(0);
        bindInfoVO.setMessage("AXE-AX回呼成功!");
        bindInfoVO.setNumType(BUSINESS_TYPE);
        bindInfoVO.setDisplayNum(bindInfoQuery.getCalled());
        if (ModelEnum.REAL_TEL.getCode().equals(bindInfoAxe.getModel())) {
            bindInfoVO.setDisplayNum(bindInfoQuery.getCaller());
        }

        bindInfoVO.setCallNum(bindInfoQuery.getCalled());
        bindInfoVO.setExtNum(bindInfoAxe.getTelXExt());
        String callerIvrBefore = "";
        String callerIvr = "";
        String calledIvr = "";
        if (bindInfoQuery.getCaller().equals(bindInfoAxe.getTel())) {
            callerIvrBefore = Convert.toStr(bindInfoAxe.getAybAudioACallXBefore(), "");
        }
        bindInfoVO.setCalledNum(bindInfoAxe.getTelB());
        bindInfoVO.setCallerIvr(IvrUtil.getIvrName(callerIvr, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setCalledIvr(IvrUtil.getIvrName(calledIvr, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setCallerIvrBefore(IvrUtil.getIvrName(callerIvrBefore, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setTransferData(bindInfoQueryService.removeAudio(bindInfoAxe));
        return Optional.of(bindInfoVO);
    }

    /**
     * AXE 不生成AYB
     */
    private Optional<BindInfoVO> getAxeBindInfoVO(BindInfoQuery bindInfoQuery, PrivateBindInfoAxe bindInfoAxe) {
        BindInfoVO bindInfoVO = axeBindConverter.bindInfo2BindInfoVO(bindInfoAxe);
        bindInfoVO.setCode(0);
        bindInfoVO.setMessage("查询AXE成功!");
        bindInfoVO.setNumType(BUSINESS_TYPE);
        bindInfoVO.setDisplayNum(bindInfoQuery.getCalled());
        if (ModelEnum.REAL_TEL.getCode().equals(bindInfoAxe.getModel())) {
            bindInfoVO.setDisplayNum(bindInfoQuery.getCaller());
        }

        bindInfoVO.setCallNum(bindInfoQuery.getCalled());
        bindInfoVO.setExtNum(bindInfoAxe.getTelXExt());
        String callerIvr = "";
        String calledIvr = "";
        String callerIvrBefore = "";
        if (!bindInfoQuery.getCaller().equals(bindInfoAxe.getTel())) {
            callerIvr = Convert.toStr(bindInfoAxe.getAudio(), "");
            calledIvr = Convert.toStr(bindInfoAxe.getAudioCalled(), "");
        }
        bindInfoVO.setCalledNum(bindInfoAxe.getTel());
        bindInfoVO.setCallerIvr(IvrUtil.getIvrName(callerIvr, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setCalledIvr(IvrUtil.getIvrName(calledIvr, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setCallerIvrBefore(IvrUtil.getIvrName(callerIvrBefore, LocalCacheService.AUDIO_CODE_CACHE));
        bindInfoVO.setTransferData(bindInfoQueryService.removeAudio(bindInfoAxe));
        return Optional.of(bindInfoVO);
    }

    /**
     * 行业短信
     */
    private Optional<BindInfoVO> getBindInfoVoByIndustrySms(PrivateBindInfoAxe bindInfoAxe) {
        BindInfoVO bindInfoVO = BindInfoVO.builder()
                .numType(NumberTypeEnum.AXEYB_AXE.name())
                .code(0)
                .message("sms查询AXE成功")
                .displayNum(bindInfoAxe.getTelX())
                .callNum(bindInfoAxe.getTelX())
                .type(0)
                .calledNum(bindInfoAxe.getTel())
                .bindId(bindInfoAxe.getBindId())
                .areaCode(bindInfoAxe.getAreaCode())
                .userData(bindInfoAxe.getUserData())
                .transferData(bindInfoAxe)
                .build();
        return Optional.of(bindInfoVO);
    }

    /**
     * AXE-AYB
     */
    private BindInfoVO getBindInfoByAyb(PrivateBindInfoAxe bindInfoAxe, BindInfoQuery bindInfoQuery) {
        String vccId = bindInfoQuery.getVccId();
        // axe 的tel 作为axb 的tel_b  被叫
        String telB = bindInfoAxe.getTel();
        // 打分机号的主叫 作为axb的tel_a 主叫
        String telA = bindInfoQuery.getCaller();
        String caller = bindInfoQuery.getCaller();
        String axeRequestId = SecureUtil.md5(telA + bindInfoAxe.getTelX() + bindInfoQuery.getDigitInfo());
        // ayb_other_show  tel回呼Y时被叫看到的来电号码
        //1：看见Y
        //2：看见tel_x
        Integer aybOtherShow = bindInfoAxe.getAybOtherShow();
        // 查询ayb的requestId信息是否存在
        Optional<AxbBindingVO> axbBindingVoOptional = bindInfoQueryService.getAxbBindInfoByRequestId(vccId, BusinessTypeEnum.AXB.name(), axeRequestId);
        String callerIvr = "";
        String calledIvr = "";
        String callerIvrBefore = "";
        String calledNum = telA;
        if (!caller.equals(telB)) {
            // AX模型B打给A，接通后B播放的语音
            callerIvr = IvrUtil.getIvrName(bindInfoAxe.getAudio(), LocalCacheService.AUDIO_CODE_CACHE);
            // AX模型B打给A，接通后A播放的语音
            calledIvr = IvrUtil.getIvrName(bindInfoAxe.getAudioCalled(), LocalCacheService.AUDIO_CODE_CACHE);
            callerIvrBefore = IvrUtil.getIvrName(bindInfoAxe.getAybAudioBCallXBefore(), LocalCacheService.AUDIO_CODE_CACHE);
            calledNum = telB;
        }

        if (axbBindingVoOptional.isPresent()) {
            // 返回结果
            AxbBindingVO axbBindingVO = axbBindingVoOptional.get();
            Optional<PrivateBindInfoAxb> bindInfoAxbOptional = bindInfoQueryService.getAxbBindInfo(vccId, BusinessTypeEnum.AXB.name(), axbBindingVO.getTelX(), telB);
            BindInfoVO bindInfoVO = axeBindConverter.bindInfo2BindInfoVO(bindInfoAxe);
            bindInfoVO.setCode(0);
            bindInfoVO.setMessage("AYB绑定关系已生成过!");
            bindInfoVO.setNumType(NumberTypeEnum.AXEYB_AYB.name());
            bindInfoVO.setDisplayNum(axbBindingVO.getTelX());
            bindInfoVO.setCallNum(axbBindingVO.getTelX());
            bindInfoVO.setCalledNum(calledNum);
            bindInfoVO.setCallerIvr(callerIvr);
            bindInfoVO.setCalledIvr(calledIvr);
            bindInfoVO.setCallerIvrBefore(callerIvrBefore);
            bindInfoVO.setExtNum(bindInfoQuery.getDigitInfo());
            bindInfoAxbOptional.ifPresent(bindInfoAxb -> bindInfoVO.setTransferData(bindInfoQueryService.removeAudio(bindInfoAxb)));
            return bindInfoVO;
        }
        // 去请求AXB绑定接口
        AxbBindingDTO axbBindingDTO = axbBindConverter.bindInfoAxe2AxbBindingDto(bindInfoAxe);
        axbBindingDTO.setRequestId(axeRequestId);
        axbBindingDTO.setTelA(telB);
        axbBindingDTO.setTelB(telA);
        axbBindingDTO.setNumType(BusinessTypeEnum.AYB.name());
        axbBindingDTO.setAxeybTelX(bindInfoQuery.getCalled());
        axbBindingDTO.setAybOtherShow(aybOtherShow);
        axbBindingDTO.setAreaCode(bindInfoAxe.getAybAreaCode());
        axbBindingDTO.setType(0);
        axbBindingDTO.setTelX(null);
        Result result = bindInfoQueryService.bindAyb(axbBindingDTO);
        if (log.isInfoEnabled()) {
            log.info("AXE-AYB设置绑定关系返回: {}", JSON.toJSONString(result));
        }
        if (ObjectUtil.isEmpty(result) || result.getCode() != 0) {
            log.info("AYB未生成绑定关系!");
            BindInfoVO bindInfoVO = axeBindConverter.bindInfo2BindInfoVO(bindInfoAxe);
            bindInfoVO.setCode(0);
            bindInfoVO.setMessage("AYB未生成绑定关系!");
            bindInfoVO.setCalledNum(calledNum);
            bindInfoVO.setNumType(NumberTypeEnum.AXEYB_AXE.name());
            bindInfoVO.setDisplayNum(bindInfoAxe.getTelX());
            bindInfoVO.setCallNum(bindInfoAxe.getTelX());
            bindInfoVO.setCallerIvr(callerIvr);
            bindInfoVO.setCalledIvr(calledIvr);
            bindInfoVO.setCallerIvrBefore(callerIvrBefore);
            bindInfoVO.setExtNum(bindInfoQuery.getDigitInfo());
            bindInfoVO.setTransferData(bindInfoQueryService.removeAudio(bindInfoAxe));
            return bindInfoVO;
        }
        // 生成AYB绑定通知
        bindPushService.pushAybBind(result, bindInfoAxe);
        PrivateBindInfoAxb privateBindInfoAxb = (PrivateBindInfoAxb) result.getData();

        BindInfoVO bindInfoVO = axeBindConverter.bindInfo2BindInfoVO(bindInfoAxe);
        bindInfoVO.setCode(0);
        bindInfoVO.setMessage("生成AYB绑定关系成功!");
        bindInfoVO.setCalledNum(calledNum);
        bindInfoVO.setNumType(NumberTypeEnum.AXEYB_AYB.name());
        bindInfoVO.setDisplayNum(privateBindInfoAxb.getTelX());
        bindInfoVO.setCallNum(privateBindInfoAxb.getTelX());
        bindInfoVO.setCallerIvr(callerIvr);
        bindInfoVO.setCalledIvr(calledIvr);
        bindInfoVO.setCallerIvrBefore(callerIvrBefore);
        bindInfoVO.setExtNum(bindInfoQuery.getDigitInfo());
        bindInfoVO.setCalledIvr(calledIvr);
        bindInfoVO.setTransferData(bindInfoQueryService.removeAudio(privateBindInfoAxb));
        return bindInfoVO;
    }

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }

}
