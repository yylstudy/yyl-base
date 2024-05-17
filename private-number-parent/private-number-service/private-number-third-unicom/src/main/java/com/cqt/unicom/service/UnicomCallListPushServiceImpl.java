package com.cqt.unicom.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.constants.ThirdConstant;
import com.cqt.common.enums.ServiceCodeEnum;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.model.push.entity.Callstat;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.unicom.dto.CallListPushDTO;
import com.cqt.model.unicom.entity.CommResult;
import com.cqt.model.unicom.entity.CustomerReceivesDataInfo;
import com.cqt.model.unicom.entity.PrivateCorpInteriorInfo;
import com.cqt.model.unicom.entity.UnicomCommonEnum;
import com.cqt.redis.util.RedissonUtil;
import com.cqt.unicom.common.HcodeCommon;
import com.cqt.unicom.config.nacos.NacosConfig;
import com.cqt.unicom.config.rabbitmq.RabbitMqSender;
import com.cqt.unicom.config.rabbitmq.UnicomRabbitMqConfig;
import com.cqt.unicom.mapper.PrivateCorpInteriorInfoMapper;
import com.cqt.unicom.mapper.PrivateNumberInfoMapper;
import com.cqt.unicom.properties.VccIdCheckProperties;
import com.cqt.unicom.util.UnicomUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * @author zhengsuhao
 * @date 2022/12/7
 */
@Api(tags = "联通集团总部(江苏)能力:话单推送服务实现")
@Slf4j
@Service
@RequiredArgsConstructor
public class UnicomCallListPushServiceImpl implements UnicomCallListPushService {


    private final RedissonUtil redissonUtil;


    private final PrivateCorpInteriorInfoMapper privateCorpInteriorInfoMapper;


    private final PrivateNumberInfoMapper numberInfoMapper;


    private final RabbitMqSender rabbitMqSender;


    private final NacosConfig nacosConfig;


    private final RestTemplate restTemplate;

    @Resource(name = "saveExecutor")
    private ThreadPoolTaskExecutor saveExecutor;


    private final VccIdCheckProperties checkProperties;

    private final HcodeCommon hcodeCommon;

