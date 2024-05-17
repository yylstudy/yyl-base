package com.cqt.thirdchinanet.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cqt.common.enums.CallEventEnum;
import com.cqt.common.enums.NumberTypeEnum;
import com.cqt.common.enums.PushTypeEnum;
import com.cqt.common.enums.ServiceCodeEnum;
import com.cqt.model.bind.ax.entity.PrivateBindInfoAx;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.push.dto.AybBindPushDTO;
import com.cqt.model.push.dto.UnbindPushDTO;
import com.cqt.model.push.entity.AcrRecordOrg;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.thirdchinanet.PrivateNumberHmycThirdChinanetApplication;
import com.cqt.thirdchinanet.entity.ChinanetStatusInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.net.InetAddress.getLocalHost;

/**
 * 推送类的转换工具
 *
 * @author hlx
 * @date 2022-02-25
 */
@Slf4j
public class PushUtil {

    /**
     * 由话单构建挂机的通话状态
     *
     * @return 通话状态信息
     */
    public static PrivateStatusInfo buildStatusByBill(AcrRecordOrg acr) {
        PrivateStatusInfo statusInfo = new PrivateStatusInfo();

        JSONObject key7 = JSONObject.parseObject((String) acr.getKey7());
        // 提前需要判断的字段
        String numType = key7.getString("num_type");
        String transferData = decodeBase64(key7.getString("transfer_data"));
        statusInfo.setExt("");
        statusInfo.setCallResult(strToInteger(acr.getReleaseCause()));
        statusInfo.setCaller(acr.getCallInNum());
        statusInfo.setCalled(acr.getCalledNum());
        statusInfo.setAppKey(acr.getVccId());

        if (!StringUtils.isEmpty(transferData)) {
            if (NumberTypeEnum.AXB.name().equals(numType)) {
                PrivateBindInfoAxb privateBindInfoAxb =
                        JSON.parseObject(transferData, PrivateBindInfoAxb.class);
                statusInfo.setVccId(privateBindInfoAxb.getVccId());
                statusInfo.setBindId(privateBindInfoAxb.getBindId());
                statusInfo.setTelX(privateBindInfoAxb.getTelX());
                statusInfo.setCallType(statusInfo.getCaller().equals(privateBindInfoAxb.getTelA()) ? 10 : 11);
            } else if (NumberTypeEnum.AXEYB_AYB.name().equals(numType)) {
                PrivateBindInfoAxb privateBindInfoAxb =
                        JSON.parseObject(transferData, PrivateBindInfoAxb.class);
                statusInfo.setVccId(privateBindInfoAxb.getVccId());
                statusInfo.setBindId(privateBindInfoAxb.getSourceBindId());
                statusInfo.setExt(privateBindInfoAxb.getSourceExtNum());
                statusInfo.setTelX(acr.getCalledDisplayNum());
                statusInfo.setCallType(statusInfo.getCaller().equals(privateBindInfoAxb.getTelA()) ? 10 : 11);
            } else if (NumberTypeEnum.AXEYB_AXE.name().equals(numType)) {
                PrivateBindInfoAxe privateBindInfoAxe =
                        JSON.parseObject(transferData, PrivateBindInfoAxe.class);
                statusInfo.setVccId(privateBindInfoAxe.getVccId());
                statusInfo.setBindId(privateBindInfoAxe.getBindId());
                statusInfo.setExt(privateBindInfoAxe.getTelXExt());
                statusInfo.setTelX(privateBindInfoAxe.getTelX());
                statusInfo.setCallType(11);
            } else if (NumberTypeEnum.AX.name().equals(numType)) {
                PrivateBindInfoAx privateBindInfoAx =
                        JSON.parseObject(transferData, PrivateBindInfoAx.class);
                statusInfo.setVccId(privateBindInfoAx.getVccId());
                statusInfo.setBindId(privateBindInfoAx.getBindId());
                statusInfo.setTelX(privateBindInfoAx.getTelX());
                statusInfo.setCallType(privateBindInfoAx.getTelA()
                        .equals(statusInfo.getCaller()) ? 10 : 11);
            }
        }
        statusInfo.setRecordId(acr.getUuId());
        statusInfo.setCurrentTime(strDateFormat(acr.getStopCallTime()));
        statusInfo.setVccId(acr.getVccId());
        statusInfo.setEvent(CallEventEnum.hangup.name());
        statusInfo.setNumType(numType);
        return statusInfo;
    }

