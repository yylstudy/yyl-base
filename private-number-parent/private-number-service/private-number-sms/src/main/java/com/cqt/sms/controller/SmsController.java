package com.cqt.sms.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.model.push.entity.CdrResult;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.sms.dao.mapper.FailMessageDao;
import com.cqt.sms.model.dto.*;
import com.cqt.sms.model.entity.*;
import com.cqt.sms.mqRend.MqSender;
import com.cqt.sms.properties.MeituanProperties;
import com.cqt.sms.service.AnsycJob;
import com.cqt.sms.service.IPrivateCorpBusinessInfoService;
import com.cqt.sms.service.SmsPushService;
import com.cqt.sms.service.SmsService;
import com.cqt.sms.util.StringUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.TreeMap;
import java.util.UUID;

/**
 * @author youngder
 * @description TODO
 * @date 2022/2/24 8:14 PM
 */
@RestController
@Slf4j
@RefreshScope
public class SmsController {

    @Resource
    AnsycJob ansycJob;
    @Autowired
    private JedisCluster jedisCluster;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IPrivateCorpBusinessInfoService businessInfoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private FailMessageDao failMessageDao;

    @Autowired
    private MeituanProperties meituanProperties;



    @Resource
    private MqSender mqSender;
    @Value("${iccp.mapUrl}")
    private String MAP_URL;
    @Value("${iccp.getCallerUrl}")
    private String GET_CALLER_URL;
    @Value("${iccp.appKey}")
    private String APPKEY;
    @Resource
    private SmsService smsService;
    //行短主叫号码
    private final String INDUSTRY_SMS_NUMBER = "1069";
    //分机号分隔符
    private static final String EXTENSION_SEPARATOR = "#";
    //行业短信签名分隔符
    private static final String INDUSTRY_SMS_SIGNATURE_SEPARATOR = "】";
    private static final String INDUSTRY_SMS_IDENTIFICATION = "&industrySms=0";
    private static final String EXT_NUMBER = "&digitInfo=";
    //短信标记缓存
    private static final String SMS_TAG_CACHE = "sms_messageReference";

    @Autowired
    private SmsPushService smsPushService;

