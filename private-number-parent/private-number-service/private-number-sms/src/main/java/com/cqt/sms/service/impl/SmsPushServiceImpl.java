package com.cqt.sms.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.constants.SystemConstant;
import com.cqt.model.common.properties.MultiRedisProperties;
import com.cqt.model.numpool.entity.PrivateNumberPool;
import com.cqt.model.numpool.entity.PrivateVccInfo;
import com.cqt.model.push.properties.PushProperties;
import com.cqt.model.sms.dto.SmsPushReq;
import com.cqt.model.sms.dto.SmsRetryDto;
import com.cqt.model.sms.entity.SmsDiscardRecord;
import com.cqt.model.sms.properties.TxSmsProperties;
import com.cqt.sms.config.rabbitmq.RabbitMqConfig;
import com.cqt.sms.dao.mapper.SmsDiscardRecordMapper;
import com.cqt.sms.dao.mapper.SmsMapper;
import com.cqt.sms.model.entity.SmsRequest;
import com.cqt.sms.model.entity.SmsRequestBody;
import com.cqt.sms.model.entity.SmsRequestHeader;
import com.cqt.sms.rabbitmq.MqProducer;
import com.cqt.sms.service.SmsPushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 短信转发
 * @author: scott
 * @date: 2022年03月28日 10:02
 */
@Slf4j
@Service
public class SmsPushServiceImpl implements SmsPushService {

    private final SmsMapper smsMapper;
    private final MqProducer mqProducer;
    private final PushProperties pushProperties;
    private final TxSmsProperties txSmsProperties;
    private final MultiRedisProperties redisProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final SmsDiscardRecordMapper smsDiscardRecordMapper;
    private final RedisTemplate<String, Object> offsiteRedisTemplate;

    @Autowired
    public SmsPushServiceImpl(SmsMapper smsMapper, MqProducer mqProducer, PushProperties pushProperties,
                              TxSmsProperties txSmsProperties, MultiRedisProperties redisProperties,
                              StringRedisTemplate stringRedisTemplate, SmsDiscardRecordMapper smsDiscardRecordMapper,
                              @Qualifier(value = "offsiteRedisTemplate") @Autowired(required = false)  RedisTemplate<String, Object> offsiteRedisTemplate) {
        this.smsMapper = smsMapper;
        this.mqProducer = mqProducer;
        this.pushProperties = pushProperties;
        this.txSmsProperties = txSmsProperties;
        this.redisProperties = redisProperties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.offsiteRedisTemplate = offsiteRedisTemplate;
        this.smsDiscardRecordMapper = smsDiscardRecordMapper;
    }