    /**
     * ayb绑定事件推送
     *
     * @param aybBindPushDTO ayb绑定信息dto
     * @return ayb绑定信息
     */
    public static PrivateFailMessage buildUnbindMessage(AybBindPushDTO aybBindPushDTO) {
        PrivateFailMessage billMessage = new PrivateFailMessage();
        billMessage.setIp(PrivateNumberHmycThirdChinanetApplication.ip);
        String aybBindJson = JSON.toJSONString(aybBindPushDTO);
        billMessage.setBody(aybBindJson);
        billMessage.setType(PushTypeEnum.AYB_BIND.name());
        return billMessage;
    }

    /**
     * 解绑事件信息构建
     *
     * @param unbindPushDTO 解绑信息dto
     * @return 解绑信息
     */
    public static PrivateFailMessage buildUnbindMessage(UnbindPushDTO unbindPushDTO) {
        PrivateFailMessage billMessage = new PrivateFailMessage();
        billMessage.setIp(PrivateNumberHmycThirdChinanetApplication.ip);
        String unbindJson = JSON.toJSONString(unbindPushDTO);
        billMessage.setBody(unbindJson);
        billMessage.setType(PushTypeEnum.UNBIND.name());
        return billMessage;
    }

    /**
     * 话单信息构建
     *
     * @param billInfo 通话话单
     * @return 通话话单信息
     */
    public static PrivateFailMessage buildBillMessage(PrivateBillInfo billInfo) {
        PrivateFailMessage billMessage = new PrivateFailMessage();
        billMessage.setIp(PrivateNumberHmycThirdChinanetApplication.ip);
        String billJson = JSON.toJSONString(billInfo);
        billMessage.setBody(billJson);
        billMessage.setType(PushTypeEnum.BILL.name());
        return billMessage;
    }

    /**
     * 通话状态信息构建
     *
     * @param statusInfo 通话状态
     * @return 通话状态信息
     */
    public static PrivateFailMessage buildStatusMessage(ChinanetStatusInfo statusInfo) {
        PrivateFailMessage billMessage = new PrivateFailMessage();
        billMessage.setIp(PrivateNumberHmycThirdChinanetApplication.ip);
        String statusJson = JSON.toJSONString(statusInfo);
        billMessage.setBody(statusJson);
        billMessage.setType(PushTypeEnum.STATUS.name());
        return billMessage;
    }

    /**
     * 透传参数解析
     *
     * @param statusInfo 通话状态
     * @return 构建好的通话状态
     */
    public static PrivateStatusInfo buildPrivateStatus(PrivateStatusInfo statusInfo) {
        String numberType = statusInfo.getNumType();
        String transferData = decodeBase64(statusInfo.getTransferData());
        statusInfo.setAppKey(statusInfo.getVccId());
        statusInfo.setCurrentTime(strDateFormat(statusInfo.getCurrentTime()));
        statusInfo.setUserData (statusInfo.getUserData ());
        if (!StringUtils.isEmpty(transferData)) {
            if (NumberTypeEnum.AXEYB_AYB.name().equals(numberType) ||
                    NumberTypeEnum.AXB.name().equals(numberType)) {
                PrivateBindInfoAxb privateBindInfoAxb =
                        JSON.parseObject(transferData, PrivateBindInfoAxb.class);
                statusInfo.setCallType(privateBindInfoAxb.getTelA()
                        .equals(statusInfo.getCaller()) ? 10 : 11);
                // ayb 需要特判
                if (privateBindInfoAxb.getBindId().contains(NumberTypeEnum.AYB.name().toLowerCase())) {
                    statusInfo.setTelX(privateBindInfoAxb.getTelX());
                }
            } else if (NumberTypeEnum.AXEYB_AXE.name().equals(numberType)) {
                PrivateBindInfoAxe privateBindInfoAxe =
                        JSON.parseObject(transferData, PrivateBindInfoAxe.class);
                statusInfo.setCallType(privateBindInfoAxe.getTel()
                        .equals(statusInfo.getCaller()) ? 10 : 11);
            } else if (NumberTypeEnum.AX.name().equals(numberType)) {
                PrivateBindInfoAx privateBindInfoAx =
                        JSON.parseObject(transferData, PrivateBindInfoAx.class);
                statusInfo.setCallType(privateBindInfoAx.getTelA()
                        .equals(statusInfo.getCaller()) ? 10 : 11);
            }
        }
        return statusInfo;
    }


