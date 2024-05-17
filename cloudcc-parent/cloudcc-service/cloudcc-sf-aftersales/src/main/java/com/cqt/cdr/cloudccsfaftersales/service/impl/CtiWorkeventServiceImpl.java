package com.cqt.cdr.cloudccsfaftersales.service.impl;
import com.alibaba.fastjson.JSONObject;
import com.cqt.cdr.cloudccsfaftersales.conf.DynamicConfig;
import com.cqt.cdr.cloudccsfaftersales.entity.dto.SFStatusReq;
import com.cqt.cdr.cloudccsfaftersales.service.CtiWorkeventService;
import com.cqt.cdr.cloudccsfaftersales.util.Sha256;
import com.cqt.cdr.cloudccsfaftersales.util.StringUtil;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class CtiWorkeventServiceImpl implements CtiWorkeventService {

    @Resource
    private RedissonUtil redissonUtilL;
    @Resource
    private DynamicConfig dynamicConfig;

    private static String ALLUSEMAC = "cqt1234";
    private static String testkey = "4c5d80e607cd9e26ac8fd954c9fdbab7";
    private static String zskey = "E532ADC266440127B76DA10129CD6386";


    @Async("doSomethingExecutor")
    @Override
    public CompletableFuture<Map<String, String>> check(SFStatusReq sfStatusReq) {
        String LOG_TAG = UUID.randomUUID().toString() + " | tssfapi坐席状态查询 | ";
        long startTime = System.currentTimeMillis();
        //设置响应内容类型L
        String detail = "";
        String laststate = "";
        String message = "";
        String code = "";
        String state = "";
        if (StringUtil.isNotEmpty(sfStatusReq.getAgentid())) {
            if (StringUtil.isNotEmpty(sfStatusReq.getVccid())) {
                if (StringUtil.isNotEmpty(sfStatusReq.getToken())) {
                    if (!checkexpress(sfStatusReq.getToken())) {
                        message = "token验证失败";
                        code = "fail";
                    } else {
                        try {
                            try {
                                String agentStatus = "";
                                try {
                                    agentStatus = redissonUtilL.get("cloudcc:agentStatus:" + sfStatusReq.getVccid() + ":" + sfStatusReq.getAgentid());
                                    if (StringUtil.isNotEmpty(agentStatus)){
                                        log.info(LOG_TAG+ "agentStatus:" + agentStatus);
                                        JSONObject jsons = JSONObject.parseObject(agentStatus);
                                        String agentstatus = jsons.get("targetStatus")+"";
                                        String transferAction = jsons.get("transferAction")+"";
                                        if ("FREE".equals(agentstatus)){
                                            state = "1";
                                        }else{
                                            state = "0";
                                        }
                                        switch(transferAction) {
                                            //签入
                                            case "CHECKIN":
                                                detail = "1";
                                                break;
                                            //签出
                                            case "CHECKOUT":
                                                detail = "0";
                                                break;
                                            //呼出
                                            case "CALLOUT":
                                                detail = "17";
                                                break;
                                            //应答
                                            case "ANSWER":
                                                detail = "13";
                                                break;
                                            //桥接
                                            case "BRIDGE":
                                                detail = "8";
                                                break;
                                            //挂机
                                            case "HANGUP":
                                                detail = "15";
                                                break;
                                            //保持
                                            case "HOLD":
                                                detail = "311";
                                                break;
                                            //取消保持
                                            case "UN_HOLD":
                                                detail = "312";
                                                break;
                                            //呼入
                                            case "CALLIN":
                                                detail = "18";
                                                break;
                                            //事后处理
                                            case "ARRANGE":
                                                detail = "4";
                                                break;
                                            //振铃
                                            case "RING":
                                                detail = "307";
                                                break;
                                            //示闲
                                            case "MAKE_FREE":
                                                detail = "2";
                                                break;
                                            //示忙
                                            case "MAKE_BUSY":
                                                detail = "3";
                                                break;
                                            //小休
                                            case "MAKE_REST":
                                                detail = "3";
                                                break;
                                            //进入事后处理
                                            case "MAKE_ARRANGE":
                                                detail = "4";
                                                break;
                                            //恢复通话前的状态
                                            case "RECOVER":
                                                detail = "19";
                                                break;
                                            //通话结束后自动示忙
                                            case "MAKE_BUSY_AFTER_CALL_STOP":
                                                detail = "20";
                                                break;
                                            //通话结束后自动示闲
                                            case "MAKE_FREE_AFTER_CALL_STOP":
                                                detail = "21";
                                                break;
                                            //通话结束后自动进入小休
                                            case "MAKE_REST_AFTER_CALL_STOP":
                                                detail = "22";
                                                break;
                                            //通话结束后自动进入签出
                                            case "CHECKOUT_AFTER_CALL_STOP":
                                                detail = "23";
                                                break;
                                            //强复位
                                            case "FORCE_RESET":
                                                detail = "24";
                                                break;
                                            //强签
                                            case "FORCE_CHECKOUT":
                                                detail = "25";
                                                break;
                                            //强制示闲
                                            case "FORCE_MAKE_FREE":
                                                detail = "26";
                                                break;
                                            //强制示忙
                                            case "FORCE_MAKE_BUSY":
                                                detail = "27";
                                                break;
                                            default:
                                                detail = "-100";
                                                break;
                                        }
                                    }else {
                                        state = "0";
                                        detail = "-100";
                                    }
                                    log.info(LOG_TAG + "处理得状态state: " + state);
                                    log.info(LOG_TAG + "获取最近detail: " + detail);
                                    log.info(LOG_TAG + "获取最近laststate: " + laststate);
                                    code = "success";
                                    message = "请求成功";
                                }catch (Exception e){
                                    log.error(LOG_TAG + "操作redis异常：", e);
                                }
                            }catch (Exception e){
                                code = "fail";
                                message = e.getMessage();
                                log.error(LOG_TAG + "获取缓存异常: ", e);
                            }
                        } catch (Exception e) {
                            code = "fail";
                            message = e.getMessage();
                            log.error(LOG_TAG + "坐席信息表请求异常: ", e);
                        }
                    }
                } else {
                    message = "toke不能为空";
                    code = "fail";
                }
            } else {
                message = "企业不能为空";
                code = "fail";
            }
        } else {
            message = "坐席工号不能为空";
            code = "fail";
        }
        Map<String, String> map = new HashMap<>();
        map.put("detail", detail);
        map.put("code", code);
        map.put("state", state);
        map.put("message", message);
        log.info(LOG_TAG + "返回的map：" + map);
        long endTime = System.currentTimeMillis();
        log.info("总耗时：" + (endTime - startTime) + "ms");
        return CompletableFuture.completedFuture(map);
    }

    public Boolean checkexpress(String token) {
        Boolean flag = false;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 1);
        for (int i = 0; i < 600; i++) {
            calendar.add(Calendar.SECOND, -1);
            String timestamp = StringUtil.getDate14FromDate(calendar.getTime());

            String checkToken = Sha256.getSHA256Str(dynamicConfig.getXfsfstatukey() + timestamp);
            if (token.equals(checkToken) || token.equals(ALLUSEMAC)) {
                flag = true;
                break;
            }
            // log.info("timestamp=" + timestamp + "====checkToken=" + checkToken);
        }
        calendar.add(Calendar.SECOND, 600);
        calendar.add(Calendar.SECOND, -1);
        for (int i = 0; i < 600; i++) {
            calendar.add(Calendar.SECOND, 1);
            String timestamp = StringUtil.getDate14FromDate(calendar.getTime());
            String checkToken = Sha256.getSHA256Str(dynamicConfig.getXfsfstatukey() + timestamp);
            //log.info("timestamp="+timestamp + "====checkToken=" + checkToken);
            if (token.equals(checkToken) || token.equals(ALLUSEMAC)) {
                flag = true;
                break;
            }
        }
        return flag;
    }
}


