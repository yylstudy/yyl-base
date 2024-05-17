package com.cqt.unicom.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.enums.PushTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.model.bind.query.BindInfoQuery;
import com.cqt.model.bind.vo.BindInfoVO;

import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.model.push.entity.AcrRecordOrg;
import com.cqt.model.push.entity.Callstat;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.model.unicom.dto.CallListPushDTO;
import com.cqt.redis.util.RedissonUtil;
import com.cqt.unicom.PrivateNumberChinaUnicomApplication;
import com.cqt.unicom.cache.UnicomLocalCacheService;
import com.cqt.unicom.config.RabbitMqConfig;
import com.cqt.unicom.dao.FailMessageDao;
import com.cqt.unicom.dto.QueryAxeBindDTO;
import com.cqt.unicom.dto.QueryBindDTO;
import com.cqt.unicom.dto.UnicomCdrDTO;
import com.cqt.unicom.dto.UnicomEventDTO;
import com.cqt.unicom.mapper.PrivateNumberInfoMapper;
import com.cqt.unicom.properties.QueryBindProperties;
import com.cqt.unicom.properties.RepushProperties;
import com.cqt.unicom.rabbitmq.MqSender;
import com.cqt.unicom.rabbitmq.RabbitService;
import com.cqt.unicom.util.BankCallChannelUtil;
import com.cqt.unicom.util.ConvertUtil;
import com.cqt.unicom.vo.ResultErrVO;
import com.cqt.unicom.vo.UnicomAxbBindInfoVO;
import com.cqt.unicom.vo.UnicomAxeFirstBindInfoVO;
import com.cqt.unicom.vo.UnicomAxeSecondBindInfoVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static javafx.scene.input.KeyCode.S;