    /**
     * acr转化为美团需要的话单
     *
     * @param acr 内部话单
     * @return 通用话单
     */
    public static PrivateBillInfo buildPrivateBill(AcrRecordOrg acr) {
        PrivateBillInfo privateBillInfo = new PrivateBillInfo();
        JSONObject key7 = JSONObject.parseObject((String) acr.getKey7());
        log.info("接收话单报文=>{}", JSON.toJSONString(acr));

        // 提前需要判断的字段
        String numType = key7.getString("num_type");
        privateBillInfo.setTelA(acr.getCallInNum());
        privateBillInfo.setTelB(acr.getCalledNum());
        String transferData = decodeBase64(key7.getString("transfer_data"));
        // serviceCode 判断
        privateBillInfo.setServiceCode(typeOfServiceCode(numType));
        privateBillInfo.setAppKey(acr.getVccId());

        if (!StringUtils.isEmpty(transferData)) {
            if (NumberTypeEnum.AXB.name().equals(numType)) {
                // 有可能是axb 或者 axeyb 中的 ayb
                PrivateBindInfoAxb privateBindInfoAxb =
                        JSON.parseObject(transferData, PrivateBindInfoAxb.class);
                privateBillInfo.setBindId(privateBindInfoAxb.getBindId());
                privateBillInfo.setSourceBindId(privateBindInfoAxb.getSourceBindId());
                privateBillInfo.setSourceRequestId(privateBindInfoAxb.getSourceRequestId());
                privateBillInfo.setUserData(privateBindInfoAxb.getUserData());
                privateBillInfo.setCallType(privateBillInfo.getTelA()
                        .equals(privateBindInfoAxb.getTelA()) ? 10 : 11);
                privateBillInfo.setAreaCode(privateBindInfoAxb.getAreaCode());
                privateBillInfo.setBindTime(dateFormat(privateBindInfoAxb.getCreateTime()));
                privateBillInfo.setRecordFlag(privateBindInfoAxb.getEnableRecord());
                privateBillInfo.setRequestId(privateBindInfoAxb.getRequestId());
                // 判断是否为ayb
                privateBillInfo.setServiceCode(privateBillInfo.getSourceBindId() == null ?
                        ServiceCodeEnum.AXB.getCode() : ServiceCodeEnum.AXEYB_AYB.getCode());
                privateBillInfo.setExt("");
            } else if (NumberTypeEnum.AXEYB_AYB.name().equals(numType)) {
                // 生成了ayb的axeyb_axe关系
                PrivateBindInfoAxb privateBindInfoAxb =
                        JSON.parseObject(transferData, PrivateBindInfoAxb.class);
                privateBillInfo.setBindId(privateBindInfoAxb.getSourceBindId());
                privateBillInfo.setUserData(privateBindInfoAxb.getUserData());
                // axeyb固定为11
                privateBillInfo.setCallType(11);
                privateBillInfo.setAreaCode(privateBindInfoAxb.getSourceAreaCode());
                privateBillInfo.setBindTime(dateFormat(privateBindInfoAxb.getSourceBindTime()));
                privateBillInfo.setRecordFlag(privateBindInfoAxb.getEnableRecord());
                privateBillInfo.setRequestId(privateBindInfoAxb.getSourceRequestId());
                privateBillInfo.setExt(privateBindInfoAxb.getSourceExtNum());
                privateBillInfo.setTelY(privateBindInfoAxb.getTelX());
                privateBillInfo.setTelX(privateBindInfoAxb.getAxeybTelX());
            } else if (NumberTypeEnum.AXEYB_AXE.name().equals(numType)) {
                // 只接收没生成ayb的axeyb_axe关系
                PrivateBindInfoAxe privateBindInfoAxe =
                        JSON.parseObject(transferData, PrivateBindInfoAxe.class);
                privateBillInfo.setBindId(privateBindInfoAxe.getBindId());
                privateBillInfo.setUserData(privateBindInfoAxe.getUserData());
                // axeyb固定为11
                privateBillInfo.setCallType(11);
                privateBillInfo.setAreaCode(privateBindInfoAxe.getAreaCode());
                privateBillInfo.setBindTime(dateFormat(privateBindInfoAxe.getCreateTime()));
                privateBillInfo.setRecordFlag(privateBindInfoAxe.getEnableRecord());
                privateBillInfo.setRequestId(privateBindInfoAxe.getRequestId());
                privateBillInfo.setExt(privateBindInfoAxe.getTelXExt());
            } else if (NumberTypeEnum.AX.name().equals(numType)) {
                PrivateBindInfoAx privateBindInfoAx =
                        JSON.parseObject(transferData, PrivateBindInfoAx.class);
                privateBillInfo.setBindId(privateBindInfoAx.getBindId());
                privateBillInfo.setUserData(privateBindInfoAx.getUserData());
                privateBillInfo.setCallType(privateBillInfo.getTelA()
                        .equals(privateBindInfoAx.getTelA()) ? 10 : 11);
                privateBillInfo.setAreaCode(privateBindInfoAx.getAreaCode());
                privateBillInfo.setBindTime(dateFormat(privateBindInfoAx.getCreateTime()));
                privateBillInfo.setRecordFlag(privateBindInfoAx.getEnableRecord());
                privateBillInfo.setRequestId(privateBindInfoAx.getRequestId());
            }
        }else {

            privateBillInfo.setBindId (acr.getMessageId ());
            privateBillInfo.setAreaCode (acr.getKey2 ());
            privateBillInfo.setUserData (key7.getString ("userData"));
        }

        privateBillInfo.setRecordId(acr.getUuId());
        privateBillInfo.setBeginTime(strDateFormat((String) acr.getKey3()));


        if (NumberTypeEnum.AXEYB_AXE.name().equals(numType)) {
            privateBillInfo.setTelX(acr.getCallerDisplayNum());
            privateBillInfo.setTelY(acr.getCalledDisplayNum()
                    .equals(acr.getCallerDisplayNum()) ? acr.getCallerDisplayNum() : acr.getCalledDisplayNum());
        } else if (NumberTypeEnum.AXEYB_AYB.name().equals(numType)) {
            // ayb上面处理
        } else if (NumberTypeEnum.AXB.name().equals(numType)) {
            privateBillInfo.setTelX(acr.getCallerDisplayNum());
            privateBillInfo.setTelY(acr.getCalledDisplayNum());
        } else if (NumberTypeEnum.AXE.name().equals(numType)) {
            privateBillInfo.setTelX(acr.getCallerDisplayNum());
            privateBillInfo.setTelY(acr.getCalledDisplayNum());
        } else if (NumberTypeEnum.AX.name().equals(numType)){
            privateBillInfo.setTelX(acr.getCallerDisplayNum());
            privateBillInfo.setTelY("");
        }else {
            privateBillInfo.setTelY (acr.getCalledDisplayNum ());
            privateBillInfo.setTelX(acr.getCallerDisplayNum());
        }
        privateBillInfo.setCalloutTime (strDateFormat(acr.getCallOutTime ()));
        privateBillInfo.setConnectTime(strDateFormat(acr.getAbStartCallTime()));
        privateBillInfo.setAlertingTime(strDateFormat((String) acr.getKey1()));
        privateBillInfo.setReleaseTime(strDateFormat(acr.getStopCallTime()));
        privateBillInfo.setCallDuration(strToInteger(acr.getCalledDuration()));
        privateBillInfo.setCallResult(strToInteger(acr.getReleaseCause()));
        privateBillInfo.setRecordFileUrl(StringUtils.isEmpty(acr.getSrfmsgid()) ? "" : acr.getMsserver() + acr.getSrfmsgid());
        privateBillInfo.setRecordStartTime(strDateFormat(key7.getString("recordStartTime")));
        return privateBillInfo;
    }

