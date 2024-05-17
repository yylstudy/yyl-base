package com.cqt.wechat.utils;

import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.enums.CallEventEnum;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.model.sms.dto.CommonSmsBillPushDTO;
import com.cqt.wechat.config.RedissonUtil;
import com.cqt.wechat.entity.MsgInfo;
import com.cqt.wechat.entity.StatusInfo;
import com.cqt.wechat.entity.Thcode;
import com.cqt.wechat.entity.WechatCdrInfo;
import com.cqt.wechat.mapper.HcodeMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author huweizhong
 * date  2023/2/22 10:30
 */
@Service
@RequiredArgsConstructor
public class ConvertUtil {

    private final RedissonUtil redissonUtil;

    private final HcodeMapper hcodeMapper;

    public WechatCdrInfo toWechatCdr(PrivateBillInfo privateBillInfo) throws ParseException {
        WechatCdrInfo cdrInfo = new WechatCdrInfo();
        cdrInfo.setBaseInfo(toCallBaseInfo(privateBillInfo));
        cdrInfo.setRecord(toCallRecord(privateBillInfo));
        cdrInfo.setStatus(toCallStatus(privateBillInfo));
        return cdrInfo;
    }

    /**
     * @return 话单基础信息
     */
    public WechatCdrInfo.CallBaseInfo toCallBaseInfo(PrivateBillInfo privateBillInfo) {
        WechatCdrInfo.CallBaseInfo callBaseInfo = new WechatCdrInfo.CallBaseInfo();
        callBaseInfo.setCallId(privateBillInfo.getRecordId());
        callBaseInfo.setAreaCode(privateBillInfo.getAreaCode());
        callBaseInfo.setBindId(StringUtils.isEmpty(privateBillInfo.getBindId()) ? "0" : privateBillInfo.getBindId());
        callBaseInfo.setCaller(privateBillInfo.getTelA());
        callBaseInfo.setCalled(StringUtils.isEmpty(privateBillInfo.getTelB()) ? "0" : privateBillInfo.getTelB());
        callBaseInfo.setTelX(privateBillInfo.getTelX());
        callBaseInfo.setTelXext(privateBillInfo.getExt());
        return callBaseInfo;
    }

    /**
     * @return 话单状态信息
     */
    public WechatCdrInfo.CallRecord toCallRecord(PrivateBillInfo privateBillInfo) throws ParseException {
        WechatCdrInfo.CallRecord callRecord = new WechatCdrInfo.CallRecord();
        callRecord.setResultFlag(privateBillInfo.getRecordFlag());
        callRecord.setRecordFileUrl(privateBillInfo.getRecordFileUrl());
        if (StringUtils.isEmpty(privateBillInfo.getRecordStartTime())) {
            callRecord.setRecordStartTime(0);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(privateBillInfo.getRecordStartTime()));
            long time = calendar.getTimeInMillis() / 1000;

            callRecord.setRecordStartTime((int) time);
        }