    @ApiOperation("接收上行短信")
    @PostMapping("/receiveSms")
    public void smsReceiver(@RequestBody SmsRequest smsRequest, HttpServletResponse response) {
        String uuid = StrUtil.uuid();
        ansycJob.testAsyn(response, JSONUtil.toJsonStr(Result.ok()));
        try {
            log.info("{}|接收到VCCIDSMS短信请求：{}", uuid, JSONUtil.toJsonStr(smsRequest));
            SmsRequestBody smsRequestBody = smsRequest.getBody();

            //添加短信条数判断
            String smsContent = smsRequestBody.getInContent();
            int smsNumber = StringUtil.getSmsNumber(smsContent);
            smsRequestBody.setSmsNumber(smsNumber);
            smsRequest.setBody(smsRequestBody);


            //String requestTime = DateUtil.now();
//            smsRequestBody.setRequestTime(requestTime);
            //获取发短信号码
            String aNumber = smsRequestBody.getaPhoneNumber();
            if (aNumber.startsWith("86")) {
                aNumber = aNumber.substring(2);
            }
            String content = smsRequestBody.getInContent();
            String bindInfoStr;
            MeituanSmsStatePush meituanSmsStatePush;
            String inPhoneNumber = smsRequestBody.getInPhoneNumber();
            String getCallerUrl = GET_CALLER_URL + "caller=" + aNumber + "&called=" + inPhoneNumber + "&vccId=" + smsRequestBody.getVccId() + "&callId=" + uuid + "&smsFlag=1";
//            String getCallerUrl = "http://172.16.251.36:18800/private-hmyc/api/v1/query/bindInfo?caller=" + aNumber + "&called=" + inPhoneNumber+ "&vccId=1007&callId="+uuid;
            //判断短信类型
            //普通AX短信内容：#分机号#+短信内容
            if (!aNumber.startsWith(INDUSTRY_SMS_NUMBER) && content.startsWith(EXTENSION_SEPARATOR)) {
                String extNum = content.split(EXTENSION_SEPARATOR)[1];
                smsRequestBody.setInContent(content.replaceAll(EXTENSION_SEPARATOR + extNum + EXTENSION_SEPARATOR, ""));
                getCallerUrl = getCallerUrl + EXT_NUMBER + extNum;
//                bindInfoStr = restTemplate.getForObject(getCallerUrl, String.class);
                //本地模拟测试
                log.info("{}|查询绑定关系url：{}", uuid, getCallerUrl);
                bindInfoStr = HttpUtil.get(getCallerUrl);
                //【美团外卖】#0123#短信内容 /#0123#【美团外卖】短信内容
            } else if (aNumber.startsWith(INDUSTRY_SMS_NUMBER) //主叫号码为1069
                    && (content.contains(INDUSTRY_SMS_SIGNATURE_SEPARATOR + EXTENSION_SEPARATOR) || content.startsWith(EXTENSION_SEPARATOR)) && content.split(EXTENSION_SEPARATOR)[1].length() == 4 && StrUtil.isNumeric(content.split(EXTENSION_SEPARATOR)[1])) {
                String extNum = content.split(EXTENSION_SEPARATOR)[1];
                //下发的短信内容去除分机号
                smsRequestBody.setInContent(content.replaceAll(EXTENSION_SEPARATOR + extNum + EXTENSION_SEPARATOR, ""));
                getCallerUrl = getCallerUrl + EXT_NUMBER + extNum + INDUSTRY_SMS_IDENTIFICATION;
//                bindInfoStr = restTemplate.getForObject(getCallerUrl, String.class);
                log.info("{}|查询绑定关系url：{}", uuid, getCallerUrl);
                bindInfoStr = HttpUtil.get(getCallerUrl);
            } else {
                log.info("{}|查询绑定关系url：{}", uuid, getCallerUrl);
                //bindInfoStr = restTemplate.getForObject(getCallerUrl, String.class);
                bindInfoStr = HttpUtil.get(getCallerUrl);
            }
            log.info("{}|查询AYB绑定关系url：{}", uuid, getCallerUrl);
            log.info("{}|查询到绑定关系：{}", uuid, bindInfoStr);
            BindInfo bindInfo = JSONUtil.toBean(bindInfoStr, BindInfo.class);
            if (bindInfo.getCode() == 0 && StrUtil.isNotEmpty(bindInfo.getCallNum())) {
                smsRequest.getBody().setOutPhoneNumber(bindInfo.getCallNum());
                smsRequest.getBody().setCalledNumber(bindInfo.getCalledNum());
                smsRequest.getBody().setBindId(bindInfo.getBindId());

                meituanSmsStatePush = getMeituanSmsStatePush(bindInfo, smsRequest, aNumber, inPhoneNumber, content, smsRequestBody.getVccId());
//                //短信黑白名单过滤
//                smsFilter(uuid, smsRequestBody.getInContent(), smsRequest, meituanSmsStatePush);
                // 敏感词匹配
//                boolean sensitiveWordsFilter = smsPushService.sensitiveWordsFilter(smsRequestBody.getInContent());
//                if (sensitiveWordsFilter) {
//                    log.info("msgId=>{}, vccId=>{}, 本次短信内容: {} 命中平台敏感词 不做转发!", uuid, smsRequestBody.getVccId (), smsRequestBody.getInContent());
//                    return ;
//                }
//                //黑名单匹配
//                boolean whiteblackWordsFilter = smsPushService.blackWordsFilter(smsRequestBody.getVccId (),  smsRequestBody.getInContent());
//                if (whiteblackWordsFilter) {
//                    log.info("msgId=>{}, vccId=>{}, 本次短信内容: {} 命中黑名单", uuid, smsRequestBody.getVccId (), smsRequestBody.getInContent());
//                    //白名单匹配
//                    boolean whiteWordCheckFlag = smsPushService.whiteWordsFilter(smsRequestBody.getVccId (),  smsRequestBody.getInContent());
//                    if (!whiteWordCheckFlag) {
//                        log.info("msgId=>{}, vccId=>{}, 本次短信内容: {} 命中黑名单，未命中白名单, 本次短信不做转发!", uuid, smsRequestBody.getVccId (), smsRequestBody.getInContent());
//                        return;
//                    }
//                }

                String isSendResult = sendSms(bindInfo.getBindId(), bindInfo.getType(), uuid, smsRequest);
                //判断短信发送成功与否
                meituanSmsStatePush.setSms_number(smsNumber);
                if (!"0".equals(isSendResult)) {
                    meituanSmsStatePush.setSms_number(0);
                }
                meituanSmsStatePush.setSms_result(Integer.parseInt(isSendResult));
            } else {
                //查询绑定关系失败不下发
                log.info("{}|无绑定关系或AXE第一次查询：{}", uuid, bindInfoStr);
                return;
            }
            //       VccIdConfigInfo vccIdConfigInfo = smsService.findVccIdConfigInfoByVccId(smsRequestBody.getVccId());
            PrivateCorpBusinessInfo vccIdConfigInfo = getVccInfo(smsRequestBody.getVccId());
            String smsPushUrl = vccIdConfigInfo.getSmsPushUrl();
            try {

                String json = JSONUtil.toJsonStr(meituanSmsStatePush);
                TreeMap paramsMap = JSON.parseObject(json, TreeMap.class);
                //获取sign
                meituanSmsStatePush.setSign(StringUtil.createSign(paramsMap, smsRequestBody.getVccId(), vccIdConfigInfo.getSecretKey()));
                String potsResult;
                if (smsRequestBody.getVccId().equals(meituanProperties.getVccId())){
                    MeiTuanSmsPush meiTuanSmsPush = toMeituanPush(meituanSmsStatePush);
                    String s = decodeBase64((String) bindInfo.getTransferData());
                    JSONObject jsonObject = JSONObject.parseObject(s);
                    if (ObjectUtil.isNotEmpty(jsonObject)){
                        meiTuanSmsPush.setTs(jsonObject.getLong("ts"));
                        meiTuanSmsPush.setSign(jsonObject.getString("sign"));
                    }
                    meiTuanSmsPush.setTransfer_time((int) (DateUtil.parseDateTime(meituanSmsStatePush.getTransfer_time()).getTime()/1000));
                    log.info("{}|推送美团报文：{}|url：{}", uuid, JSONUtil.toJsonStr(meiTuanSmsPush), smsPushUrl);
                    potsResult = HttpUtil.post(smsPushUrl, JSONUtil.toJsonStr(meiTuanSmsPush));
                    log.info(uuid + "|美团状态推送结果：" + potsResult);
                    MeiTuanResp meiTuanResp = JSONUtil.toBean(potsResult, MeiTuanResp.class);
                    PrivateFailMessage smsStatePush = new PrivateFailMessage();
                    smsStatePush.setId(StrUtil.uuid());
                    smsStatePush.setVccid(smsRequestBody.getVccId());
                    smsStatePush.setType("SMS");
                    smsStatePush.setIp(StringUtil.getLocalIpStr());
                    smsStatePush.setBody(JSONUtil.toJsonStr(meiTuanSmsPush));
                    if (meiTuanResp.getCode()!=0) {
                        //推送失败，写入mq
                        log.info(uuid + "|状态推送失败，写入mq进行重推");
                        catchPushException(smsStatePush);
                    }
                    return;
                }else {
                    log.info("{}|推送报文：{}|url：{}", uuid, JSONUtil.toJsonStr(meituanSmsStatePush), smsPushUrl);
                    potsResult = HttpUtil.post(smsPushUrl, JSONUtil.toJsonStr(meituanSmsStatePush));
                    log.info(uuid + "|状态推送结果：" + potsResult);
                }

                MeiTuanResp meiTuanResp = JSONUtil.toBean(potsResult, MeiTuanResp.class);
                //        SmsStatePush smsStatePush = new SmsStatePush();
                PrivateFailMessage smsStatePush = new PrivateFailMessage();
                smsStatePush.setId(StrUtil.uuid());
                smsStatePush.setVccid(smsRequestBody.getVccId());
//                smsStatePush.setUrl(smsPushUrl);
                smsStatePush.setType("SMS");
                smsStatePush.setIp(StringUtil.getLocalIpStr());
                smsStatePush.setBody(JSONUtil.toJsonStr(meituanSmsStatePush));
                if (!"success".equals(meiTuanResp.getMessage())) {
                    //推送失败，写入mq
                    log.info(uuid + "|状态推送失败，写入mq进行重推");
                    catchPushException(smsStatePush);
                }
            } catch (Exception e) {
                log.error(uuid + "|状态推送异常：" + e);
                //推送异常，写状态重推延时队列
                //        SmsStatePush smsStatePush = new SmsStatePush();
                PrivateFailMessage smsStatePush = new PrivateFailMessage();
                smsStatePush.setId(StrUtil.uuid());
                smsStatePush.setVccid(smsRequestBody.getVccId());
//                smsStatePush.setUrl(smsPushUrl);
                smsStatePush.setIp(StringUtil.getLocalIpStr());
                smsStatePush.setType("SMS");
                smsStatePush.setBody(JSONUtil.toJsonStr(meituanSmsStatePush));
                catchPushException(smsStatePush);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(uuid + "|未知异常" + e);
        }
    }

    public static String decodeBase64(String code) {
        try {
            return new String(Base64.decode(code), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.info("transferData解码失败，异常=>{}", e.getMessage());
            return "{}";
        }
    }
    public void catchPushException(PrivateFailMessage failMessage) {
        try {
            PrivateCorpBusinessInfo vccInfo = getVccInfo(failMessage.getVccid());
            String smsPushUrl = vccInfo.getSmsPushUrl();
            log.info("{}|重推报文|url：{}", JSONUtil.toJsonStr(failMessage.getBody()), smsPushUrl);
            String potsResult = HttpUtil.post(smsPushUrl, JSONUtil.toJsonStr(failMessage.getBody()));
            log.info("|状态推送结果：" + potsResult);
            MeiTuanResp meiTuanResp = JSONUtil.toBean(potsResult, MeiTuanResp.class);
            if (!"success".equals(meiTuanResp.getMessage())) {
                //推送失败，写入mq
                log.info("|状态推送失败，写入mq进行重推");
                // 重推次数小于规定次数,重推次数加一,发送到mq延迟队列
                int currentNum = failMessage.getNum();
                String businessKey = String.format(PrivateCacheConstant.CORP_BUSINESS_INFO, failMessage.getVccid());
                Object o = redisTemplate.opsForValue().get(businessKey);
                //重推次数
                Integer rePush = null;
                //重推间隔时间
                Integer rePushTime = null;
                if (o != null) {
                    PrivateCorpBusinessInfo privateCorpBusinessInfo = JSONUtil.toBean(JSONUtil.toJsonStr(o), PrivateCorpBusinessInfo.class);
                    rePush = privateCorpBusinessInfo.getPushRetryNum();
                    rePushTime = privateCorpBusinessInfo.getPushRetryMin();
                } else {
                    QueryWrapper<PrivateCorpBusinessInfo> wrapper = new QueryWrapper<>();
                    wrapper.eq("vcc_id", failMessage.getVccid());
                    PrivateCorpBusinessInfo privateCorpBusinessInfo = businessInfoService.getOne(wrapper);
                    rePush = privateCorpBusinessInfo.getPushRetryNum();
                    rePushTime = privateCorpBusinessInfo.getPushRetryMin();
                }

                if (rePush != null) {
                    if (currentNum < rePush) {
                        failMessage.setNum(currentNum + 1);
                        mqSender.send(JSON.toJSONString(failMessage), rePushTime);
                        log.info("重推未超过次数，再次推送延迟队列。当前重推次数=>{}", currentNum);
                    } else {
                        // 超过重推次数入库不再重推
                        failMessage.setErrMsg("重推超过次数");
                        failMessage.setCreateTime(new Date());
                        log.info("重推超过次数，数据入库不再推送。已重推次数=>{}", currentNum);
                        log.info("入库信息：" + failMessage);
                        int insert = failMessageDao.insert(failMessage);
                        log.info("入库结果：" + insert);

                    }
                } else {
                    log.info("重推次数为空，请检查配置");
                }
            }
        } catch (Exception e) {
            int currentNum = failMessage.getNum();
            String businessKey = String.format(PrivateCacheConstant.CORP_BUSINESS_INFO, failMessage.getVccid());
            Object o = redisTemplate.opsForValue().get(businessKey);
            //重推次数
            Integer rePush = null;
            //重推间隔时间
            Integer rePushTime = null;
            if (o != null) {
                PrivateCorpBusinessInfo privateCorpBusinessInfo = JSONUtil.toBean(JSONUtil.toJsonStr(o), PrivateCorpBusinessInfo.class);
                rePush = privateCorpBusinessInfo.getPushRetryNum();
                rePushTime = privateCorpBusinessInfo.getPushRetryMin();
            } else {
                QueryWrapper<PrivateCorpBusinessInfo> wrapper = new QueryWrapper<>();
                wrapper.eq("vcc_id", failMessage.getVccid());
                PrivateCorpBusinessInfo privateCorpBusinessInfo = businessInfoService.getOne(wrapper);
                rePush = privateCorpBusinessInfo.getPushRetryNum();
                rePushTime = privateCorpBusinessInfo.getPushRetryMin();
            }

            if (rePush != null) {
                if (currentNum < rePush) {
                    failMessage.setNum(currentNum + 1);
                    mqSender.send(JSON.toJSONString(failMessage), rePushTime);
                    log.info("重推未超过次数，再次推送延迟队列。当前重推次数=>{}", currentNum);
                } else {
                    // 超过重推次数入库不再重推
                    failMessage.setErrMsg("重推超过次数");
                    failMessage.setCreateTime(new Date());
                    log.info("重推超过次数，数据入库不再推送。已重推次数=>{}", currentNum);
                    log.info("入库信息：" + failMessage);
                    int insert = failMessageDao.insert(failMessage);
                    log.info("入库结果：" + insert);

                }
            } else {
                log.info("重推次数为空，请检查配置");
            }
        }


    }

    //下行短信发送
    private String sendSms(String bindId, Integer type, String uuid, SmsRequest smsRequest) {
        SmsRequestHeader smsRequestHeader = smsRequest.getHeader();
        SmsRequestBody smsRequestBody = smsRequest.getBody();
        //请求唯一标识
        String msgId = smsRequestHeader.getMessageId();
        String streamNum = smsRequestHeader.getStreamNumber();
        //组装短信请求，请求Map 发送短信
        SendSmsRequest postToMapSmsRequest = new SendSmsRequest();
        SendSmsRequestBody body = new SendSmsRequestBody();
        SmsRequestHeader header = new SmsRequestHeader();
        header.setMessageId(msgId);
        header.setStreamNumber(streamNum);
        //处理发短信号码（小号前面要叫上86）
        String outNumber = smsRequestBody.getOutPhoneNumber();
        String calledNumber = smsRequestBody.getCalledNumber();
        String vccId = smsRequestBody.getVccId();
        //联通号码发送短信
        String areaCode;
        if (!outNumber.startsWith("86")) {
            //获取小号区号
            outNumber = 86 + outNumber;
        }
        body.setType(type);
        body.setRequestTime(smsRequestBody.getRequestTime());
        //设置发短信小号
        body.setInPhoneNumber(smsRequestBody.getInPhoneNumber());
        body.setAPhoneNumber(smsRequestBody.getaPhoneNumber());
        body.setBindId(bindId);
        body.setOutPhoneNumber(outNumber);
        body.setVccId(vccId);
        body.setSmsNumber(smsRequestBody.getSmsNumber());
        //设置收短信号码
        body.setBPhoneNumber(calledNumber);
        header.setStreamNumber(smsRequestHeader.getStreamNumber());
        header.setMessageId(smsRequestHeader.getMessageId());
        String sendResultPush = "";
        //设置短信内容
        body.setInContent(smsRequestBody.getInContent());
        //就版本map要求的循环递增参数
        String s = jedisCluster.get(SMS_TAG_CACHE);
        if (StringUtil.isEmpty(s)) {
            s = "1";
            jedisCluster.incr(SMS_TAG_CACHE);
        } else {
            if ("255".equals(s)) {
                s = "1";
                jedisCluster.set(SMS_TAG_CACHE, "1");
            } else {
                jedisCluster.incr(SMS_TAG_CACHE);
            }
        }
        header.setMessageReference(s);
        postToMapSmsRequest.setHeader(header);
        postToMapSmsRequest.setBody(body);
        //转json
        String msgRequest = JSONUtil.toJsonStr(postToMapSmsRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        HttpEntity<String> formEntity = new HttpEntity<>(msgRequest, headers);
        //请求VCCIDSMS发短信
        log.info(uuid + "|请求VCCIDSMS报文:" + msgRequest + "|url:" + MAP_URL);
//        String sendResult = "200OK";
        String sendResult = restTemplate.postForObject(MAP_URL, formEntity, String.class);
        log.info(uuid + "|请求VCCIDSMS结果：" + sendResult);
        if (StringUtil.isNotEmpty(sendResult)) {
            if (sendResult.contains("OK")) {
                sendResultPush = "0";
            } else if (sendResult.contains("0004")) {
                sendResultPush = "4";
            } else if (sendResult.contains("0001")) {
                sendResultPush = "1";
            } else if (sendResult.contains("0002")) {
                sendResultPush = "2";
            } else if (sendResult.contains("0005")) {
                sendResultPush = "5";
            } else if (sendResult.contains("0006")) {
                sendResultPush = "3";
            }
        } else {
            sendResultPush = "99";
        }
        //返回短信发送结果
        return sendResultPush;
    }


    /**
     * AXE模式短信推送
     */
    public MeituanSmsStatePush getMeituanSmsStatePush(BindInfo bindInfo, SmsRequest smsRequest, String aNumber, String inNumber, String oldMsg, String vccId) {
        return MeituanSmsStatePush.builder().appkey(vccId).ts(System.currentTimeMillis()).sms_id(smsRequest.getHeader().getStreamNumber()).area_code(bindInfo.getAreaCode()).bind_id(bindInfo.getBindId()).sender(aNumber).sender_show(inNumber).receiver_show(bindInfo.getCallNum()).receiver(bindInfo.getCalledNum()).transfer_time(DateUtil.now()).sms_content(oldMsg).sms_result(0).request_id(bindInfo.getRequest_id()).user_data(bindInfo.getUserData()).build();
    }

    private PrivateCorpBusinessInfo getVccInfo(String vccId) {
        //业务配置key
        String busKey = String.format(PrivateCacheConstant.CORP_BUSINESS_INFO, vccId);
        try {
            Object o1 = redisTemplate.opsForValue().get(busKey);
            PrivateCorpBusinessInfo businessInfo = null;
            if (o1 == null) {
                QueryWrapper<PrivateCorpBusinessInfo> wrapper = new QueryWrapper<>();
                wrapper.eq("vcc_id", vccId);
                businessInfo = businessInfoService.getOne(wrapper);
            } else {
                businessInfo = JSONUtil.toBean(JSONUtil.toJsonStr(o1), PrivateCorpBusinessInfo.class);
            }
            return businessInfo;
        } catch (Exception e) {
            QueryWrapper<PrivateCorpBusinessInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("vcc_id", vccId);
            PrivateCorpBusinessInfo businessInfo = businessInfoService.getOne(wrapper);
            return businessInfo;
        }
    }

    /**
     * third通用短信转发
     *
     * @param smsRequest 短信内容
     */
    @ApiOperation("第三方调用")
    @PostMapping(value = "/third/sms-receive")
    public CdrResult smsThirdPush(@RequestBody String smsRequest) {
        log.info("接收到第三方短信请求: {}", smsRequest);
        String uuid = UUID.randomUUID().toString();
        MeituanSmsStatePush meituanSmsStatePush = JSONUtil.toBean(smsRequest, MeituanSmsStatePush.class);
        String vccId = meituanSmsStatePush.getAppkey();
        PrivateCorpBusinessInfo vccIdConfigInfo = getVccInfo(vccId);
        if(ObjectUtil.isNull(vccIdConfigInfo)){
            return CdrResult.ok();
        }
        String smsPushUrl = vccIdConfigInfo.getSmsPushUrl();
        try {

            String json = JSONUtil.toJsonStr(meituanSmsStatePush);
            TreeMap paramsMap = JSON.parseObject(json, TreeMap.class);

            //获取sign
            meituanSmsStatePush.setSign(StringUtil.createSign(paramsMap, vccId, vccIdConfigInfo.getSecretKey()));
            log.info("{}|推送报文：{}|url：{}", uuid, JSONUtil.toJsonStr(meituanSmsStatePush), smsPushUrl);
            String potsResult = HttpUtil.post(smsPushUrl, JSONUtil.toJsonStr(meituanSmsStatePush));
            log.info(uuid + "|状态推送结果：" + potsResult);
            MeiTuanResp meiTuanResp = JSONUtil.toBean(potsResult, MeiTuanResp.class);
            //        SmsStatePush smsStatePush = new SmsStatePush();
            PrivateFailMessage smsStatePush = new PrivateFailMessage();
            smsStatePush.setId(StrUtil.uuid());
            smsStatePush.setVccid(vccId);
//                smsStatePush.setUrl(smsPushUrl);
            smsStatePush.setType("SMS");
            smsStatePush.setIp(StringUtil.getLocalIpStr());
            smsStatePush.setBody(JSONUtil.toJsonStr(meituanSmsStatePush));
            if (!"success".equals(meiTuanResp.getMessage())) {
                //推送失败，写入mq
                log.info(uuid + "|状态推送失败，写入mq进行重推");
                catchPushException(smsStatePush);
            }
        } catch (Exception e) {
            log.error(uuid + "|状态推送异常：" + e);
            //推送异常，写状态重推延时队列
            //        SmsStatePush smsStatePush = new SmsStatePush();
            PrivateFailMessage smsStatePush = new PrivateFailMessage();
            smsStatePush.setId(StrUtil.uuid());
            smsStatePush.setVccid(vccId);
//                smsStatePush.setUrl(smsPushUrl);
            smsStatePush.setIp(StringUtil.getLocalIpStr());
            smsStatePush.setType("SMS");
            smsStatePush.setBody(JSONUtil.toJsonStr(meituanSmsStatePush));
            catchPushException(smsStatePush);
        }
        return CdrResult.ok();
    }

    public MeiTuanSmsPush toMeituanPush(MeituanSmsStatePush meituanSmsStatePush){
        MeiTuanSmsPush meiTuanSmsPush = new MeiTuanSmsPush();
        BeanUtils.copyProperties(meituanSmsStatePush,meiTuanSmsPush);
        meiTuanSmsPush.setAppId(meituanProperties.getAppId());
        return meiTuanSmsPush;
    }


}