    public static int strToInteger(String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String dateFormat(Date date) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String strDateFormat(String dateStr) {
        DateFormat sourceFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date date = sourceFormat.parse(dateStr);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    public static Integer typeOfServiceCode(String numType) {
        if (NumberTypeEnum.AXEYB_AXE.name().equals(numType)) {
            return ServiceCodeEnum.AXEYB_AXE.getCode();
        } else if (NumberTypeEnum.AXEYB_AYB.name().equals(numType)) {
            return ServiceCodeEnum.AXEYB_AXE.getCode();
        } else if (NumberTypeEnum.AXB.name().equals(numType)) {
            return ServiceCodeEnum.AXB.getCode();
        } else if (NumberTypeEnum.AXE.name().equals(numType)) {
            return ServiceCodeEnum.AXE.getCode();
        } else if (NumberTypeEnum.AX.name().equals(numType)) {
            return ServiceCodeEnum.AX.getCode();
        }
        return 0;
    }


    public static String decodeBase64(String code) {
        try {
            return new String(Base64.decode(code), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.info("transferData解码失败，异常=>{}", e.getMessage());
            return "{}";
        }
    }

    public static String createSign(Map<String, Object> params, String vccId, String secretKey) {
        params.remove("appkey");
        params.remove("sign");
        params.remove("vcc_id");
        List<String> paramList = new ArrayList<>();
        params.forEach((key, value) -> {
            if (ObjectUtil.isNotEmpty(value)) {
                paramList.add(key + "=" + StrUtil.removeAll(value.toString(), CharUtil.CR, CharUtil.LF, CharUtil.SPACE));
            }
        });
        paramList.add("secret_key=" + secretKey);
        return SecureUtil.md5(String.join("&", paramList)).toUpperCase();
    }

    public static String getLocalIpStr() {
        InetAddress addr = null;
        try {
            addr = getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        assert addr != null;
        return addr.getHostAddress();
    }


}
