package com.cqt.vccidhmyc.web.service.impl;

import cn.hutool.core.util.StrUtil;
import com.cqt.model.unicom.entity.PrivateCorpInteriorInfo;
import com.cqt.vccidhmyc.config.cache.RoamingNumberCache;
import com.cqt.vccidhmyc.web.model.vo.CallDispatcherVO;
import com.cqt.vccidhmyc.web.service.CallDispatcherService;
import com.cqt.vccidhmyc.web.service.DataQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-03-27 14:16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallDispatcherServiceImpl implements CallDispatcherService {

    private final DataQueryService dataQueryService;

    @Override
    public CallDispatcherVO dispatcher(String callerNum, String calledNum) {
        CallDispatcherVO callDispatcherVO = new CallDispatcherVO();
        log.info("主叫: {}, 被叫: {}", callerNum, calledNum);

        // 中间号是否是漫游号
        String roamingCalledNum = "86" + calledNum;
        boolean isRoaming = RoamingNumberCache.isExist(roamingCalledNum);

        String secretNo = calledNum;
        // 是手机号
        if (isRoaming) {
            // 根据漫游号查询imsi
            Optional<String> imsiOptional = dataQueryService.getImsi(roamingCalledNum);
            if (!imsiOptional.isPresent()) {
                // 异常
                log.error("漫游号: {}, 未查询到imsi", roamingCalledNum);
                return CallDispatcherVO.fail("未查询到imsi", calledNum);
            }

            String imsi = imsiOptional.get();
            // 根据msrn查询小号
            secretNo = dataQueryService.getSecretNoByImsi(imsi);
            log.info("根据msrn查询小号, 漫游号: {}, imsi: {}, 小号: {}", roamingCalledNum, imsi, secretNo);
        }
        // 根据号码查询vccId
        String vccId = dataQueryService.getVccIdBySecretNo(secretNo);
        if (StrUtil.isEmpty(vccId)) {
            // 没找到vccId
            log.error("根据x号码: {}, 没找到vccId", secretNo);
            return CallDispatcherVO.fail("未查询到vccId", calledNum);
        }

        // 其他是固话, 95

        // 根据小号查询内部配置
        Optional<PrivateCorpInteriorInfo> corpInteriorInfoOptional = dataQueryService.getCorpInteriorInfo(vccId);
        if (!corpInteriorInfoOptional.isPresent()) {
            log.error("未查询到企业: {}, 内部配置", vccId);
            return CallDispatcherVO.fail("未查询到企业内部配置", calledNum);
        }
        PrivateCorpInteriorInfo corpInteriorInfo = corpInteriorInfoOptional.get();
        dealCallDispatcherVO(callDispatcherVO, corpInteriorInfo, secretNo);
        return callDispatcherVO;
    }

    private void dealCallDispatcherVO(CallDispatcherVO callDispatcherVO,
                                      PrivateCorpInteriorInfo corpInteriorInfo,
                                      String secretNo) {
        callDispatcherVO.setStateCode("0000");
        callDispatcherVO.setRemark("success");
        callDispatcherVO.setCallLimit(dataQueryService.getCallLimit(corpInteriorInfo.getVccId()));
        callDispatcherVO.setVccId(corpInteriorInfo.getVccId());
        callDispatcherVO.setCallIn95Num(corpInteriorInfo.getCallin95Num());
        callDispatcherVO.setCallInFix(corpInteriorInfo.getCallinFixNum());
        callDispatcherVO.setCallIn400Num(corpInteriorInfo.getCallin400Num());
        callDispatcherVO.setMiddleNum(secretNo);
        callDispatcherVO.setAllIsRecord(corpInteriorInfo.getIsRecord());
        callDispatcherVO.setFindBindRelationUrl(corpInteriorInfo.getQueryBindInfoUrl());
        callDispatcherVO.setLuaName(corpInteriorInfo.getLuaName());
        callDispatcherVO.setAutoHangup(corpInteriorInfo.getAutoHangup());
        callDispatcherVO.setFailStateAsr(corpInteriorInfo.getFailStatEasr());
    }

}