    @Async("taskExecutor")
    @Override
    public void smsReceive(SmsRequest smsRequest) {
        long startMill = System.currentTimeMillis();
        SmsRequestBody smsRequestBody = smsRequest.getBody();
        SmsRequestHeader smsRequestHeader = smsRequest.getHeader();

        //获取企业vccId
        String vccId = smsRequestBody.getVccId();
        //短信内容
        String smsContent = smsRequestBody.getInContent();
        //主叫号码
        String telA = smsRequestBody.getaPhoneNumber();
        //被叫号码
        String telX = smsRequestBody.getInPhoneNumber();
        //短信发送时间
        String requestTime = smsRequestBody.getRequestTime();
        //流水号
        String msgId = smsRequestHeader.getMessageId();
        try {

            //获取X号码阈值配置
            PrivateNumberPool privateNumberPool = queryNumberPoolByNumber(telX);

            if (privateNumberPool == null) {
                log.info("msgId=>{}, X号码=>{} 未查询到号码池信息, 本次短信不做推送", msgId, telX);
                //保存短信丢弃原因
                saveSmsDiscardRecord(telA, telX, vccId, msgId, requestTime, smsContent, "未查询到X号码对应的号码池信息", false);
                return;
            }

            Long currentDailySmsCount = getCurrentDailySmsCount(telX);
            Long currentMonthlySmsCount = getCurrentMonthlySmsCount(telX);

            log.info("msgId=>{}, X号码=>{}, 当日短信发送数量=>{}, 当月短信发送数量=>{}", msgId, telX, currentDailySmsCount, currentMonthlySmsCount);

            //达到当月短信发送阈值
            if (privateNumberPool.getMonthlyShortMessage() != null && currentMonthlySmsCount > privateNumberPool.getMonthlyShortMessage()) {
                log.info("msgId=>{}, X号码=> {} 已达当月短信发送阈值, 本次短信不做转发, 当月短信发送数量=>{}, 当前发送阈值配置=>{}", msgId, telX, currentMonthlySmsCount, privateNumberPool.getMonthlyShortMessage());
                //保存短信丢弃原因
                saveSmsDiscardRecord(telA, telX, vccId, msgId, requestTime, smsContent, "达到当月短信发送阈值", true);
                return;
            }

            //达到当日短信发送阈值
            if (privateNumberPool.getDailyShortMessage() != null && currentDailySmsCount > privateNumberPool.getDailyShortMessage()) {
                log.info("msgId=>{}, X号码=>{} 已达当日短信发送阈值, 本次短信不做转发, 当日短信发送数量=>{}, 当前发送阈值配置=>{}", msgId, telX, currentMonthlySmsCount, privateNumberPool.getDailyShortMessage());
                //保存短信丢弃原因
                saveSmsDiscardRecord(telA, telX, vccId, msgId, requestTime, smsContent, "达到当日短信发送阈值", true);
                return;
            }
            // 敏感词匹配
            boolean sensitiveWordsFlag = sensitiveWordsFilter(smsContent);
            if (sensitiveWordsFlag) {
                log.info("msgId=>{}, vccId=>{}, 本次短信内容: {} 命中平台敏感词, 本次短信不做转发!", msgId, vccId, smsContent);
                //保存短信丢弃原因
                saveSmsDiscardRecord(telA, telX, vccId, msgId, requestTime, smsContent, "短信内容命中平台敏感词", true);
                return;
            }
            //白名单匹配
            boolean whiteWordCheckFlag = whiteWordsFilter(vccId, smsContent);
            if (!whiteWordCheckFlag) {
                log.info("msgId=>{}, vccId=>{}, 本次短信内容: {} 未命中白名单, 本次短信不做转发!", msgId, vccId, smsContent);
                //保存短信丢弃原因
                saveSmsDiscardRecord(telA, telX, vccId, msgId, requestTime, smsContent, "短信内容未命中白名单词汇", true);
                return;
            }

            //查询企业个性化配置信息
            PrivateVccInfo vccInfo = queryVccInfoByVccId(vccId);
            if (vccInfo == null) {
                log.info("msgId=>{}, vccId=>{} 未查询到该企业的个性化配置信息, 本次短信不做转发!", msgId, vccId);
                //保存短信丢弃原因
                saveSmsDiscardRecord(telA, telX, vccId, msgId, requestTime, smsContent, "未查询到企业的个性化配置信息", true);
                return;
            }

            if (StrUtil.isBlank(vccInfo.getSmsPushUrl())) {
                log.info("msgId=>{}, vccId=>{} 未查询到该企业的短信推送地址, 本次短信不做转发!", msgId, vccId);
                //保存短信丢弃原因
                saveSmsDiscardRecord(telA, telX, vccId, msgId, requestTime, smsContent, "未配置企业短信推送地址", true);
                return;
            }

            //短信推送
            log.info("msgId=>{}, X号码=>{}, vccId=>{}, 前置验证通过, 开始向企业接口推送短信", msgId, telX, vccId);
            smsContentPush(vccInfo.getSmsPushUrl(), telX, telA, smsContent, requestTime, msgId, vccId, 0);

        }finally {
            log.info("msgId=>{} 本次短信推送处理耗时: {} ms", msgId, System.currentTimeMillis() - startMill);
        }
    }