        return callRecord;
    }

    public WechatCdrInfo.CallStatus toCallStatus(PrivateBillInfo privateBillInfo) throws ParseException {
        WechatCdrInfo.CallStatus callStatus = new WechatCdrInfo.CallStatus();
        callStatus.setCallResult(privateBillInfo.getCallResult());
        Calendar calendar = Calendar.getInstance();
        if (StringUtils.isEmpty(privateBillInfo.getBeginTime())) {
            callStatus.setBeginTime(0);
        } else {
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(privateBillInfo.getBeginTime()));
            long time = calendar.getTimeInMillis() / 1000;
            callStatus.setBeginTime((int) time);
        }
        if (StringUtils.isEmpty(privateBillInfo.getConnectTime())) {
            callStatus.setConnectTime(0);
        } else {
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(privateBillInfo.getConnectTime()));
            long time = calendar.getTimeInMillis() / 1000;

            callStatus.setConnectTime((int) time);
        }
        if (StringUtils.isEmpty(privateBillInfo.getAlertingTime())) {
            callStatus.setAlertTime(0);
        } else {
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(privateBillInfo.getAlertingTime()));
            long time = calendar.getTimeInMillis() / 1000;

            callStatus.setAlertTime((int) time);
        }
        if (StringUtils.isEmpty(privateBillInfo.getReleaseTime())) {
            callStatus.setReleaseTime(0);
        } else {
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(privateBillInfo.getReleaseTime()));
            long time = calendar.getTimeInMillis() / 1000;

            callStatus.setReleaseTime((int) time);
        }
        callStatus.setCallDuration(privateBillInfo.getCallDuration());
        return callStatus;
    }


    public MsgInfo toMsgInfo(CommonSmsBillPushDTO smsPushReq) throws ParseException {
        MsgInfo msgInfo = new MsgInfo();
        MsgInfo.MsgBaseInfo msgBaseInfo = new MsgInfo.MsgBaseInfo();
        msgBaseInfo.setSmsId(smsPushReq.getSmsId());
        msgBaseInfo.setBindId(smsPushReq.getBindId());
        msgBaseInfo.setAreaCode(smsPushReq.getAreaCode());
        msgBaseInfo.setSender(smsPushReq.getSender());
        msgBaseInfo.setReceiver(smsPushReq.getReceiver());
        msgBaseInfo.setSenderShow(smsPushReq.getSenderShow());
        msgBaseInfo.setReceiverShow(smsPushReq.getReceiverShow());
        Calendar calendar = Calendar.getInstance();
        if (StringUtils.isEmpty(smsPushReq.getTransferTime())) {
            msgBaseInfo.setTransferTime(0);
        } else {
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(smsPushReq.getTransferTime()));
            long time = calendar.getTimeInMillis() / 1000;
            msgBaseInfo.setTransferTime((int) time);
        }

        msgBaseInfo.setSmsContent(smsPushReq.getSmsContent());
        msgBaseInfo.setSmsResult(Integer.valueOf(smsPushReq.getSmsResult()));
        msgInfo.setBaseInfo(msgBaseInfo);
        return msgInfo;
    }

    public StatusInfo toStatusInfo(PrivateStatusInfo privateStatusInfo) throws ParseException {
        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setEvent(privateStatusInfo.getEvent());
        WechatCdrInfo.CallBaseInfo callBaseInfo = new WechatCdrInfo.CallBaseInfo();
        callBaseInfo.setCallId(privateStatusInfo.getRecordId());
        if (StringUtils.isEmpty(privateStatusInfo.getTelX())) {
            callBaseInfo.setAreaCode("0");
        } else {
            callBaseInfo.setAreaCode(getAreacode(privateStatusInfo.getTelX().substring(0, 7)));
        }
        callBaseInfo.setBindId(StringUtils.isEmpty(privateStatusInfo.getBindId()) ? "0" : privateStatusInfo.getBindId());
        callBaseInfo.setCalled(StringUtils.isEmpty(privateStatusInfo.getCalled()) ? "0" : privateStatusInfo.getCalled());
        callBaseInfo.setCaller(StringUtils.isEmpty(privateStatusInfo.getCaller()) ? "0" : privateStatusInfo.getCaller());
        callBaseInfo.setTelX(privateStatusInfo.getTelX());
        callBaseInfo.setTelXext(StringUtils.isEmpty(privateStatusInfo.getExt()) ? "0" : privateStatusInfo.getExt());
        WechatCdrInfo.CallStatus callStatus = new WechatCdrInfo.CallStatus();
        Calendar calendar = Calendar.getInstance();
        long time = 0;
        if (StringUtils.isNotEmpty(privateStatusInfo.getCurrentTime())) {
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(privateStatusInfo.getCurrentTime()));
            time = calendar.getTimeInMillis() / 1000;
        }
        callStatus.setBeginTime(0);
        callStatus.setConnectTime(0);
        callStatus.setAlertTime(0);
        callStatus.setReleaseTime(0);
        callStatus.setCallDuration(0);
        if (privateStatusInfo.getEvent().equals(CallEventEnum.answer.name())) {
            callStatus.setConnectTime((int) time);
        } else if (privateStatusInfo.getEvent().equals(CallEventEnum.hangup.name())) {
            callStatus.setReleaseTime((int) time);
        } else if (privateStatusInfo.getEvent().equals(CallEventEnum.callin.name())||privateStatusInfo.getEvent().equals(CallEventEnum.callout.name())) {
            callStatus.setBeginTime((int) time);
        } else if (privateStatusInfo.getEvent().equals(CallEventEnum.ringing.name())) {
            callStatus.setAlertTime((int) time);
        }
        callStatus.setCallResult(privateStatusInfo.getCallResult() == null ? 0 : privateStatusInfo.getCallResult());
        statusInfo.setBaseInfo(callBaseInfo);
        statusInfo.setStatus(callStatus);
        return statusInfo;
    }

    public String getAreacode(String hcode) {
        String s = String.format(PrivateCacheConstant.TEL_CODE_OF_AREA_CODE_KEY, hcode);
        String areaCode = redissonUtil.getString(s);
        if (StringUtils.isEmpty(areaCode)) {
            Thcode thcode = hcodeMapper.selectById(hcode);
            if (thcode != null) {
                areaCode = thcode.getAreacode();

            }
        }
        return areaCode;
    }


}