    /**
     * @param callListPushDTO 联通集团总部(江苏)话单推送入参
     * @return CustomerReceivesDataInfo
     */
    @ApiOperation("集团报文转换客户接受报文服务实现")
    @Override
    public CustomerReceivesDataInfo getCustomerReceivesDataInfo(CallListPushDTO callListPushDTO) {
        if (log.isInfoEnabled()) {
            log.info("江苏联通送集团话单原始入参：{}", JSON.toJSONString(callListPushDTO));
        }
        String stopCallTime = UnicomUtil.timestampConversion(callListPushDTO.getReleaseTime());
        String uuid = callListPushDTO.getCallId().substring(0, 10);
        CustomerReceivesDataInfo customerReceivesDataInfo = new CustomerReceivesDataInfo();
        //插入流水号
        String bindingId = callListPushDTO.getBindingId();
        if (StringUtil.isBlank(bindingId)) {
            //如果绑定ID、额外数据均为空，则返回报错
            if (!StringUtil.isBlank(callListPushDTO.getAdditionalData())) {
                HashMap<String, String> hashMap = JSON.parseObject(callListPushDTO.getAdditionalData(), HashMap.class);
                bindingId = hashMap.get("bind_id");
            }
        }
        customerReceivesDataInfo.setMessageId(bindingId);
        //插入呼叫中心callId
        customerReceivesDataInfo.setAcrCallId(callListPushDTO.getCallId());
        //插入主叫电话 A
        customerReceivesDataInfo.setCallInNum(callListPushDTO.getPhoneNumberA());
        //插入被叫电话B
        customerReceivesDataInfo.setCalledNum(callListPushDTO.getPhoneNumberB());
        //插入中间联系号码（中间号码）X


        //插入主叫话单流水号（通话结束时间+截取10位callid字段）
        customerReceivesDataInfo.setCallerStreamNo(stopCallTime + uuid);
        //插入主叫应答时间
        String startCallerTime = null;
        if (!StringUtil.isBlank(callListPushDTO.getCallTime())) {
            startCallerTime = UnicomUtil.timestampConversion(callListPushDTO.getCallTime());

        }
        customerReceivesDataInfo.setStartCallTime(startCallerTime);
        //插入通话结束时间
        customerReceivesDataInfo.setStopCallTime(stopCallTime);
        //插入主叫通话时长
        int timeLag = UnicomUtil.talkTime(callListPushDTO.getStartTime(), callListPushDTO.getReleaseTime());
        customerReceivesDataInfo.setDuration(timeLag);
        //插入主叫费用
        customerReceivesDataInfo.setCallCost(0);
        //插入主叫结束原因
        log.info("江苏联通送通话状态：{}", callListPushDTO.getReleaseCause());
        String endCause = UnicomUtil.searchCalledRelCause(callListPushDTO.getReleaseCause());
        if (UnicomCommonEnum.ONE.getValue().equals(endCause) && timeLag > 0) {
            endCause = "1";
        } else if (UnicomCommonEnum.ONE.getValue().equals(endCause) && timeLag == 0) {
            endCause = "9";
        }
        customerReceivesDataInfo.setCallerRelCause(endCause);
        customerReceivesDataInfo.setCallerrelCause(endCause);
        log.info("转换后江苏联通送通话状态：{}", endCause);
        //插入主叫结束的原始原因值
        customerReceivesDataInfo.setCallerOriRescode("");
        //插入被叫话单流水号（通话结束时间+截取10位callid字段）
        customerReceivesDataInfo.setCalledStreamNo(stopCallTime + uuid);
        //插入被叫应答时间
        String startCallTime = UnicomUtil.timestampConversion(callListPushDTO.getStartTime());
        customerReceivesDataInfo.setStartCalledTime(startCallTime);
        //插入被叫通话时长
        customerReceivesDataInfo.setCalledDuration(timeLag);
        //插入被叫通话费用
        customerReceivesDataInfo.setCalledCost(0);
        //插入通话结束原因
        customerReceivesDataInfo.setReleaseCause(Integer.parseInt(endCause));
        //插入被叫结束的原始原因值
        customerReceivesDataInfo.setCalledOriRescode(0);
        //插入通话录音路径
        log.info("江苏联通送通话录音地址：{}", callListPushDTO.getRecordingUrl());
        customerReceivesDataInfo.setSrfmsgid(callListPushDTO.getRecordingUrl());
        if(StrUtil.isNotBlank (callListPushDTO.getPhoneNumberY ())){
            //显示号
            customerReceivesDataInfo.setDisplayNumber(callListPushDTO.getPhoneNumberY());
            //插入计费号码
            customerReceivesDataInfo.setChargeNumber(callListPushDTO.getPhoneNumberY());
            //插入被叫来电号码
            customerReceivesDataInfo.setCalledDisplayNum(callListPushDTO.getPhoneNumberY());
        }else {
            //显示号
            customerReceivesDataInfo.setDisplayNumber(callListPushDTO.getPhoneNumberX());
            //插入计费号码
            customerReceivesDataInfo.setChargeNumber(callListPushDTO.getPhoneNumberX());
            //插入被叫来电号码
            customerReceivesDataInfo.setCalledDisplayNum(callListPushDTO.getPhoneNumberX());
        }
        //插入主叫释放Reason
        customerReceivesDataInfo.setCallerRelReason(callListPushDTO.getReleaseDirection());
        //插入被叫释放Reason
        customerReceivesDataInfo.setCalledRelReason(callListPushDTO.getReleaseDirection());
        //插入媒体服务器名称
        String serverUrl = null;
        if (!StringUtil.isBlank(callListPushDTO.getRecordingUrl())) {
            serverUrl = UnicomUtil.extractServerUrl(callListPushDTO.getRecordingUrl());
        }
        customerReceivesDataInfo.setMsserver(serverUrl);

        //插入主叫来电号码
        customerReceivesDataInfo.setCallerDisplayNum(callListPushDTO.getPhoneNumberX());
        //插入业务关键字
        customerReceivesDataInfo.setServicekey("900007");
        //插入Servicekey为900007时，中间联系号码（中间号码）
        customerReceivesDataInfo.setMiddleNumber(callListPushDTO.getPhoneNumberX());
        //插入ab通话开始时间格:yyyymmddhhmmss
        customerReceivesDataInfo.setAbStartCallTime(startCallTime);
        //插入ab通话结束时间格:yyyymmddhhmmss
        customerReceivesDataInfo.setAbStopCallTime(stopCallTime);
        //插入主叫通话时长
        customerReceivesDataInfo.setCallerDuration(String.valueOf(timeLag));
        //插入中间号应答时间(主叫呼叫时间)
        customerReceivesDataInfo.setMiddleStartTime(startCallerTime);
        //插入中间号转呼时间(通话开始时间)
        customerReceivesDataInfo.setMiddleCallTime(startCallerTime);
        //插入Servicekey为900008,900009,900010,900013时，放音模式（0：语音验证码语音文件； 2：语音通知语音文件；3：语音通知TTS合成；）
        customerReceivesDataInfo.setPalyMode("");
        //插入开始呼叫时间
        customerReceivesDataInfo.setCallOutTime(startCallerTime);
        //插入按键收号
        customerReceivesDataInfo.setDtmfKey(callListPushDTO.getDgts());
        //插入振铃时长
        customerReceivesDataInfo.setCallRingTime(UnicomUtil.timestampConversion(callListPushDTO.getRingingTime()));
        //插入被叫应答时间
        customerReceivesDataInfo.setCallAnswerTime(startCallTime);
        //插入计费个数
        customerReceivesDataInfo.setCostCount("0");
        //插入计费时长
        customerReceivesDataInfo.setDuration(timeLag);
        //插入计费数量
        customerReceivesDataInfo.setCostCount("0");
        //插入Servicekey为900005时，客户端会话标识，
        //Servicekey为900014时，为imei
        customerReceivesDataInfo.setClientId("");
        //插入Servicekey为900005时，会议Id
        //Servicekey为900014时，为agentId
        customerReceivesDataInfo.setConfId("");
        //插入Servicekey为900005时，Sessionid
        customerReceivesDataInfo.setSid("");
        //插入企业标识
        String vccid = getVccid(callListPushDTO.getPhoneNumberX ());


        customerReceivesDataInfo.setVccId(vccid);
        //插入uuId
        customerReceivesDataInfo.setUuId(callListPushDTO.getCallId());
        //插入客户话单回调地址
        //通过VCCID查询客户URL
        String customerUrl = null;
        try {
            String corpInfo = redissonUtil.getString(String.format(PrivateCacheConstant.CORP_INTERIOR_INFO, vccid, 900007));
            //判读缓存获取是否非空
            if (!StringUtil.isBlank(corpInfo)) {
                PrivateCorpInteriorInfo privateCorpInteriorInfo = JSON.parseObject(corpInfo, PrivateCorpInteriorInfo.class);
                //判读字段是否非空
                if (!StringUtil.isBlank(privateCorpInteriorInfo.getVoiceCdrUrl())) {
                    customerUrl = privateCorpInteriorInfo.getVoiceCdrUrl();
                }
            }
            //redis找不到查数据库
            if (StrUtil.isBlank(customerUrl)) {
                customerUrl = privateCorpInteriorInfoMapper.selectByVccId(vccid, "900007");
            }
        } catch (Exception e) {
            log.error("操作异常: ", e);
        }
        customerReceivesDataInfo.setCallAcrUrl(StringUtil.isBlank(customerUrl) ? "" : customerUrl);
        //插入被叫结束原因值
        customerReceivesDataInfo.setCalledRelCause(endCause);
        //插入振铃时间
        customerReceivesDataInfo.setKey1(UnicomUtil.timestampConversion(callListPushDTO.getRingingTime()));
        //插入中间号区号
        String tel = customerReceivesDataInfo.getDisplayNumber().substring(0, 7);
        String areaCode = redissonUtil.getString("h_" + tel);
        customerReceivesDataInfo.setKey2(areaCode);
        //插入A路入SN时间(A打X呼叫时间)
        customerReceivesDataInfo.setKey3(UnicomUtil.timestampConversion(callListPushDTO.getCallTime()));
        //插入userData
        customerReceivesDataInfo.setKey7(StringUtil.isBlank(callListPushDTO.getAdditionalData()) ? "" : callListPushDTO.getAdditionalData());
        return customerReceivesDataInfo;
    }

