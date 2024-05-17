package com.cqt.cdr.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.base.contants.CommonConstant;
import com.cqt.cdr.entity.AcrRecord;
import com.cqt.cdr.interceptor.TheadLocalUtil;
import com.cqt.cdr.mapper.AcrRecordMapper;
import com.cqt.cdr.service.AcrRecordService;
import com.cqt.cdr.util.LocalOrLongUtils;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.cdr.entity.CallCenterMainCdr;
import com.cqt.model.cdr.entity.CallCenterSubCdr;
import com.cqt.model.cdr.entity.CdrChanneldata;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.number.entity.NumberInfo;
import com.cqt.mybatisplus.config.core.RequestDataHelper;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static com.cqt.cdr.util.CommonUtils.date2Str;

/**
 * @author Administrator
 * @description 针对表【clouccc_acr_record】的数据库操作Service实现
 * @createDate 2023-08-21 15:26:31
 */
@Service
@Slf4j
public class AcrRecordServiceImpl extends ServiceImpl<AcrRecordMapper, AcrRecord>
        implements AcrRecordService {
    private static final Logger CDR_RECORD_FAIL_LOG = LoggerFactory.getLogger("cdrRecordInsertFailLoger");

    @Resource
    RedissonUtil redissonUtil;

    @Resource
    private CommonDataOperateService commonDataOperateService;

    @Override
    public void insertAcrRecord(String LOG_TAG, String month, ArrayList<AcrRecord> acrRecords, AcrRecord acrRecord) {
        RequestDataHelper.setRequestData(CommonConstant.COMPANY_CODE_KEY, acrRecord.getGroupnumber());
        try {
            this.save(acrRecord);
        } catch (DuplicateKeyException duplicateKeyException) {

        } catch (Exception e) {
            log.error(LOG_TAG + "计费话单：{}，录入异常", acrRecord, e);
            if (e instanceof TransientDataAccessResourceException) {
                throw e;
            }
            CDR_RECORD_FAIL_LOG.info(TheadLocalUtil.instance().getSql());
        } finally {
            TheadLocalUtil.instance().reset();
        }
    }

//    @Override
//    public void insertAcrRecord(String LOG_TAG, String month, ArrayList<AcrRecord> acrRecords, AcrRecord acrRecord) {
//        try {
//            RequestDataHelper.setRequestData(CommonConstant.COMPANY_CODE_KEY, acrRecord.getGroupnumber());
//            this.save(acrRecord);
//        } catch (DuplicateKeyException duplicateKeyException) {
//
//        } catch (BadSqlGrammarException badSqlGrammarException) {
//            badSqlGrammarException.printStackTrace();
//            String errorMessage = badSqlGrammarException.getMessage();
//            if (errorMessage.contains("doesn't exist")) {
//                String cloucccAcrRecord = tables("acr_record", month, acrRecord.getGroupnumber());
//                mainCdrMapper.createTable(cloucccAcrRecord);
//                acrRecords.forEach(arCdr -> {
//                    try {
//                        this.save(arCdr);
//                    } catch (Exception e) {
//                        log.warn(LOG_TAG + "计费话单sql：{}，录入异常存入cdr-insert-fail日志", TheadLocalUtil.instance().getSql(), e);
//                        CDR_RECORD_FAIL_LOG.info(TheadLocalUtil.instance().getSql());
//                    }
//                });
//            } else {
//                log.warn(LOG_TAG + "计费话单sql：{}，录入异常存入cdr-insert-fail日志", TheadLocalUtil.instance().getSql(), badSqlGrammarException);
//                CDR_RECORD_FAIL_LOG.info(TheadLocalUtil.instance().getSql());
//            }
//        } catch (Exception e) {
//            log.error(LOG_TAG + "计费话单：{}，录入异常", acrRecord, e);
//            CDR_RECORD_FAIL_LOG.info(TheadLocalUtil.instance().getSql());
//        } finally {
//            TheadLocalUtil.instance().reset();
//        }
//    }

    public AcrRecord getAcrRecord(CallCenterMainCdr mainCdr, CallCenterSubCdr callCenterSubCdr, CdrChanneldata cdrChanneldata){
        AcrRecord acrRecord = null;
        // vcc_id不存在不计费
        CompanyInfo companyInfoDTO = commonDataOperateService.getCompanyInfoDTO(callCenterSubCdr.getCompanyCode());
        if (companyInfoDTO != null && StringUtils.isEmpty(companyInfoDTO.getVccId())) {
            return acrRecord;
        }
        acrRecord = new AcrRecord();
        // 企业标识查找的ctd企业标识
        // 拿到company_code找到vccid
        acrRecord.setGroupnumber(companyInfoDTO.getVccId());

        String callUUID = callCenterSubCdr.getUuid();

        // 取主话单的callStartTime
        String callStartTime = date2Str(mainCdr.getCallStartTime());
        String streamnumber = "C" + callStartTime + "AC" + callUUID.replaceAll("-", "").substring(0, 16);
        // 序列号，"C" + callStartTime + "AC" + callUUID.replaceAll("-", "").substring(0, 16)
        acrRecord.setStreamnumber(streamnumber);

        Integer cdrType = callCenterSubCdr.getCdrType();
        Optional<NumberInfo> numberInfo = commonDataOperateService.getNumberInfo(callCenterSubCdr.getPlatformNumber());
        String chargeNumber = numberInfo.isPresent() ? numberInfo.get().getChargeNumber() : callCenterSubCdr.getPlatformNumber();

        // 呼入
        if (cdrType == 1) {
            // 业务关键字 呼入920011
            acrRecord.setServicekey("920011");

            // 呼入：客户呼入的平台号码（有配置特服号码的使用特服号码）
            acrRecord.setCalledpartynumber(chargeNumber);

            // 呼入：客户呼入的平台号码查找对应的计费号码，如没有配置计费号码，则计费号码为客户呼入的平台号码
            acrRecord.setSpecificchargedpar(chargeNumber);

            // 呼入：客户号码
            acrRecord.setCallingpartynumber(callCenterSubCdr.getDisplayNumber());

            // 分机号码直接填，企业主显号找到号码表的外显号
            // 呼入：坐席号码；
            acrRecord.setTranslatednumber(callCenterSubCdr.getAgentId());

            // 呼入：坐席 呼出: 客户号码
            acrRecord.setOricallednumber(callCenterSubCdr.getAgentId());

            // 呼入：客户号码；呼出：坐席
            acrRecord.setOricallingnumber(callCenterSubCdr.getDisplayNumber());

            // 呼出写1，呼入写2
            acrRecord.setAcrtype(2);

            // 呼入 主叫区号 呼出 被叫区号
            acrRecord.setAreanumber(callCenterSubCdr.getCallerAreaCode());

            // 开始时间与结束时间，呼入a侧是用户侧，呼出b侧是用户侧
            // 客户侧开始时间 answer_time
            acrRecord.setStartdateandtime(date2Str(callCenterSubCdr.getAAnswerTime()));
            // 客户侧结束时间 hangup_time
            acrRecord.setStopdateandtime(date2Str(callCenterSubCdr.getAHangupTime()));
        }

        // 呼出
        if (cdrType == 0) {
            // 呼出900011
            acrRecord.setServicekey("900011");

            // 呼出：客户号码
            acrRecord.setCalledpartynumber(callCenterSubCdr.getCalleeNumber());

            // 呼出：呼出的平台号码（有配置特服号码的使用特服号码）
            acrRecord.setCallingpartynumber(chargeNumber);

            // 呼出：呼出的平台号码查找对应的计费号码，如没有配置计费号码，则计费号码为呼出的平台号码
            acrRecord.setSpecificchargedpar(chargeNumber);

            // 呼出：客户号码
            acrRecord.setTranslatednumber(callCenterSubCdr.getCalleeNumber());

            // 呼出: 客户号码
            acrRecord.setOricallednumber(callCenterSubCdr.getCalleeNumber());

            // 呼入：客户号码；呼出：坐席
            acrRecord.setOricallingnumber(callCenterSubCdr.getAgentId());

            // 呼出写1，呼入写2
            acrRecord.setAcrtype(1);

            // 客户侧区号
            // 呼入 主叫区号 呼出 被叫区号
            acrRecord.setAreanumber(callCenterSubCdr.getCalleeAreaCode());

            // 开始时间与结束时间，呼入a侧是用户侧，呼出b侧是用户侧
            // 客户侧开始时间 answer_time
            acrRecord.setStartdateandtime(date2Str(callCenterSubCdr.getBAnswerTime()));
            // 客户侧结束时间 hangup_time
            acrRecord.setStopdateandtime(date2Str(callCenterSubCdr.getBHangupTime()));
        }

        // 通话费用
        acrRecord.setCallcost(1);
        acrRecord.setChargemode(1);

        // 振铃时间 b路 应答时间-振铃时间
        if (callCenterSubCdr.getBRingStamp() == null) {
            acrRecord.setExtforwardnumber(null);
        } else {
            long l = (callCenterSubCdr.getBAnswerStamp() == null ? callCenterSubCdr.getBHangupStamp() : callCenterSubCdr.getBAnswerStamp()) - callCenterSubCdr.getBRingStamp();
            acrRecord.setExtforwardnumber(new BigDecimal(l).divide(new BigDecimal(1000)).toString());
        }

        // 通话时长 秒 ivr + 通话
        Long ivrDuration = null;
        if (callCenterSubCdr.getStartIvrTime() != null && callCenterSubCdr.getEndIvrTime() != null) {
            ivrDuration = (callCenterSubCdr.getEndIvrTime().getTime() - callCenterSubCdr.getStartIvrTime().getTime()) / 1000;
        }
        Long l = (ivrDuration == null ? 0l : ivrDuration) + (callCenterSubCdr.getBDuration() == null ? 0 : callCenterSubCdr.getBDuration()) / 1000;
        acrRecord.setDuration(l);

        // 呼叫开始时间 a路
        acrRecord.setBegintime(date2Str(callCenterSubCdr.getACallStartTime()));

        // 呼入时间(暂不确定);
        acrRecord.setCallintime(null);

        acrRecord.setCalltype(0);

        // 坐席工号
        acrRecord.setCallersubgroup(callCenterSubCdr.getAgentId());
        acrRecord.setCalleesubgroup("");

        // "C" + callStartTime + "AC" + callUUID.replaceAll("-", "").substring(0, 16)
        acrRecord.setAcrcallid(streamnumber);

        acrRecord.setCallerpnp("");
        acrRecord.setCalleepnp("");
        acrRecord.setReroute(1);
        acrRecord.setCallcategory(1);

        callCenterSubCdr.setCalleeAreaCode(LocalOrLongUtils.getNumberCode(callCenterSubCdr.getCalleeNumber(), redissonUtil));
        callCenterSubCdr.setCallerAreaCode(LocalOrLongUtils.getNumberCode(callCenterSubCdr.getCallerAreaCode(), redissonUtil));
        String callerAreaCode = callCenterSubCdr.getCallerAreaCode();
        String calleeAreaCode = callCenterSubCdr.getCalleeAreaCode();
        if (StringUtils.isEmpty(callerAreaCode) || StringUtils.isEmpty(calleeAreaCode) || calleeAreaCode.equals(callerAreaCode)) {
            // 市话
            acrRecord.setChargetype(0);
            acrRecord.setLocalorlong("0");
        } else {
            // 国内长途
            acrRecord.setChargetype(1);
            acrRecord.setLocalorlong("1");
        }

        acrRecord.setUserpin("");
        // 0非视频，1是视频
        // 需要转下
        if (callCenterSubCdr.getMediaType() == 0) {
            acrRecord.setVideocallflag(1);
        } else {
            acrRecord.setVideocallflag(2);
        }

        acrRecord.setServiceid("");

        // 通话Uuid
        acrRecord.setForwardnumber(callCenterSubCdr.getUuid());

        acrRecord.setSrfmsgid(callCenterSubCdr.getRecordUrl());

        if (cdrChanneldata != null) {
            acrRecord.setMsserver(cdrChanneldata.getFreeSwitchName());
        }

        acrRecord.setReleasecause(null);

        acrRecord.setChargeclass(1);
        acrRecord.setTransparentparamet("");

        return acrRecord;
    }
}




