package com.cqt.push.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cqt.common.enums.NumberTypeEnum;
import com.cqt.model.push.entity.AcrRecordOrg;
import com.cqt.push.entity.Bill;
import com.cqt.push.properties.BillProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author huweizhong
 * date  2023/10/7 17:21
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MeituanUtil {
    private final BillProperties billProperties;




    public Bill buildMeituanBill(AcrRecordOrg acr) {
        Bill bill = new Bill();
        JSONObject key7 = JSONObject.parseObject((String) acr.getKey7());
        log.info("接收话单报文=>{}", JSON.toJSONString(acr));
        String ts = key7.getString("ts");
        if (ts.length()>10){
            ts = ts.substring(0,10);
        }
        String transferData = PushUtil.decodeBase64(key7.getString("transfer_data"));
        JSONObject jsonObject = JSONObject.parseObject(transferData);

        bill.setAppKey(billProperties.getAppKey());
        bill.setAppId(billProperties.getAppId());
        bill.setRecordId(acr.getUuId());
        bill.setBindId(acr.getMessageId());
        bill.setRelateBindId(key7.getString("relate_bind_id"));
        String numType = key7.getString("num_type");
        bill.setBeginTime(strToUnixTime((String) acr.getKey3()));
        if (!StringUtils.isEmpty(acr.getMessageId())){
            int substring = Integer.parseInt(acr.getMessageId().substring(4, 6));
            if (substring == 21){
                bill.setBindId(jsonObject.getString("sourceBindId"));
                bill.setRelateBindId(acr.getMessageId());
            }

            if (substring == 22 ){
                substring = 20;
                bill.setTelA(acr.getCalledNum());
                bill.setTelB(acr.getCallInNum());
                bill.setCallType(11);
            }else {
                bill.setTelA(acr.getCallInNum());
                bill.setTelB(acr.getCalledNum());
            }
            bill.setServiceCode(substring);
        }else {
            bill.setTelA(acr.getCallInNum());
            bill.setTelB(acr.getCalledNum());
            if ("XE".equals(acr.getPlayMode())){
                bill.setServiceCode(20);
            }else {
                bill.setServiceCode(10);
            }
        }


        // axb ax  ayb
//        if ("AXB".equals(numType)) {
//            bill.setServiceCode(10);
//        } else if ("AXE".equals(numType)) {
//            bill.setServiceCode(20);
//        } else if (StringUtils.isEmpty(numType)) {
//            bill.setServiceCode(10);
//        } else {
//            bill.setServiceCode(21);
//        }
        bill.setAreaCode((String) acr.getKey2());

        double num = (double) (bill.getBeginTime() - bill.getBindTime()) / 14400;

        if (!StringUtils.isEmpty(transferData)) {
            bill.setBindTime(strToUnixTimeByNom(jsonObject.getString("createTime")));
            bill.setTs(jsonObject.getLong("ts"));
            bill.setUserData(jsonObject.getString("userData"));
            bill.setSign(jsonObject.getString("sign"));
            if (NumberTypeEnum.AXB.name().equals(numType) || NumberTypeEnum.AYB.name().equals(numType)) {
                // 有可能是axb 或者 axeyb 中的 ayb
                bill.setCallType(bill.getTelA()
                        .equals(jsonObject.getString("telA")) ? 10 : 11);
            } else if (NumberTypeEnum.AXE.name().equals(numType)) {
                // axeyb固定为11
                bill.setCallType(11);
            }
        }

        // axb
        if (bill.getServiceCode() == 10) {
            if (bill.getCallType() == 11) {
                bill.setTelA(acr.getCalledNum());
                bill.setTelB(acr.getCallInNum());
            }
            bill.setTelX(acr.getCallerDisplayNum());
            bill.setTelY("");
            bill.setBillCode(bill.getBindId() + "_" + (int) Math.ceil(num));
            // ax
        } else if (bill.getServiceCode() == 20) {
            bill.setTelX(acr.getCallerDisplayNum() + "," + acr.getDtmfKey());
            bill.setTelY(acr.getCalledDisplayNum().equals(acr.getCallerDisplayNum()) ? "" : acr.getCalledDisplayNum());
            bill.setBillCode(bill.getBindId() + "_" + acr.getCallInNum() + "_" + (int) Math.ceil(num));
            // ayb
        } else {
            if (bill.getCallType() == 11) {
                bill.setTelA(acr.getCalledNum());
                bill.setTelB(acr.getCallInNum());
            }
            bill.setTelX(acr.getCalledDisplayNum());
            bill.setTelY(acr.getCalledDisplayNum());
            bill.setBillCode(bill.getBindId() + "_" + (int) Math.ceil(num));
        }
        if ("99".equals(acr.getReleaseCause())) {
            bill.setCallType(10);
            bill.setBillCode("");
        }

        bill.setConnectTime(strToUnixTime(acr.getAbStartCallTime()));
        bill.setAlertingTime(strToUnixTime((String) acr.getKey1()));
        bill.setReleaseTime(strToUnixTime(acr.getStopCallTime()));
        bill.setCallDuration(Integer.parseInt(acr.getCalledDuration()));
        bill.setBillDuration(Integer.parseInt(acr.getCalledDuration()));
        if (("9".equals(acr.getReleaseCause()) || "91".equals(acr.getReleaseCause())) && acr.getCalledOriRescode ().startsWith ("5")) {
            bill.setCallResult(14);
        }else {
            bill.setCallResult(callResult(acr.getReleaseCause()));
        }
        if(bill.getCallResult() == 10){
            bill.setAlertingTime(bill.getReleaseTime());
        }
        bill.setRecordFileUrl(StringUtils.isEmpty(acr.getSrfmsgid()) ? "" : acr.getMsserver() + acr.getSrfmsgid());
        // 通话时长低于阈值的设置成空字符串
        if (bill.getCallDuration() < billProperties.getCallDuration()) {
            bill.setRecordFileUrl("");
        }
//        bill.setUserData(key7.getString("userData"));
        bill.setHasCost(bill.getBillDuration() > 0 ? 1 : 0);
        bill.setCallCost(0);
        return bill;
    }

    public static Integer callResult(String releaseCause) {
        switch (releaseCause) {
            case "1":
                return 1;
            case "2":
                return 2;
            case "4":
                return 3;
            case "5":
                return 7;
            case "6":
                return 6;
            case "7":
                return 9;
            case "8":
                return 16;
            case "9":
                return 5;
            case "91":
                return 5;
            case "10":
                return 2;
            case "11":
                return 4;
            case "99":
                return 10;
            case "12":
                return 16;
            default:
                return 16;
        }
    }

    public static int strToUnixTime(String dateStr) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            return (int) (dateFormat.parse(dateStr).getTime() / 1000);
        } catch (ParseException e) {
            return (int) (new Date(0).getTime() / 1000);
        }
    }

    public static int strToUnixTimeByNom(String dateStr) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return (int) (dateFormat.parse(dateStr).getTime() / 1000);
        } catch (ParseException e) {
            return (int) (new Date(0).getTime() / 1000);
        }
    }
}