    /**
     * @param customerReceivesDataInfo 话单客户接受报文信息
     * @return String
     */

    @Override
    @ApiOperation("报文放入消息队列服务实现")
    public String setMessageQueue(CustomerReceivesDataInfo customerReceivesDataInfo) {
        log.info("队列接收到江苏联通通话话单： {}", JSONUtil.toJsonStr(customerReceivesDataInfo));
        String chargeType=hcodeCommon.getChargeType(customerReceivesDataInfo.getCalledNum(), customerReceivesDataInfo.getDisplayNumber());
        Callstat callstat = UnicomUtil.buildCallStat(customerReceivesDataInfo, customerReceivesDataInfo.getKey2(),hcodeCommon.getNumberCode(customerReceivesDataInfo.getCalledNum ()), nacosConfig.getSupplierId(),chargeType);
        log.info("队列转换后的江苏联通通话话单:{}", JSONUtil.toJsonStr(callstat));
        JSONObject json = new JSONObject(callstat);
        saveExecutor.execute(() -> {
            try {
                rabbitMqSender.send(json, ThirdConstant.ICCPCDRSAVEEXCHANGE, ThirdConstant.ICCPCDRSAVEROUTEKEY, 0);
            } catch (Exception e) {
                log.error("队列调用异常", e);
            }
        });
        return UnicomCommonEnum.SCUESS.getValue();
    }