/**
 * @author huweizhong
 * date  2023/7/6 9:19
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnicomBindInfoService {

    private final QueryBindProperties queryBindProperties;

    private final RedissonUtil redissonUtil;

    private final BankCallChannelUtil bankCallChannelUtil;

    private final PrivateNumberInfoMapper privateNumberInfoMapper;

    private final RabbitService rabbitService;

    private final MqSender mqSender;

    private final FailMessageDao failMessageDao;

    private final RepushProperties repushProperties;


    private final ConvertUtil convertUtil;

    private final PrivateNumberBindInfoService privateNumberBindInfoService;



    public Object queryAxbBindInfo(QueryBindDTO queryBindDTO)  {
        log.info("联通请求绑定关系原始请求(AXB):"+JSONObject.toJSONString(queryBindDTO));
        String substring = queryBindDTO.getCallId().substring(0, 32);
        queryBindDTO.setCallId(substring);
        PrivateNumberInfo privateNumberInfo = getPrivateNumberInfo(queryBindDTO.getCallee());
        if (ObjectUtil.isEmpty(privateNumberInfo)){
            return ResultErrVO.fail("未查询到号码信息");
        }
        if (!queryBindProperties.getVccIds().contains(privateNumberInfo.getVccId())){
            return  privateNumberBindInfoService.getNumberBindingQuery(queryBindDTO);
        }
        redissonUtil.setString("ChinaUnicomCallId_"+queryBindDTO.getCallId(),queryBindDTO.getCallee(),600L, TimeUnit.SECONDS);
        Map<String, String> urlMap = queryBindProperties.getAxbUrlMap();
        String url = urlMap.get(privateNumberInfo.getVccId());
        try (HttpResponse httpResponse = HttpRequest.post(url)
                .timeout(10000)
                .body(JSONObject.toJSONString(queryBindDTO))
                .execute()) {
            if (httpResponse.isOk()) {
                String body = httpResponse.body();
                JSONObject resObject = JSONObject.parseObject(body);
                int code = resObject.getInteger("code");
                String type = resObject.getString("type");
                String message = resObject.getString("message");
                if (code == 0) {
                    if ("AXB".equals(type)){
                        UnicomAxbBindInfoVO unicomAxbBindInfoVO = UnicomAxbBindInfoVO.buildUnicomBindInfoVO(message);
                        if (org.apache.commons.lang.StringUtils.isNotEmpty((unicomAxbBindInfoVO.getData().getConnectAudioToUp()))){
                            unicomAxbBindInfoVO.getData().setConnectAudioToUp(UnicomLocalCacheService.AUDIO_CODE_CACHE.get(unicomAxbBindInfoVO.getData().getConnectAudioToUp()));
                        }
                        if (org.apache.commons.lang.StringUtils.isNotEmpty((unicomAxbBindInfoVO.getData().getConnectAudioToDown()))){
                            unicomAxbBindInfoVO.getData().setConnectAudioToDown(UnicomLocalCacheService.AUDIO_CODE_CACHE.get(unicomAxbBindInfoVO.getData().getConnectAudioToDown()));
                        }
                        if (org.apache.commons.lang.StringUtils.isNotEmpty((unicomAxbBindInfoVO.getData().getAudioCode()))){
                            unicomAxbBindInfoVO.getData().setAudioCode(UnicomLocalCacheService.AUDIO_CODE_CACHE.get(unicomAxbBindInfoVO.getData().getAudioCode()));
                        }
                        log.info("返回联通AXB绑定关系：" + JSONObject.toJSONString(unicomAxbBindInfoVO));
                        return  unicomAxbBindInfoVO;
                    }else {
                        log.info("返回联通AXE第一次查询绑定关系：" + message);
                        return UnicomAxeFirstBindInfoVO.buildUnicomBindInfoVO(message);
                    }
                } else {
                    return ResultErrVO.fail("查询绑定返回错误码："+code);
                }
            }
            log.info("callId=>{}|查询绑定关系失败:{}",queryBindDTO.getCallId(),httpResponse.body());
            return ResultErrVO.fail("查询绑定关系失败");

        } catch (Exception e) {
            log.error("参数:{},查询绑定关系异常:{}",JSONObject.toJSONString(queryBindDTO),e);
            return ResultErrVO.fail("查询绑定关系异常");
        }
    }



    public Object queryAxeSecond(QueryAxeBindDTO queryAxeBind)  {
        log.info("联通请求绑定关系原始请求(AXE):"+JSONObject.toJSONString(queryAxeBind));
        String number = redissonUtil.getString("ChinaUnicomCallId_" + queryAxeBind.getCallId());
        PrivateNumberInfo privateNumberInfo = getPrivateNumberInfo(number);
        if (ObjectUtil.isEmpty(privateNumberInfo)){
            return ResultErrVO.fail("未查询到号码信息");
        }
        Map<String, String> urlMap = queryBindProperties.getAxeUrlMap();
        String url = urlMap.get(privateNumberInfo.getVccId());
        try (HttpResponse httpResponse = HttpRequest.post(url)
                .timeout(10000)
                .body(JSONObject.toJSONString(queryAxeBind))
                .execute()) {
            if (httpResponse.isOk()) {
                String body = httpResponse.body();
                JSONObject resObject = JSONObject.parseObject(body);
                int code = resObject.getInteger("code");
                String message = resObject.getString("message");
                if (code == 0) {
                    UnicomAxeSecondBindInfoVO unicomAxeSecondBindInfoVO = JSONUtil.toBean(message, UnicomAxeSecondBindInfoVO.class);
                    log.info("返回联通AXE绑定关系：" + unicomAxeSecondBindInfoVO);
                    return unicomAxeSecondBindInfoVO;

                } else {
                    return ResultErrVO.fail("查询绑定返回错误码："+code);
                }
            }
            log.info("callId=>{}|查询绑定关系失败:{}",queryAxeBind.getCallId(),httpResponse.body());
            return ResultErrVO.fail("查询绑定关系失败");

        } catch (Exception e) {
            log.error("参数:{},查询绑定关系异常:{}",JSONObject.toJSONString(queryAxeBind),e);
            return ResultErrVO.fail("查询绑定关系异常");
        }
    }

    public void unicomBill(UnicomCdrDTO cdrDTO){
        PrivateFailMessage privateFailMessage = new PrivateFailMessage();
        String substring = cdrDTO.getId().substring(0, 32);
        cdrDTO.setId(substring);
        PrivateNumberInfo privateNumberInfo = getPrivateNumberInfo(cdrDTO.getInboundCallee());
        if (ObjectUtil.isEmpty(privateNumberInfo)){
            log.info("未查询到号码配置信息："+cdrDTO.getInboundCallee());
            return;
        }
        AcrRecordOrg convert = convert(cdrDTO, privateNumberInfo.getVccId());
        privateFailMessage.setVccid(privateNumberInfo.getVccId());
        privateFailMessage.setType(PushTypeEnum.BILL.name());
        privateFailMessage.setIp(PrivateNumberChinaUnicomApplication.ip);
        privateFailMessage.setBody(JSON.toJSONString(convert));
        privateFailMessage.setNum(0);
        privateFailMessage.setId(cdrDTO.getId());
        push(privateFailMessage);
        Callstat callstat = toCallstat(convert, privateNumberInfo.getVccId());
        log.info("入库话单："+JSONObject.toJSONString(callstat));
        mqSender.sendCallStat(callstat);
    }

    public void push(PrivateFailMessage failMessage)  {
        Map<String, String> urlMap = queryBindProperties.getBillUrlMap();
        String url = urlMap.get(failMessage.getVccid());
        log.info("话单推送body=>{},url=>{}",failMessage.getBody(),url);
        try (HttpResponse httpResponse = HttpRequest.post(url)
                .timeout(10000)
                .body(failMessage.getBody())
                .execute()) {
            if (httpResponse.isOk()) {
                log.info("话单推送成功，httpResponseBody=>{},url=>{},推送报文=>{}",httpResponse.body(),url,failMessage.getBody());
                return;
            }
            log.info("话单推送返回失败，httpResponseBody=>{},url=>{},推送报文=>{}",httpResponse.body(),url,failMessage.getBody());
        }catch (Exception e){
            log.info("话单推送异常，error=>{},url=>{},推送报文=>{}",e,url,failMessage.getBody());
            catchBillException(failMessage, e.toString());
        }
    }



    private PrivateNumberInfo getPrivateNumberInfo(String number){
        String numberInfoKey = PrivateCacheUtil.getNumberInfo(number);
        PrivateNumberInfo privateNumberInfo = null;
        try {
            String numberInfo = redissonUtil.getString(numberInfoKey);
            privateNumberInfo = JSONObject.parseObject(numberInfo, PrivateNumberInfo.class);
            if (ObjectUtil.isEmpty(privateNumberInfo)){
                privateNumberInfo = privateNumberInfoMapper.selectById(number);
                redissonUtil.setString(numberInfoKey,JSONObject.toJSONString(privateNumberInfo));
            }
        }catch (Exception e){
            log.error("number=>{}|查询平台号码信息异常：{}",number,e);
            privateNumberInfo = privateNumberInfoMapper.selectById(number);
        }
        return privateNumberInfo;
    }


    public void pushEvent(PrivateFailMessage failMessage) {
        UnicomEventDTO eventDTO = JSONObject.parseObject(failMessage.getBody(), UnicomEventDTO.class);
        log.info("通话状态："+JSONObject.toJSONString(eventDTO));
        String number;
        if (eventDTO.getFlag() == 1){
            number = eventDTO.getCallee();
        }else {
            number = eventDTO.getCaller();
        }
        PrivateNumberInfo privateNumberInfo = getPrivateNumberInfo(number);
        if (ObjectUtil.isEmpty(privateNumberInfo)){
            log.info("未查询到号码配置信息：" + number);
            return;
        }
        failMessage.setVccid(privateNumberInfo.getVccId());
        failMessage.setType(toEventType(eventDTO.getFlag()));
        Map<String, String> urlMap = queryBindProperties.getEventUrlMap();
        String url = urlMap.get(privateNumberInfo.getVccId());
        try (HttpResponse httpResponse = HttpRequest.post(url)
                .timeout(10000)
                .body(JSONObject.toJSONString(eventDTO))
                .execute()) {
            if (httpResponse.isOk()) {
                log.info("通话状态推送成功，httpResponseBody=>{},url=>{},推送报文=>{}",httpResponse.body(),url,JSONObject.toJSONString(eventDTO));
                return;
            }
            log.info("通话状态推送返回失败，httpResponseBody=>{},url=>{},推送报文=>{}",httpResponse.body(),url,JSONObject.toJSONString(eventDTO));
        }catch (Exception e){
            log.info("通话状态推送异常，error=>{},url=>{},推送报文=>{}",e,url,JSONObject.toJSONString(eventDTO));
            catchBillException(failMessage, e.toString());
        }
    }

    private void catchBillException(PrivateFailMessage failMessage, String e) {
        try{
            log.info("推送请求超时或无响应，push_type=>{}，错误信息:{}", failMessage.getType(), e);
            // 队列创建
            String queueName = String.format(RabbitMqConfig.PRIVATE_PUSH_DELAY_QUEUE, failMessage.getVccid());
            rabbitService.createQueue(queueName);
            // 重推次数小于规定次数,重推次数加一,发送到mq延迟队列
            int currentNum = failMessage.getNum();
            int rePush = repushProperties.getRetryNum();
            int rePushTime = repushProperties.getRetryMinute();
            if (currentNum < rePush) {
                failMessage.setNum(currentNum + 1);
                mqSender.sendLazy(failMessage, queueName, rePushTime);
                log.info("重推未超过次数，再次推送延迟队列。当前重推次数=>{}，push_type=>{}",  failMessage.getNum(), failMessage.getType());
            } else {
                // 超过重推次数入库不再重推
                failMessage.setErrMsg("重推超过次数");
                failMessage.setCreateTime(new Date());
                failMessageDao.insert(failMessage);
            }

        }catch (Exception e1){
            log.error("重推异常:" + e1);
        }


    }

    public void encrypt(String reqMsg) {
        log.info("联通加密前话单："+reqMsg);
        String s = bankCallChannelUtil.testBy1024(reqMsg);
        log.info("联通解密后话单："+s);
        UnicomCdrDTO unicomCdrDTO = JSONObject.parseObject(s, UnicomCdrDTO.class);
        unicomBill(unicomCdrDTO);
    }

    private String toEventType(Integer flag){
        if (flag == 1){
            return "callIn";
        }else if (flag == 2){
            return "ringing";
        } else if (flag == 3) {
            return "answered";
        } else if (flag == 6) {
            return "callOut";
        }else if (flag == 4){
            return "discontected";
        }
        return "";
    }

    private AcrRecordOrg convert(UnicomCdrDTO cdrDTO,String vccId){
        AcrRecordOrg acrRecordOrg = new AcrRecordOrg();
        acrRecordOrg.setAcrCallId(cdrDTO.getId());
        acrRecordOrg.setUuId(cdrDTO.getId());
        acrRecordOrg.setCallAcrUrl(cdrDTO.getRecordUrl());
        acrRecordOrg.setCallInNum(cdrDTO.getInboundCaller());
        acrRecordOrg.setCalledNum(cdrDTO.getOutboundCallee());
        acrRecordOrg.setDisplayNumber(cdrDTO.getInboundCallee());
        acrRecordOrg.setMiddleNumber(cdrDTO.getInboundCallee());
        acrRecordOrg.setStartCallTime(timeFormat(cdrDTO.getConnectTime()));
        acrRecordOrg.setMiddleStartTime(timeFormat(cdrDTO.getConnectTime()));
        acrRecordOrg.setMiddleCallTime(timeFormat(cdrDTO.getConnectTime()));
        acrRecordOrg.setStartCalledTime(timeFormat(cdrDTO.getConnectTime()));
        acrRecordOrg.setStopCallTime(timeFormat(cdrDTO.getEndTime()));
        acrRecordOrg.setAbStartCallTime(timeFormat(cdrDTO.getConnectTime()));
        acrRecordOrg.setAbStopCallTime(timeFormat(cdrDTO.getEndTime()));
        acrRecordOrg.setCallRingTime(timeFormat(cdrDTO.getRingTime()));
        acrRecordOrg.setCallAnswerTime(timeFormat(cdrDTO.getConnectTime()));
        acrRecordOrg.setDuration(String.valueOf(cdrDTO.getTalkTime()));
        acrRecordOrg.setCalledDuration(String.valueOf(cdrDTO.getTalkTime()));
        acrRecordOrg.setCallerDuration(String.valueOf(cdrDTO.getTalkTime()));
        acrRecordOrg.setCallerRelCause("1");
//        if (StringUtil.isNotEmpty(cdrDTO.getConnectTime())){
//            DateTime parse = DateUtil.parse(cdrDTO.getStartTime(), DatePattern.NORM_DATETIME_PATTERN);
//            DateTime parse1 = DateUtil.parse(cdrDTO.getConnectTime(), DatePattern.NORM_DATETIME_PATTERN);
//            long between = DateUtil.between(parse, parse1, DateUnit.SECOND);
//            acrRecordOrg.setCallerDuration(String.valueOf(between));
//        }
        if (StringUtils.isEmpty(cdrDTO.getTalkTime())){
            acrRecordOrg.setCalledDuration("0");
            acrRecordOrg.setDuration("0");
            acrRecordOrg.setCallerDuration("0");
            acrRecordOrg.setCallerRelCause("9");
        }

        acrRecordOrg.setReleaseCause(String.valueOf(cdrDTO.getReleaseDir()));
        acrRecordOrg.setCalledOriRescode("200");
        acrRecordOrg.setCallerDisplayNum(cdrDTO.getInboundCallee());
        acrRecordOrg.setCalledDisplayNum(cdrDTO.getOutboundCaller());
        BindInfoVO bindInfo = JSONObject.parseObject(cdrDTO.getuId(),BindInfoVO.class);
        acrRecordOrg.setKey7(cdrDTO.getuId());
        acrRecordOrg.setKey3(timeFormat(cdrDTO.getStartTime()));
        if (ObjectUtil.isNotEmpty(bindInfo)){
            acrRecordOrg.setMessageId(bindInfo.getBindId());
            acrRecordOrg.setKey2(bindInfo.getAreaCode());
        }else {
            String string = redissonUtil.getString("h_" + cdrDTO.getInboundCallee().substring(0, 7));
            acrRecordOrg.setKey2(string);
            acrRecordOrg.setMessageId("");
        }
        acrRecordOrg.setCallCost("0");
        acrRecordOrg.setCostCount("0");
        acrRecordOrg.setCallerStreamNo(timeFormat(cdrDTO.getEndTime())+cdrDTO.getId().substring(0,10));
        acrRecordOrg.setKey1(timeFormat(cdrDTO.getRingTime()));
        acrRecordOrg.setServicekey("900007");
        acrRecordOrg.setVccId(vccId);
        acrRecordOrg.setReleaseCause(getReleaseCause(cdrDTO.getReleaseCause()));
        acrRecordOrg.setCallerrelCause("1");

        acrRecordOrg.setCalledRelCause(getReleaseCause(cdrDTO.getReleaseCause()));
        if ("99".equals(getReleaseCause(cdrDTO.getReleaseCause())) && ObjectUtil.isNotEmpty(bindInfo)){
            acrRecordOrg.setReleaseCause("9");
        }
        acrRecordOrg.setDtmfKey(cdrDTO.getInteractDtmfValues());
        acrRecordOrg.setKey10(cdrDTO.getuId());
        if (StringUtil.isNotEmpty(cdrDTO.getRecordUrl())){
            String msserver = getMsServer(cdrDTO.getRecordUrl());
            acrRecordOrg.setMsserver(msserver);
            acrRecordOrg.setSrfmsgid(cdrDTO.getRecordUrl().replace(msserver,""));
        }else {
            acrRecordOrg.setMsserver("");
            acrRecordOrg.setSrfmsgid("");
        }
        return acrRecordOrg;
    }

    public Callstat toCallstat(AcrRecordOrg billInfo,String vccId)  {
        String chargeType = convertUtil.getChargeType(billInfo.getCallInNum(), billInfo.getCalledNum());
        String time  = billInfo.getAbStartCallTime();
        if (StringUtils.isEmpty(billInfo.getAbStartCallTime())){
            time = billInfo.getAbStopCallTime();
        }
        return Callstat.builder()
                .streamnumber(time+ UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16))
                .serviceid("")
                .servicekey("900007")
                .callersubgroup("")
                .calleesubgroup("")
                .callerpnp("")
                .calleepnp("")
                .msserver("")
                .areanumber("")
                .dtmfkey("")
                .recordPush("")
                .calltype("0")
                .callcost(0)
                .calledpartynumber(StringUtils.isEmpty(billInfo.getDisplayNumber()) ? "" : convertUtil.getNumberUn86(billInfo.getDisplayNumber()))
                .callingpartynumber(StringUtils.isEmpty(billInfo.getCallInNum())?"":convertUtil.getNumberUn86(billInfo.getCallInNum()))
                .chargemode("0")
                .specificchargedpar(StringUtils.isEmpty(billInfo.getDisplayNumber()) ? "" : convertUtil.getNumberUn86(billInfo.getDisplayNumber()))
                .translatednumber(StringUtils.isEmpty(billInfo.getCalledNum()) ? "" : convertUtil.getNumberUn86(billInfo.getCalledNum()))
                .startdateandtime(StringUtils.isEmpty(billInfo.getAbStartCallTime()) ? billInfo.getAbStopCallTime() : billInfo.getAbStartCallTime())
                .stopdateandtime(StringUtils.isEmpty(billInfo.getAbStopCallTime()) ? "" : billInfo.getAbStopCallTime())
                .duration(StringUtils.isEmpty(billInfo.getDuration()) ? "" : billInfo.getDuration())
                .chargeclass("102")
                .transparentparamet(StringUtils.isEmpty(billInfo.getMessageId()) ? "" : billInfo.getMessageId())
                .acrcallid(convertUtil.acrCallId(time))
                .oricallednumber(StringUtils.isEmpty(billInfo.getCallInNum()) ? "" : convertUtil.getNumberUn86(billInfo.getCallInNum()))
                .oricallingnumber(StringUtils.isEmpty(billInfo.getCalledNum()) ? "" : convertUtil.getNumberUn86(billInfo.getCalledNum()))
                .reroute("1")
                .groupnumber(vccId)
                .callcategory("1")
                .chargetype(StringUtils.isEmpty(chargeType) ? "" : chargeType)
                .acrtype("1")
                .videocallflag(convertUtil.videoCallFlag(billInfo.getCallAcrUrl(), Integer.valueOf(billInfo.getDuration())))
                .forwardnumber(StringUtils.isEmpty(billInfo.getAcrCallId()) ? "" : billInfo.getAcrCallId())
                .extforwardnumber(org.apache.commons.lang.StringUtils.isBlank(billInfo.getCallRingTime()) ? "" : billInfo.getCallRingTime())
                .srfmsgid(org.apache.commons.lang.StringUtils.isBlank(billInfo.getCallAcrUrl()) ? "" : billInfo.getCallAcrUrl())
                .begintime(StringUtils.isEmpty(billInfo.getAbStartCallTime()) ? billInfo.getAbStopCallTime() : billInfo.getAbStartCallTime())
                .releasecause(StringUtils.isEmpty(billInfo.getReleaseCause()) ? "" : billInfo.getReleaseCause())
                .releasereason(convertUtil.cnResultCode(Integer.valueOf(billInfo.getReleaseCause())).getDesc())
                .key5("1")
                .userpin("")
                .bNumFail("")
                .key3(StringUtils.isEmpty(String.valueOf(billInfo.getKey2())) ? "" : String.valueOf(billInfo.getKey2()))
                .key2(StringUtils.isEmpty(billInfo.getAbStartCallTime()) ? "" : billInfo.getAbStartCallTime())
                .key1("")
                .key4("")
                .build();
    }

    public String timeFormat(String time){
        if (StringUtils.isEmpty(time)){
            return "";
        }
        SimpleDateFormat format =  new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN);
        DateTime parse = DateUtil.parse(time, format);
        return DateUtil.format(parse,DatePattern.PURE_DATETIME_PATTERN);
    }

    public static void main(String[] args) {
        DateTime parse = DateUtil.parse("2023-11-11 12:12:12", DatePattern.NORM_DATETIME_PATTERN);
        DateTime parse1 = DateUtil.parse("2023-11-11 12:12:19", DatePattern.NORM_DATETIME_PATTERN);
        long between = DateUtil.between(parse, parse1, DateUnit.SECOND);
        System.out.println(between);
    }

    private String getMsServer(String s){
        String[] split = s.split("/");
        StringBuilder ms = new StringBuilder();
        int i = 0 ;
        for (String s1 : split) {
            i++;
            if (i<4){
                ms.append(s1).append("/");
            }
        }
        return ms.toString();
    }

    private String getReleaseCause(Integer res){
        if (res==11||res==13||res==14||res==12){
            return "1";
        } else if (res == 5){
            return "4";
        } else if (res == 26) {
            return "11";
        } else if (res == 24 || res == 25) {
            return "2";
        } else if (res == 20) {
            return "5";
        } else if (res == 23) {
            return "6";
        } else if (res == 21 ) {
            return "9";
        } else if (res == 22) {
            return "91";
        } else {
            return "99";
        }
    }
}