    @Override
    public void smsContentPush(String pushUrl, String telX, String telA, String smsContent, String requestTime, String msgId, String vccId, Integer currentCount) {
        Date requestDate = DateUtil.parse(requestTime, DatePattern.PURE_DATETIME_PATTERN);

        SmsPushReq smsPushReq = new SmsPushReq();
        //通用短信推送参数
        smsPushReq.setTelA(telA);
        smsPushReq.setTelX(telX);
        smsPushReq.setCallid(msgId);
        smsPushReq.setSmsContent(smsContent);
        smsPushReq.setCalltime(DateUtil.formatDateTime(requestDate));

        //腾讯特有个性化推送参数
        smsPushReq.setAppid(txSmsProperties.getAppId());
        smsPushReq.setDevid(txSmsProperties.getDevId());
        smsPushReq.setSdkappid(txSmsProperties.getSdkAppId());

        String json = JSON.toJSONString(smsPushReq);
        log.info("=>=> 企业短信推送 msgId=>{}, 地址=>{}, 重推次数=>{}, 请求报文=>{}", msgId, pushUrl, currentCount, json);
        String failedReason = "";
        try {
            HttpResponse resp = HttpRequest.post(pushUrl).timeout(10000).body(json).execute();
            log.info("<=<= 企业短信推送 msgId=>{}, 响应状态=>{}, 响应报文=>{}", msgId, resp.getStatus(), resp.body());
            //判断接口响应状态码为2xx则认为推送成功
            if (resp.isOk()) {
                //log.info("企业短信推送成功, msgId=>{}, 重推次数=>{}", msgId, currentCount);
                return;
            }

        }catch (Exception e) {
            log.error("企业短信推送异常, 异常信息: ", e);
            failedReason = e.getMessage();
        }
        Integer maxRetryCount = pushProperties.getRetryNum();
        log.info("企业短信推送非2XX响应, 尝试进行重推, 重推次数=>{}, 最大重推次数=>{}", currentCount, maxRetryCount);
        //重试次数小于阈值则进行重推
        if (currentCount < maxRetryCount) {
            SmsRetryDto smsRetryDto = new SmsRetryDto();
            smsRetryDto.setTelA(telA);
            smsRetryDto.setTelX(telX);
            smsRetryDto.setMsgId(msgId);
            smsRetryDto.setVccId(vccId);
            smsRetryDto.setPushUrl(pushUrl);
            smsRetryDto.setSmsContent(smsContent);
            smsRetryDto.setLastSendTime(new Date());
            smsRetryDto.setRequestTime(requestTime);
            smsRetryDto.setLastFailedReason(failedReason);
            smsRetryDto.setCurrentRetryCount(++ currentCount);

            //放入延时队列中
            mqProducer.sendLazy(RabbitMqConfig.SMS_PUSH_EXCHANGE, RabbitMqConfig.SMS_PUSH_ROUTING, smsRetryDto);
            return;
        }
        //超过阈值, 则丢弃本次短信记录, 并保存
        saveSmsDiscardRecord(telA, telX, vccId, msgId, requestTime, smsContent, failedReason + "达到最大重推次数: " + maxRetryCount, true);
    }


    /**
     * 短信白名单词汇匹配
     * 判断给定的短信文本是否命中白名单关键字
     * @param vccId 企业vccId
     * @param smsContent 短信文本
     * @return boolean 短信白名单匹配结果
     */
    @Override
    public boolean whiteWordsFilter(String vccId, String smsContent) {
        if (StrUtil.isBlank(smsContent)) {
            log.error("短信文本为空, 本次不匹配: {}", smsContent);
            return false;
        }
        //白名单匹配
        List<String> whiteWords = queryWhiteWordsByVccId(vccId);
        log.info(whiteWords.toString());

        //匹配白名单词汇
        return keyWordsFilter(smsContent, whiteWords);
    }

    /**
     * 短信黑名单词汇匹配
     * 判断给定的短信文本是否命中黑名单关键字
     * @param vccId 企业vccId
     * @param smsContent 短信文本
     * @return boolean 短信黑名单匹配结果, 命中则为true
     * */
    @Override
    public boolean blackWordsFilter(String vccId, String smsContent) {
        if (StrUtil.isBlank(smsContent)) {
            log.error("短信文本为空, 本次不匹配: {}", smsContent);
            return false;
        }
        //黑名单匹配
        List<String> blackWords = queryBlackWordsByVccId(vccId);
        //匹配黑名单词汇
        return keyWordsFilter(smsContent, blackWords);
    }