    /**
     * @param customerReceivesDataInfo 话单客户接受报文信息
     * @return String
     */
    @Override
    @ApiOperation("调用COMMCdrPUSH服务")
    public String pushCommCdrPushService(CustomerReceivesDataInfo customerReceivesDataInfo) {
        String customerUrl = customerReceivesDataInfo.getCallAcrUrl();
        try {
            log.info("客户url地址：{}", customerUrl);
            if (StrUtil.isBlank(customerUrl)) {
                log.info("查找不到客户绑定url{}", customerUrl);
                return UnicomCommonEnum.FAIL.getValue();
            }
            //处理通话录音下载url字段
            String srfmsgid = customerReceivesDataInfo.getSrfmsgid();
            if(StrUtil.isNotBlank (srfmsgid)){
                for (int i = 0; i < 3; i++) {
                    srfmsgid = srfmsgid.substring(srfmsgid.indexOf("/") + 1);
                }
            }
            customerReceivesDataInfo.setSrfmsgid(srfmsgid);
            CommResult commResult;
            if (!checkProperties.getCommon().contains(customerReceivesDataInfo.getVccId())){
                customerReceivesDataInfo.setCallAcrUrl (checkProperties.getThirdUrl ());
                commResult=  sendPrivatePush(customerReceivesDataInfo);
            }else {
                //调用COMMCdrPUSH服务
                log.info("送COMMCdrPUSH服务客户报文：{}", JSON.toJSONString(customerReceivesDataInfo));
                commResult = restTemplate.postForObject(customerUrl, customerReceivesDataInfo, CommResult.class);
                log.info("调用COMMCdrPUSH服务，返回结果：{}", (commResult != null ? commResult.toString() : null));
            }

            if (commResult != null && UnicomCommonEnum.SCUESS.getValue().contains(commResult.getReason())) {
                return UnicomCommonEnum.SCUESS.getValue();
            }
            rabbitMqSender.send(customerReceivesDataInfo, UnicomRabbitMqConfig.COMM_PUSH_DELAYED_EXCHANGE, UnicomRabbitMqConfig.COMM_PUSH_DELAYED_ROUTING, 0);
            return UnicomCommonEnum.FAIL.getValue();

        } catch (RestClientException e) {
            log.error("推送commpush服务异常: ", e);
            //将服务丢入延迟队列
            rabbitMqSender.send(customerReceivesDataInfo, UnicomRabbitMqConfig.COMM_PUSH_DELAYED_EXCHANGE, UnicomRabbitMqConfig.COMM_PUSH_DELAYED_ROUTING, 0);
            return UnicomCommonEnum.FAIL.getValue();
        }
    }

    public CommResult sendPrivatePush(CustomerReceivesDataInfo customerReceivesDataInfo){
        String url = customerReceivesDataInfo.getCallAcrUrl();
        PrivateBillInfo privateBillInfo = getPrivateBillInfo(customerReceivesDataInfo);
        if(StrUtil.isBlank (privateBillInfo.getBindId ())){
            CommResult commResult = new CommResult();
            commResult.setReason (UnicomCommonEnum.SCUESS.getValue ());
            return commResult;
        }
        log.info("推送通用PUSH服务客户报文：{}", JSON.toJSONString(privateBillInfo));
        CommResult commResult = restTemplate.postForObject(url, privateBillInfo, CommResult.class);
        log.info("调用COMMCdrPUSH服务，返回结果："+commResult);
        return commResult;

    }

