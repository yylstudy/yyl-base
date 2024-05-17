package com.cqt.wechat.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.push.entity.PrivateCdrRePush;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.model.sms.dto.CommonSmsBillPushDTO;
import com.cqt.wechat.config.CorpBusinessConfigCache;
import com.cqt.wechat.config.RabbitMqConfig;
import com.cqt.wechat.config.RedissonUtil;
import com.cqt.wechat.dao.FailMessageDao;
import com.cqt.wechat.entity.*;
import com.cqt.wechat.job.QueryTokenTask;
import com.cqt.wechat.mapper.PrivateCdrRepushMapper;
import com.cqt.wechat.mapper.PrivateCorpBusinessInfoMapper;
import com.cqt.wechat.mapper.PrivateCorpInfoMapper;
import com.cqt.wechat.mapper.PrivateNumberInfoMapper;
import com.cqt.wechat.properties.WechatPushProperties;
import com.cqt.wechat.rabbitmq.MqSender;
import com.cqt.wechat.rabbitmq.RabbitService;
import com.cqt.wechat.utils.ConvertUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author hlx
 * @date 2021-09-15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PushService {

    private final static Integer UN_AUTH_ERROR_CODE = 40001;

    private final MqSender mqSender;

    private final FailMessageDao failMessageDao;

    private final RedissonUtil redissonUtil;

    private final PrivateCdrRepushMapper repushMapper;

    private final RabbitService rabbitService;

    private final PrivateCorpInfoMapper privateCorpInfoMapper;

    private final PrivateCorpBusinessInfoMapper businessInfoMapper;

    private final ConvertUtil convertUtil;

    private final WechatPushProperties pushProperties;

    private final ObjectMapper objectMapper;

    private final PrivateNumberInfoMapper privateNumberInfoMapper;

    private final QueryTokenTask queryTokenTask;


    @Async("threadPoolTaskExecutor")
    public void pushBillStart(PrivateFailMessage failMessage) throws ParseException, JsonProcessingException {
        log.info("开始执行话单推送");
        PrivateBillInfo billInfo = JSON.parseObject(failMessage.getBody(), PrivateBillInfo.class);
        String recordId = billInfo.getRecordId();
        WechatCdrInfo cdrInfo = convertUtil.toWechatCdr(billInfo);
        String token = redissonUtil.getString(PrivateCacheConstant.WECHAT_TOKEN);
        String url = pushProperties.getBillUrl() + token;
        String s1 = objectMapper.writeValueAsString(cdrInfo);
        log.info("推送的话单报文=>{}\n推送话单的地址=>{}, 若为空则不推送",
                s1, url);
        try (HttpResponse httpResponse = HttpRequest.post(url)
                .timeout(10000)
                .body(s1)
                .execute()) {
            if (httpResponse.isOk()) {
                String body = httpResponse.body();
                JSONObject resObject = JSONObject.parseObject(body);
                int code = resObject.getInteger("errcode");
                String message = resObject.getString("errmsg");
                if (code == 0) {
                    log.info("话单推送成功，重推次数=>{}，RecordId=>{}\n返回的报文=>{}",
                            failMessage.getNum(), billInfo.getRecordId(), body);
                } else if (code == 40001) {
                    failMessage.setErrMsg(message);
                    failMessage.setCreateTime(new Date());
                    failMessageDao.insert(failMessage);
                    log.info("token失效，重新请求微信鉴权接口获取token，RecordId=>{}，错误报文=>{}", recordId, body);
                    queryTokenTask.getToken();
                    catchBillException(failMessage, message);
                } else {
                    failMessage.setErrMsg(message);
                    failMessage.setCreateTime(new Date());
                    failMessageDao.insert(failMessage);
                    log.info("通话话单推送成功，但返回错误码，通话状态入库，RecordId=>{}，错误报文=>{}", recordId, body);
                }

                return;
            }
            int status = httpResponse.getStatus();
            String s = "响应码：" + status;
            catchBillException(failMessage, s);

        } catch (Exception e) {
            catchBillException(failMessage, e.toString());
        }
    }

    @Async("threadPoolTaskExecutor")
    public void pushStatusStart(PrivateFailMessage failMessage) throws ParseException, JsonProcessingException {
        log.info("开始执行通话状态推送");
        PrivateStatusInfo privateStatusInfo = JSON.parseObject(failMessage.getBody(), PrivateStatusInfo.class);
        String recordId = privateStatusInfo.getRecordId();
        StatusInfo statusInfo = convertUtil.toStatusInfo(privateStatusInfo);
        String token = redissonUtil.getString(PrivateCacheConstant.WECHAT_TOKEN);
        String url = pushProperties.getStatusUrl() + token;
        String s1 = objectMapper.writeValueAsString(statusInfo);

        log.info("推送的通话状态报文=>{}\n推送通话状态的地址=>{}",
                s1, url);
        try (HttpResponse httpResponse = HttpRequest.post(url)
                .timeout(10000)
                .body(JSON.toJSONString(statusInfo))
                .execute()) {
            if (httpResponse.isOk()) {
                String body = httpResponse.body();
                JSONObject resObject = JSONObject.parseObject(body);
                int code = resObject.getInteger("errcode");
                String message = resObject.getString("errmsg");
                if (code == 0) {
                    log.info("通话状态推送成功，重推次数=>{}，RecordId=>{}\n返回的报文=>{}",
                            failMessage.getNum(), recordId, body);
                } else if (code == 40001) {
                    failMessage.setErrMsg(message);
                    failMessage.setCreateTime(new Date());
                    failMessageDao.insert(failMessage);
                    log.info("token失效，重新请求微信鉴权接口获取token，RecordId=>{}，错误报文=>{}", recordId, body);
                    queryTokenTask.getToken();
                    catchPushException(failMessage, message);
                } else {
                    failMessage.setErrMsg(message);
                    failMessage.setCreateTime(new Date());
                    failMessageDao.insert(failMessage);
                    log.info("通话状态推送成功，但返回错误码，通话状态入库，RecordId=>{}，错误报文=>{}", recordId, body);
                }
                return;
            }

            int status = httpResponse.getStatus();
            String s = "响应码：" + status;
            catchPushException(failMessage, s);

        } catch (Exception e) {
            log.error(String.valueOf(e));
            catchPushException(failMessage, e.getMessage());
        }
    }


    private void catchPushException(PrivateFailMessage failMessage, String e) {

        log.info("推送请求超时或无响应，push_type=>{}，错误信息:{}", failMessage.getType(), e);
        // 队列创建
        String queueName = String.format(RabbitMqConfig.PRIVATE_PUSH_DELAY_QUEUE, "wx");
        rabbitService.createQueue(queueName);
        // 重推次数小于规定次数,重推次数加一,发送到mq延迟队列
        int currentNum = failMessage.getNum();
        String type = failMessage.getType();
        //重推次数
        Integer rePush;
        //重推间隔时间
        Integer rePushTime;
        String businessKey = String.format(PrivateCacheConstant.CORP_BUSINESS_INFO, failMessage.getVccid());
        Object o = redissonUtil.getObject(businessKey);
        if (o != null) {
            PrivateCorpBusinessInfo privateCorpBusinessInfo = JSONUtil.toBean(JSONUtil.toJsonStr(o), PrivateCorpBusinessInfo.class);
            rePush = privateCorpBusinessInfo.getStatusRetryNum();
            rePushTime = privateCorpBusinessInfo.getStatusRetryMin();
        } else {
            QueryWrapper<PrivateCorpBusinessInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("vcc_id", failMessage.getVccid());
            PrivateCorpBusinessInfo privateCorpBusinessInfo = businessInfoMapper.selectOne(wrapper);
            rePush = privateCorpBusinessInfo.getStatusRetryNum();
            rePushTime = privateCorpBusinessInfo.getStatusRetryMin();
        }
        if (rePush != null) {
            if (currentNum < rePush) {
                failMessage.setNum(currentNum + 1);
                mqSender.sendLazy(JSON.toJSONString(failMessage), queueName, rePushTime);
                log.info("重推未超过次数，再次推送延迟队列。当前重推次数=>{}，push_type=>{}", currentNum, type);
            } else {
                // 超过重推次数入库不再重推
                failMessage.setErrMsg("重推超过次数");
                failMessage.setCreateTime(new Date());
                failMessageDao.insert(failMessage);
                log.info("重推超过次数，数据入库不再推送。已重推次数=>{}，push_type=>{}", currentNum, type);
            }
        } else {
            log.info("重推次数为空，请检查配置");
        }


    }

    private void catchBillException(PrivateFailMessage failMessage, String e) {
        try {
            log.info("推送请求超时或无响应，push_type=>{}，错误信息:{}", failMessage.getType(), e);
            // 队列创建
            String queueName = String.format(RabbitMqConfig.PRIVATE_PUSH_DELAY_QUEUE, "wx");
            rabbitService.createQueue(queueName);
            // 重推次数小于规定次数,重推次数加一,发送到mq延迟队列
            int currentNum = failMessage.getNum();
            String type = failMessage.getType();
            String businessKey = String.format(PrivateCacheConstant.CORP_BUSINESS_INFO, failMessage.getVccid());
            Object o = redissonUtil.getObject(businessKey);
            //重推次数
            Integer rePush;
            //重推间隔时间
            Integer rePushTime;
            if (o != null) {
                PrivateCorpBusinessInfo privateCorpBusinessInfo = JSONUtil.toBean(JSONUtil.toJsonStr(o), PrivateCorpBusinessInfo.class);
                rePush = privateCorpBusinessInfo.getPushRetryNum();
                rePushTime = privateCorpBusinessInfo.getPushRetryMin();
            } else {
                QueryWrapper<PrivateCorpBusinessInfo> wrapper = new QueryWrapper<>();
                wrapper.eq("vcc_id", failMessage.getVccid()).last("limit 1");
                PrivateCorpBusinessInfo privateCorpBusinessInfo = businessInfoMapper.selectOne(wrapper);
                rePush = privateCorpBusinessInfo.getPushRetryNum();
                rePushTime = privateCorpBusinessInfo.getPushRetryMin();
            }

            if (rePush != null) {
                if (currentNum < rePush) {
                    failMessage.setNum(currentNum + 1);
                    mqSender.sendLazy(JSON.toJSONString(failMessage), queueName, rePushTime);
                    log.info("重推未超过次数，再次推送延迟队列。当前重推次数=>{}，push_type=>{}", currentNum, type);
                } else {
                    // 超过重推次数入库不再重推
                    failMessage.setErrMsg("重推超过次数");
                    failMessage.setCreateTime(new Date());
                    failMessageDao.insert(failMessage);
                    try {
                        QueryWrapper<PrivateCorpBusinessInfo> wrapper = new QueryWrapper<>();
                        wrapper.eq("vcc_id", failMessage.getVccid()).last("limit 1");
                        PrivateCorpBusinessInfo businessInfo = businessInfoMapper.selectOne(wrapper);
                        PrivateCdrRePush privateCdrRepush = new PrivateCdrRePush();
                        privateCdrRepush.setCdrPushUrl(businessInfo.getBillPushUrl());
                        privateCdrRepush.setVccId(businessInfo.getVccId());
                        PrivateCorpInfo corpInfo = privateCorpInfoMapper.selectOne(new QueryWrapper<PrivateCorpInfo>().eq("vcc_id", businessInfo.getVccId()).last("limit 1"));
                        privateCdrRepush.setVccName(corpInfo.getVccName());
                        privateCdrRepush.setFailReason(failMessage.getErrMsg());
                        privateCdrRepush.setJsonStr(failMessage.getBody());
                        privateCdrRepush.setRepushFailTime(new Date());
                        repushMapper.insert(privateCdrRepush);
                        log.info("入庫成功");
                    } catch (Exception e1) {
                        log.info("入庫失敗");
                        e1.printStackTrace();
                    }
                }
            } else {
                log.info("重推次数为空，请检查配置");
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            log.error("重推异常");
        }
    }

    @Async("threadPoolTaskExecutor")
    public void pushMsg(PrivateFailMessage failMessage) throws JsonProcessingException, ParseException {
        CommonSmsBillPushDTO commonSmsBillPushDTO = JSON.parseObject(failMessage.getBody(), CommonSmsBillPushDTO.class);
        MsgInfo msgInfo = convertUtil.toMsgInfo(commonSmsBillPushDTO);
        String pushData = objectMapper.writeValueAsString(msgInfo);
        String token = redissonUtil.getString(PrivateCacheConstant.WECHAT_TOKEN);
        String url = pushProperties.getMsgUrl() + token;
        try (HttpResponse httpResponse = HttpRequest.post(url)
                .timeout(10000)
                .body(pushData)
                .execute()) {
            if (httpResponse.isOk()) {
                String body = httpResponse.body();
                JSONObject resObject = JSONObject.parseObject(body);
                int code = resObject.getInteger("errcode");
                String message = resObject.getString("errmsg");
                failMessage.setErrMsg(message);
                if (code == 0) {
                    log.info("推送报文: {}, 短信话单推送成功, 推送接口: {}, 返回: {}", pushData, url, body);
                    return;
                } else if (code == 40001) {
                    log.info("推送报文: {}, token失效，重推: {}, 返回: {}", pushData, url, body);
                    queryTokenTask.getToken();
                    repushMsg(failMessage, message);
                    return;
                }

                // 接口返回码不正常, 进入重推再判断
                log.info("推送报文: {}, 短信话单推送接口返回失败, 推送接口: {}, 返回: {}", pushData, url, body);
            }

            // 接口返回httpStatus不正常, 重试
            repushMsg(failMessage, "接口返回httpStatus不正常");
            log.error("推送报文: {}, 短信话单接口返回httpStatus不正常: {}, 推送接口: {}", pushData, httpResponse.getStatus(), url);
        } catch (Exception e) {
            // 接口调用异常, 重试
            log.error(" 推送报文: {}, 短信话单推送异常, 推送接口: {}, 异常信息: {}", pushData, url, e.getMessage());
            repushMsg(failMessage, e.getMessage());
        }
    }

    private void repushMsg(PrivateFailMessage failMessage, String e) {
        try {
            log.info("推送请求超时或无响应，push_type=>{}，错误信息:{}", failMessage.getType(), e);
            // 队列创建
            String queueName = String.format(RabbitMqConfig.PRIVATE_PUSH_DELAY_QUEUE, "SMS");
            rabbitService.createQueue(queueName);
            // 重推次数小于规定次数,重推次数加一,发送到mq延迟队列
            int currentNum = failMessage.getNum();
            String type = failMessage.getType();
            //重推次数
            Integer rePush = pushProperties.getSmsRetryTimes();
            //重推间隔时间
            Integer rePushTime = pushProperties.getSmsRetryInterval();

            if (rePush != null) {
                if (currentNum < rePush) {
                    failMessage.setNum(currentNum + 1);
                    mqSender.sendLazy(JSON.toJSONString(failMessage), queueName, rePushTime);
                    log.info("重推未超过次数，再次推送延迟队列。当前重推次数=>{}，push_type=>{}", currentNum, type);
                } else {
                    try {
                        CommonSmsBillPushDTO smsBillPushDTO = JSON.parseObject(failMessage.getBody(), CommonSmsBillPushDTO.class);
                        PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO = getPrivateCorpBusinessInfoDTO(smsBillPushDTO.getSenderShow());
                        PrivateCdrRePush privateCdrRePush = new PrivateCdrRePush();
                        privateCdrRePush.setId(smsBillPushDTO.getSmsId());
                        privateCdrRePush.setCdrPushUrl(pushProperties.getMsgUrl());
                        privateCdrRePush.setVccId(privateCorpBusinessInfoDTO.getVccId());
                        privateCdrRePush.setVccName(privateCorpBusinessInfoDTO.getVccName());
                        privateCdrRePush.setFailReason(e);
                        privateCdrRePush.setJsonStr(failMessage.getBody());
                        privateCdrRePush.setRepushFailTime(DateUtil.date());
                        privateCdrRePush.setCreateTime(DateUtil.date());
                        privateCdrRePush.setCreateBy("wechat");
                        privateCdrRePush.setUpdateTime(DateUtil.date());
                        privateCdrRePush.setUpdateBy("wechat");
                        repushMapper.insert(privateCdrRePush);
                    } catch (Exception e1) {
                        log.info("入庫失敗");
                        e1.printStackTrace();
                    }
                }
            } else {
                log.info("重推次数为空，请检查配置");
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            log.error("重推异常");
        }

    }

    public PrivateCorpBusinessInfoDTO getPrivateCorpBusinessInfoDTO(String secretNo) {
        String xNumberBelongVccIdKey = String.format(PrivateCacheConstant.X_NUMBER_BELONG_VCC_ID_KEY, secretNo);
        String vccId = redissonUtil.getString(xNumberBelongVccIdKey);
        if (StrUtil.isEmpty(vccId)) {
            PrivateNumberInfo privateNumberInfo = privateNumberInfoMapper.selectById(secretNo);
            if (ObjectUtil.isEmpty(privateNumberInfo)) {
                throw new RuntimeException("中间号不存在本平台!");
            }
            vccId = privateNumberInfo.getVccId();
        }

        return CorpBusinessConfigCache.get(vccId).orElseThrow(() -> new RuntimeException("未找到中间号归属"));
    }

    public static void main(String[] args) throws ParseException {
//        PrivateStatusInfo privateStatusInfo = new PrivateStatusInfo();
//        String aa = JSON.toJSONString(privateStatusInfo, SerializerFeature.WRITE_MAP_NULL_FEATURES);
//        JSONObject object = JSON.parseObject(aa);
//        System.out.println(object);
        String s = "2023-02-28 09:57:58";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s));
        long l = calendar.getTimeInMillis() / 1000;

        System.out.println((int) l);

    }


    /**
     * 号码池变更同步微信视频号通知
     */
    public void notifyWechat(List<String> areaCodeList) {
        WechatNotifyDTO wechatNotifyDTO = new WechatNotifyDTO();
        wechatNotifyDTO.setAreaCodeList(areaCodeList);
        requestWechat(pushProperties.getPoolNotifyUrl(), JSON.toJSONString(wechatNotifyDTO));
    }

    private void requestWechat(String url, String body) {
        String cacheToken = redissonUtil.getString(PrivateCacheConstant.WECHAT_TOKEN);
        if (StringUtils.isEmpty(cacheToken)) {
            queryTokenTask.getToken();
            cacheToken = redissonUtil.getString(PrivateCacheConstant.WECHAT_TOKEN);
        }
        String resp = requestWechat(url, body, cacheToken);
        if (StrUtil.isNotEmpty(resp)) {
            JSONObject retObj = JSONObject.parseObject(resp);
            Integer errorCode = retObj.getInteger("errcode");
            // 接口鉴权失败, 说明token已过期, 发起重试
            if (UN_AUTH_ERROR_CODE.equals(errorCode)) {
                queryTokenTask.getToken();
                String newToken = redissonUtil.getString(PrivateCacheConstant.WECHAT_TOKEN);
                requestWechat(url, body, newToken);
            }
        }
    }

    private String requestWechat(String url, String body, String accessToken) {
        String requestUrl = url + accessToken;
        log.info("请求微信视频号接口: {}, 请求参数: {}", requestUrl, body);
        try (HttpResponse response = HttpRequest.post(requestUrl)
                .body(body)
                .timeout(10000)
                .execute()) {
            log.info("请求微信视频号接口: {}, 响应状态码: {}, 响应内容: {}", requestUrl, response.getStatus(), response.body());
            return response.body();
        }
    }
}