    /**
     * 匹配给定的文本内容是否在给定的关键字集c
     * @param content 文本内容
     * @param keyWords 关键词集
     * @return boolean 匹配结果
     * */
    public boolean keyWordsFilter(String content, List<String> keyWords) {
        //遍历循环匹配
        //同一个企业的多组关键字词汇为或关系, 一组的关键字词汇为且的关系
        for (String keyWord : keyWords) {
            boolean filterFlag = true;
            //多级关键字切分
            List<String> partWords = StrUtil.split(keyWord, "、");
            //匹配当前多级黑白名单词汇
            for (String partWord : partWords) {
                filterFlag &= content.contains(partWord);
            }
            //命中一组关键字词汇才算通过
            if (filterFlag) {
                log.info("短信文本内容: {}, 命中多级关键字词汇: {}", content, keyWord);
                return true;
            }
        }
        return false;
    }


    /**
     * 匹配给定的文本内容是否命中敏感词汇
     * @param content 文本内容
     * @return boolean 匹配结果
     * */
    public boolean sensitiveWordsFilter(String content) {
        List<String> sensitiveWords = querySensitiveWords();
        //遍历循环匹配
        for (String sensitiveWord : sensitiveWords) {
            // 命中任一敏感词, 都算匹配成功
            if (content.contains(sensitiveWord)) {
                log.info("短信文本内容: {}, 命中敏感词汇: {}", content, sensitiveWord);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> queryBlackWordsByVccId(String vccId) {
        String key = String.format(PrivateCacheConstant.COMMON_BLACK_WORDS_KEY, vccId);
        String blackWordsObj = stringRedisTemplate.opsForValue().get(key);

        if (ObjectUtil.isNotNull(blackWordsObj)) {
            return JSONObject.parseArray(blackWordsObj, String.class);
        }

        log.info("黑名单词汇查询, vccId:{}, 本次未命中缓存", vccId);
        List<String> blackWords = smsMapper.findBlackWordsByVccId(vccId);
        //回写redis 缓存
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(blackWords));

        return blackWords;
    }

    @Override
    public List<String> queryWhiteWordsByVccId(String vccId) {
        String key = String.format(PrivateCacheConstant.COMMON_WHITE_WORDS_KEY, vccId);
        String whiteWordsObj = stringRedisTemplate.opsForValue().get(key);

        if (ObjectUtil.isNotNull(whiteWordsObj)) {
            return JSONObject.parseArray(whiteWordsObj, String.class);
        }

        log.info("白名单词汇查询, vccId:{}, 本次未命中缓存", vccId);
        List<String> whiteWords = smsMapper.findWhiteWordsByVccId(vccId);

        //回写redis缓存
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(whiteWords));
        return whiteWords;
    }

    /**
     * 查询通用短信敏感词
     *
     * @return List 短信敏感词
     */
    @Override
    public List<String> querySensitiveWords() {
        String key = PrivateCacheConstant.COMMON_SENSITIVE_WORDS_KEY;
        String sensitiveWordsObj = stringRedisTemplate.opsForValue().get(key);

        if (ObjectUtil.isNotNull(sensitiveWordsObj)) {
            return JSONObject.parseArray(sensitiveWordsObj, String.class);
        }

        List<String> whiteWords = smsMapper.findBlackWordsByVccId(SystemConstant.DEFAULT_VCC_ID);

        //回写redis缓存
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(whiteWords));
        return whiteWords;
    }

    @Override
    public PrivateNumberPool queryNumberPoolByNumber(String xNumber) {
        String key = String.format(PrivateCacheConstant.XSMS_SHORT_MESSAGE_LIMIT_KEY, xNumber);

        String privateNumberObj = stringRedisTemplate.opsForValue().get(key);
        if (ObjectUtil.isNotNull(privateNumberObj)) {
            return JSON.parseObject(privateNumberObj, PrivateNumberPool.class);
        }
        log.info("企业信息查询, X号码:{}, 本次未命中缓存", xNumber);
        PrivateNumberPool privateNumberPool = smsMapper.findNumberPoolInfoByNumber(xNumber);
        //回写redis缓存
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(privateNumberPool));