    /**
     * 查询企业标识
     * @param phoneNumberX X号码
     * @return 企业标识
     */
    @Override
    public String getVccid(String phoneNumberX){
        String vccid="";
        try {
            vccid = redissonUtil.getString(String.format(PrivateCacheConstant.X_NUMBER_BELONG_VCC_ID_KEY, phoneNumberX));
            if (StringUtil.isEmpty(vccid)) {
                PrivateNumberInfo privateNumberInfo = numberInfoMapper.selectById(phoneNumberX);
                vccid = privateNumberInfo.getVccId();
            }
        } catch (Exception e) {
            log.error("操作异常: ", e);
        }
        return vccid;
    }

    @Override
    public PrivateBillInfo getPrivateBillInfo(CustomerReceivesDataInfo acr) {
        PrivateBillInfo privateBillInfo = new PrivateBillInfo();
        privateBillInfo.setTelA(acr.getCallInNum());
        privateBillInfo.setTelB(acr.getCalledNum());
        privateBillInfo.setAppKey(acr.getVccId());
        privateBillInfo.setCalloutTime (strDateFormat(acr.getCallOutTime ()));
        privateBillInfo.setConnectTime(strDateFormat(acr.getAbStartCallTime()));
        privateBillInfo.setAlertingTime(strDateFormat(acr.getKey1()));
        privateBillInfo.setReleaseTime(strDateFormat(acr.getStopCallTime()));
        privateBillInfo.setCallDuration(acr.getCalledDuration());
        privateBillInfo.setCallResult(acr.getReleaseCause());
        privateBillInfo.setRecordFileUrl(StringUtils.isEmpty(acr.getSrfmsgid()) ? "" : acr.getMsserver() + acr.getSrfmsgid());
        privateBillInfo.setTelX(acr.getCallerDisplayNum());
        privateBillInfo.setTelY(acr.getCalledDisplayNum());
        privateBillInfo.setRecordId(acr.getUuId());
        privateBillInfo.setBeginTime(strDateFormat(acr.getKey3()));
        privateBillInfo.setBindId (acr.getMessageId ());
        privateBillInfo.setAreaCode (acr.getKey2 ());
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(acr.getKey7());
        if (jsonObject!=null){
            privateBillInfo.setUserData (jsonObject.getString("userData"));
        }
        privateBillInfo.setExt(StringUtils.isEmpty(acr.getDtmfKey()) ? "" : acr.getDtmfKey());
        privateBillInfo.setServiceCode(StringUtils.isEmpty(acr.getDtmfKey()) ? ServiceCodeEnum.AXB.getCode() : ServiceCodeEnum.AXE.getCode() );
        if (StringUtil.isNotEmpty(acr.getSrfmsgid())){
            if (StringUtil.isNotEmpty(acr.getStartCallTime())){
                privateBillInfo.setRecordStartTime(strDateFormat(acr.getStartCallTime()));
            }
            privateBillInfo.setRecordFlag(1);
        }else {
            privateBillInfo.setRecordFlag(0);

        }

        return privateBillInfo;
    }

    public static String strDateFormat(String dateStr) {
        DateFormat sourceFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date date = sourceFormat.parse(dateStr);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.format(date);
        } catch (Exception e) {
            log.error("时间转换异常："+e);
            return dateStr;
        }
    }




    @ApiOperation("获取手机号码H码")
    private   String checkMobilePhone(String number) {
        if (number == null) {
            return "";
        }
        String hcode = "";
        String pattern = "^0?1[3|4|5|6|7|8|9][\\d]{9}";
        try {
            boolean isPhone = Pattern.matches(pattern, number);
            if (isPhone) {
                if (number.startsWith("0")) {
                    hcode = redissonUtil.getString ("h_"+number.substring(1, 8));
                    log.info("获取到手机H码：" + hcode);

                } else {
                    try {
                        hcode = redissonUtil.getString("h_"+number.substring(0, 7));
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        log.error("报错号码：" + number.substring(0, 7));
                    }

                }
                if (hcode == null || "".equals(hcode)) {
                    log.error("获取到不存在H码的手机号码" + number);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hcode;
    }


}
