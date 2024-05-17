package com.cqt.cdr.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.base.util.CacheUtil;
import com.cqt.cdr.interceptor.TheadLocalUtil;
import com.cqt.cdr.mapper.SubCdrMapper;
import com.cqt.cdr.service.ISubCdrService;
import com.cqt.cdr.util.CommonUtils;
import com.cqt.cdr.util.LocalOrLongUtils;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.cdr.entity.CallCenterSubCdr;
import com.cqt.model.number.entity.NumberInfo;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @author ld
 * @since 2023-08-15
 */
@Service
@Slf4j
public class SubCdrServiceImpl extends ServiceImpl<SubCdrMapper, CallCenterSubCdr> implements ISubCdrService {
    private static final Logger MYSQL_FAIL_LOG = LoggerFactory.getLogger("cdrInsertFailLogger");

    @Resource
    RedissonUtil redissonUtil;

    @Resource
    private CommonDataOperateService commonDataOperateService;

    @Override
    public void insertSubCdr(String LOG_TAG, List<CallCenterSubCdr> subCdrs, String companyCode, String month, CallCenterSubCdr subCdr) {
        subCdr.setCalleeAreaCode(LocalOrLongUtils.getNumberCode(subCdr.getCalleeNumber(), redissonUtil));
        subCdr.setCallerAreaCode(LocalOrLongUtils.getNumberCode(subCdr.getCallerNumber(), redissonUtil));
        Optional<NumberInfo> numberInfo = commonDataOperateService.getNumberInfo(CommonUtils.replaceNumPerfix(subCdr.getPlatformNumber()));
        String chargeNumber = null;
        if (numberInfo.isPresent()) {
            chargeNumber = numberInfo.get().getChargeNumber();
        }
        subCdr.setCallerNumber(CommonUtils.replaceNumPerfix(subCdr.getCallerNumber()));
        subCdr.setDisplayNumber(CommonUtils.replaceNumPerfix(subCdr.getDisplayNumber()));
        subCdr.setCalleeNumber(CommonUtils.replaceNumPerfix(subCdr.getCalleeNumber()));
        subCdr.setPlatformNumber(CommonUtils.replaceNumPerfix(subCdr.getPlatformNumber()));
        subCdr.setChargeNumber(chargeNumber);
        // 呼入
        if (subCdr.getCdrType() == 1) {
            // 主叫号码 呼入是客户号码
            subCdr.setCallingPartyNumber(CommonUtils.replaceNumPerfix(subCdr.getDisplayNumber()));
            // 被叫号码 呼入：呼入的平台号码（有配置特服号码的使用特服号码）
            subCdr.setCalledPartyNumber(chargeNumber);
        }
        // 呼出
        if (subCdr.getCdrType() == 0) {
            // 主叫号码 呼出：呼出的平台号码（有配置特服号码的使用特服号码）
            subCdr.setCallingPartyNumber(chargeNumber);
            // 被叫号码 呼入是客户号码
            subCdr.setCalledPartyNumber(CommonUtils.replaceNumPerfix(subCdr.getCalleeNumber()));
        }
        try {
            this.save(subCdr);
        } catch (DuplicateKeyException duplicateKeyException) {
        } catch (Exception e) {
            log.error(LOG_TAG + "子话单：{}，录入异常", subCdr, e);
            if (e instanceof TransientDataAccessResourceException) {
                throw e;
            }
            MYSQL_FAIL_LOG.info(TheadLocalUtil.instance().getSql() + ";");
        } finally {
            TheadLocalUtil.instance().reset();
        }
    }

//    @Override
//    public void insertSubCdr(String LOG_TAG, List<CallCenterSubCdr> subCdrs, String companyCode, String month, CallCenterSubCdr subCdr) {
//        try {
//            subCdr.setCalleeAreaCode(LocalOrLongUtils.getNumberCode(subCdr.getCalleeNumber()));
//            subCdr.setCallerAreaCode(LocalOrLongUtils.getNumberCode(subCdr.getCallerAreaCode()));
//            NumberInfo number = redissonUtil.get("cloudcc:numberInfo:" + subCdr.getPlatformNumber(), NumberInfo.class);
//            String chargeNumber = number != null ? number.getChargeNumber() : subCdr.getPlatformNumber();
//            subCdr.setChargeNumber(chargeNumber);
//            // 呼入
//            if (subCdr.getCdrType() == 1) {
//                // 主叫号码 呼入是客户号码
//                subCdr.setCallingpartynumber(subCdr.getDisplayNumber());
//                // 被叫号码 呼入：呼入的平台号码（有配置特服号码的使用特服号码）
//                subCdr.setCalledpartynumber(chargeNumber);
//            }
//            // 呼出
//            if (subCdr.getCdrType() == 0) {
//                // 主叫号码 呼出：呼出的平台号码（有配置特服号码的使用特服号码）
//                subCdr.setCallingpartynumber(chargeNumber);
//                // 被叫号码 呼入是客户号码
//                subCdr.setCalledpartynumber(subCdr.getCalleeNumber());
//            }
//            this.save(subCdr);
//        } catch (DuplicateKeyException duplicateKeyException) {
//
//        } catch (BadSqlGrammarException badSqlGrammarException) {
//            badSqlGrammarException.printStackTrace();
//            String errorMessage = badSqlGrammarException.getMessage();
//            if (errorMessage.contains("doesn't exist")) {
//                String cloucccSubCdr = tables("clouccc_sub_cdr", month, companyCode);
//                mainCdrMapper.createTable(cloucccSubCdr);
//                subCdrs.forEach(sCdr -> {
//                    try {
//                        this.save(sCdr);
//                    } catch (Exception e) {
//                        log.warn(LOG_TAG + "子话单sql：{}，录入异常存入cdr-insert-fail日志", TheadLocalUtil.instance().getSql(), badSqlGrammarException);
//                        MYSQL_FAIL_LOG.info(TheadLocalUtil.instance().getSql());
//                    }
//                });
//            } else {
//                log.warn(LOG_TAG + "子话单sql：{}，录入异常存入cdr-insert-fail日志", TheadLocalUtil.instance().getSql(), badSqlGrammarException);
//                MYSQL_FAIL_LOG.info(TheadLocalUtil.instance().getSql());
//            }
//        } catch (Exception e) {
//            log.error(LOG_TAG + "子话单：{}，录入异常", subCdr, e);
//            MYSQL_FAIL_LOG.info(TheadLocalUtil.instance().getSql());
//        } finally {
//            TheadLocalUtil.instance().reset();
//        }
//    }
}