        return privateNumberPool;
    }

    @Override
    public PrivateNumberPool findNumberPoolByNumber(String xNumber) {
        PrivateNumberPool numberPoolInfoByNumber = smsMapper.findNumberPoolInfoByNumber(xNumber);
        return numberPoolInfoByNumber;
    }


    @Override
    public PrivateVccInfo queryVccInfoByVccId(String vccId) {
        String key = String.format(PrivateCacheConstant.VCC_INFO_KEY, vccId);

        String vccInfoStr = stringRedisTemplate.opsForValue().get(key);

        if (ObjectUtil.isNotNull(vccInfoStr)) {
            return JSON.parseObject(vccInfoStr, PrivateVccInfo.class);
        }

        log.info("企业个性化配置查询, vccId:{}, 本次未命中缓存", vccId);
        PrivateVccInfo privateVccInfo = smsMapper.findVccInfoByVccId(vccId);
        //回写redis缓存
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(privateVccInfo));

        return privateVccInfo;
    }

    /**
     * 根据X号码查询出当日的短信发送数量
     * @param telX X号码
     * @return Long 当日短信发送数量
     * */
    private Long getCurrentDailySmsCount(String telX) {
        //查询该企业当日短信发送数量
        String dailySmsCountKey = String.format(PrivateCacheConstant.DAY_SMS_SENT_COUNT_KEY, DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN), telX);

        stringRedisTemplate.expire(dailySmsCountKey, DateUnit.DAY.getMillis(), TimeUnit.MILLISECONDS);
        Long currentDailySmsCount = stringRedisTemplate.opsForValue().increment(dailySmsCountKey);

        redisOffsiteDoubleIncr(dailySmsCountKey, DateUnit.DAY.getMillis(), currentDailySmsCount);

        return currentDailySmsCount;
    }

    /**
     * 根据X号码查询出当月的短信发送数量
     * @param telX X号码
     * @return Long 当月短信发送数量
     * */
    private Long getCurrentMonthlySmsCount(String telX) {
        //查询该企业当月短信发送数量
        String monthSmsCountKey = String.format(PrivateCacheConstant.MONTH_SMS_SENT_COUNT_KEY, DateUtil.format(new Date(), DatePattern.SIMPLE_MONTH_PATTERN), telX);

        stringRedisTemplate.expire(monthSmsCountKey, 31 * 24 * 60 * 60 * 1000L, TimeUnit.MILLISECONDS);

        Long currentMonthlySmsCount = stringRedisTemplate.opsForValue().increment(monthSmsCountKey);

        redisOffsiteDoubleIncr(monthSmsCountKey, 31 * 24 * 60 * 60 * 1000L, currentMonthlySmsCount);

        return currentMonthlySmsCount;
    }

    /**
     * 保存短信丢弃记录
     * @param telA 主叫号码
     * @param telX 被叫号码
     * @param vccId  企业vccId
     * @param msgId 短信业务流水号
     * @param receiveTime 短信发送时间
     * @param smsContent 短信内容
     * @param discardInfo 丢弃原因
     * @param isDecr 是否需要自减
     * */
    private void saveSmsDiscardRecord(String telA, String telX, String vccId, String msgId, String receiveTime, String smsContent, String discardInfo, boolean isDecr) {
        log.info("msgId=>{}, 本次短信被丢弃, 丢弃原因=>{}", msgId, discardInfo);
        if (isDecr) {
            String dailySmsCountKey = String.format(PrivateCacheConstant.DAY_SMS_SENT_COUNT_KEY, DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN), telX);
            String monthSmsCountKey = String.format(PrivateCacheConstant.MONTH_SMS_SENT_COUNT_KEY, DateUtil.format(new Date(), DatePattern.SIMPLE_MONTH_PATTERN), telX);

            Long currentMonthlySmsCount = stringRedisTemplate.opsForValue().decrement(monthSmsCountKey);
            Long currentDailySmsCount = stringRedisTemplate.opsForValue().decrement(dailySmsCountKey);

            //异地机房redis写入
            redisOffsiteDoubleDecr(dailySmsCountKey, currentDailySmsCount);
            redisOffsiteDoubleDecr(monthSmsCountKey, currentMonthlySmsCount);

            log.info("msgId=>{}, 本次短信被丢弃, 修正当日短信发送数量=>{}, 修正当月短信发送数量=>{}", msgId, currentDailySmsCount, currentMonthlySmsCount);
        }
        Date requestDate = DateUtil.parse(receiveTime, DatePattern.PURE_DATETIME_PATTERN);


        SmsPushReq smsPushReq = new SmsPushReq();
        //通用短信推送参数
        smsPushReq.setTelA(telA);
        smsPushReq.setTelX(telX);
        smsPushReq.setCallid(msgId);
        smsPushReq.setSmsContent(smsContent);
        smsPushReq.setCalltime(DateUtil.formatDateTime(requestDate));

        //腾讯特有个性化推送参数
        smsPushReq.setAppid(txSmsProperties.getAppId());
        smsPushReq.setDevid(txSmsProperties.getDevId());
        smsPushReq.setSdkappid(txSmsProperties.getSdkAppId());

        SmsDiscardRecord smsDiscardRecord = new SmsDiscardRecord();

        smsDiscardRecord.setTelA(telA);
        smsDiscardRecord.setTelX(telX);
        smsDiscardRecord.setMsgId(msgId);
        smsDiscardRecord.setVccId(vccId);
        smsDiscardRecord.setReqJson(JSON.toJSONString(smsDiscardRecord));
        smsDiscardRecord.setCreateTime(new Date());
        smsDiscardRecord.setSmsContent(smsContent);
        smsDiscardRecord.setDiscardInfo(discardInfo);
        smsDiscardRecord.setReceiveTime(DateUtil.parse(receiveTime, DatePattern.PURE_DATETIME_PATTERN));

        smsDiscardRecordMapper.insert(smsDiscardRecord);
    }


    /**
     * 异地机房 redis自增写入
     * @param key 键值
     * @param expire 过期时间, 毫秒
     * @param masterCurrentCount 当前主机房redis写入数量
     * */
    private void redisOffsiteDoubleIncr(String key, Long expire, Long masterCurrentCount) {
        //判断是否激活异地机房
        if (redisProperties.getCluster2() != null && redisProperties.getCluster2().getActive()) {
            if (log.isInfoEnabled()) {
                log.debug("启用异地机房redis, 进行redis双写, key=>{}, expire=>{} ms", key, expire);
            }
            try {
                Long slaveCurrentCount = offsiteRedisTemplate.opsForValue().increment(key);
                offsiteRedisTemplate.expire(key, expire, TimeUnit.MILLISECONDS);
                //判断主备机房redis写入数量是否一致
                if (!masterCurrentCount.equals(slaveCurrentCount)) {
                    log.info("异地机房当前短信发送数量与主机房不一致, 尝试修正! masterCurrentCount=>{}, slaveCurrentCount=>{}", masterCurrentCount, slaveCurrentCount);
                    offsiteRedisTemplate.opsForValue().set(key, masterCurrentCount, expire, TimeUnit.MILLISECONDS);
                }
            }catch (Exception e) {
                log.error("异地机房redis 自增写入异常, key:{}, 过期时间:{} ms. :", key, expire, e);
            }
        }
    }

    /**
     * 异地机房 redis自减写入
     * @param key 键值
     * @param masterCurrentCount 当前主机房redis写入数量
     * */
    private void redisOffsiteDoubleDecr(String key, Long masterCurrentCount) {
        //判断是否激活异地机房
        if (redisProperties.getCluster2() != null && redisProperties.getCluster2().getActive()) {
            if (log.isInfoEnabled()) {
                log.debug("启用异地机房redis, 进行redis双写, key=>{}", key);
            }
            try {
                Long slaveCurrentCount = offsiteRedisTemplate.opsForValue().decrement(key);
                //判断主备机房redis写入数量是否一致
                if (!masterCurrentCount.equals(slaveCurrentCount)) {
                    log.info("异地机房当前短信发送数量与主机房不一致, 尝试修正! masterCurrentCount=>{}, slaveCurrentCount=>{}", masterCurrentCount, slaveCurrentCount);
                    offsiteRedisTemplate.opsForValue().set(key, masterCurrentCount);
                }
            }catch (Exception e) {
                log.error("异地机房redis 自减写入异常, key:{}, 过期时间:{} ms. :", key, e);
            }
        }
    }

    @Override
    public boolean smsCount(SmsRequest smsRequest){
        long startMill = System.currentTimeMillis();
        SmsRequestBody smsRequestBody = smsRequest.getBody();
        SmsRequestHeader smsRequestHeader = smsRequest.getHeader();

        //获取企业vccId
        String vccId = smsRequestBody.getVccId();
        //短信内容
        String smsContent = smsRequestBody.getInContent();
        //主叫号码
        String telA = smsRequestBody.getaPhoneNumber();
        //被叫号码
        String telX = smsRequestBody.getInPhoneNumber();
        //短信发送时间
        String requestTime = smsRequestBody.getRequestTime();
        //流水号
        String msgId = smsRequestHeader.getMessageId();
        PrivateNumberPool privateNumberPool = queryNumberPoolByNumber(telX);

        if (privateNumberPool == null) {
            log.info("msgId=>{}, X号码=>{} 未查询到号码池信息, 本次短信不做推送", msgId, telX);
            //保存短信丢弃原因
            saveSmsDiscardRecord(telA, telX, vccId, msgId, requestTime, smsContent, "未查询到X号码对应的号码池信息", false);
            return false;
        }
        Long currentDailySmsCount = getCurrentDailySmsCount(telX);
        Long currentMonthlySmsCount = getCurrentMonthlySmsCount(telX);

        log.info("msgId=>{}, X号码=>{}, 当日短信发送数量=>{}, 当月短信发送数量=>{}", msgId, telX, currentDailySmsCount, currentMonthlySmsCount);

        //达到当月短信发送阈值
        if (privateNumberPool.getMonthlyShortMessage() != null && currentMonthlySmsCount > privateNumberPool.getMonthlyShortMessage()) {
            log.info("msgId=>{}, X号码=> {} 已达当月短信发送阈值, 本次短信不做转发, 当月短信发送数量=>{}, 当前发送阈值配置=>{}", msgId, telX, currentMonthlySmsCount, privateNumberPool.getMonthlyShortMessage());
            //保存短信丢弃原因
            saveSmsDiscardRecord(telA, telX, vccId, msgId, requestTime, smsContent, "达到当月短信发送阈值", true);
            return false;
        }

        //达到当日短信发送阈值
        if (privateNumberPool.getDailyShortMessage() != null && currentDailySmsCount > privateNumberPool.getDailyShortMessage()) {
            log.info("msgId=>{}, X号码=>{} 已达当日短信发送阈值, 本次短信不做转发, 当日短信发送数量=>{}, 当前发送阈值配置=>{}", msgId, telX, currentMonthlySmsCount, privateNumberPool.getDailyShortMessage());
            //保存短信丢弃原因
            saveSmsDiscardRecord(telA, telX, vccId, msgId, requestTime, smsContent, "达到当日短信发送阈值", true);
            return false;
        }
        //白名单匹配
        boolean whiteWordCheckFlag = whiteWordsFilter(vccId, smsContent);
        if (!whiteWordCheckFlag) {
            log.info("msgId=>{}, vccId=>{}, 本次短信内容: {} 未命中白名单, 本次短信不做转发!", msgId, vccId, smsContent);
            //保存短信丢弃原因
            saveSmsDiscardRecord(telA, telX, vccId, msgId, requestTime, smsContent, "短信内容未命中白名单词汇", true);
            return false;
        }

        return true;
    }
}
