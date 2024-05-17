package com.cqt.ivr.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cqt.ivr.config.nacos.DynamicConfig;
import com.cqt.ivr.entity.QueueAgentInfo;
import com.cqt.ivr.entity.dto.*;
import com.cqt.ivr.entity.vo.*;
import com.cqt.ivr.service.CommIvrService;
import com.cqt.ivr.utils.LocalOrLongUtils;
import com.cqt.ivr.utils.StringUtil;
import com.cqt.ivr.utils.UT;
import com.cqt.starter.redis.util.RedissonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;


//@RestController注解相当于@ResponseBody ＋ @Controller
@RestController
@CrossOrigin
@Api(value = "通用ivr接口", tags = {"通用ivr接口"})
public class CommIvrController {

    private static final Logger logger = LoggerFactory.getLogger(CommIvrController.class);
    @Autowired
    private CommIvrService commIvrService;
    //自动注入RabbitTemplate模板类

    @Resource
    private DynamicConfig dynamicConfig;

    @Resource
    private RedissonUtil redissonUtilL;

    @RequestMapping(value = "/getIvrInfoList", method = RequestMethod.POST)
    @ApiOperation(value= "获取Ivr信息")
    public TableResult getIvrInfoList(@RequestBody CommIvrReq commIvrReq) {
        String LOG_TAG = UUID.randomUUID().toString() + " | 获取Ivr信息 | ";
        if (null == commIvrReq.getPageNo()){
            commIvrReq.setPageNo(1);
        }
        if (null == commIvrReq.getPageSize()){
            commIvrReq.setPageSize(10);
        }
        logger.info(LOG_TAG + "commIvrReq:" + commIvrReq);
        TableResult tableResult = commIvrService.getIvrInfoList(commIvrReq, LOG_TAG);
        return tableResult;
    }

    @RequestMapping(value = "/buryingPoint", method = RequestMethod.POST)
    @ApiOperation(value= "触发埋点信息")
    public BuryingPointRes buryingPoint(BuryingPointReq req) {
        String LOG_TAG = UUID.randomUUID().toString() + " | 触发埋点信息 | ";
        BuryingPointRes res = new BuryingPointRes();
        res.setCode("0");
        res.setResult("yes");
        res.setBuringPointcode("1");
        String nowTime = StringUtil.getDate("yyyy-MM-dd HH:mm:ss");
        String month = StringUtil.getDate("yyyyMM");
        req.setTriggerTime(nowTime);
        req.setMonth(month);
        logger.info(LOG_TAG + "req:" + req);
        if (StringUtil.isNotEmpty(req.getUuid()) && StringUtil.isNotEmpty(req.getCompany_code())){
            commIvrService.insertAllsByMonth(req, LOG_TAG);
        }else {
            logger.info(LOG_TAG + "请求信息没有uuid或企业标识");
            res.setBuringPointcode("2");
        }
        logger.info(LOG_TAG + "返回信息 | " + JSON.toJSONString(res));
        return res;
    }


    @RequestMapping(value = "/getAgentStatus", method = RequestMethod.POST)
    @ApiOperation("获取坐席状态")
    public AgentStatusRes getAgentStatus(AgentStatusReq req) {
        //agentStatus  1:空闲     2：忙碌    3：离线
        String LOG_TAG = UUID.randomUUID().toString() + " | 获取坐席状态 | ";
        logger.info(LOG_TAG + "req:" + req);
        AgentStatusRes res = new AgentStatusRes();
        res.setCode("0");
        res.setResult("yes");
        try {
            String agentStatus = "";
            try {
                agentStatus = redissonUtilL.get("cloudcc:agentStatus:" + req.getCompany_code() + ":" + req.getAgentid());
            }catch (Exception e){
                logger.error(LOG_TAG + "操作redis异常：", e);
            }
            if (StringUtil.isNotEmpty(agentStatus)){
                logger.info(LOG_TAG+ "agentStatus:" + agentStatus);
                JSONObject jsons = JSONObject.parseObject(agentStatus);
                String agentstatus = jsons.get("targetStatus")+"";
                if ("FREE".equals(agentstatus)){
                    res.setAgentStatus("1");
                }else if ("OFFLINE".equals(agentstatus)){
                    res.setAgentStatus("3");
                }else{
                    res.setAgentStatus("2");
                }
            }else {
                res.setAgentStatus("3");
            }
        }catch (Exception e){
            res.setResult("处理异常");
            logger.error("处理异常：", e);
        }
        logger.info(LOG_TAG + "返回信息 | " + JSON.toJSONString(res));
        return res;

    }


    @RequestMapping(value = "/getQueueStatus", method = RequestMethod.POST)
    @ApiOperation("获取技能组状态")
    public QueueStatusRes getQueueStatus(QueueStatusReq req) {
        //queuestatus  1:空闲     2：忙碌   3：无人上班
        String LOG_TAG = UUID.randomUUID().toString() + " | 获取技能组状态 | ";
        String companyCode = req.getCompany_code();
        if (StringUtil.isEmpty(companyCode)){
            if (req.getSysQueueid().contains("_")){
                companyCode = req.getSysQueueid().substring(0,req.getSysQueueid().indexOf("_"));
                req.setCompany_code(companyCode);
            }
        }
        logger.info(LOG_TAG + "req:" + req);
        QueueStatusRes res = new QueueStatusRes();
        res.setCode("0");
        res.setResult("yes");
        try{
            Map<String, String> freeAgentMap = null;
            Map<String, String> allSkillMap = null;
            Map<String, String> skillAgentMap = null;
            Map<String, String> allAgentStatusMap = null;
            try {
                freeAgentMap = redissonUtilL.getStringMap("cloudcc:freeAgent:" + req.getCompany_code() + ":" + req.getSysQueueid());
            }catch (Exception e){
                logger.error(LOG_TAG + "操作redis异常：", e);
            }
            if (!freeAgentMap.isEmpty()){
                res.setQueueStatus("1");
            }else {
                res.setQueueStatus("3");
                try {
                    allSkillMap = redissonUtilL.getStringMap(req.getCompany_code() + ":all:skill");
                }catch (Exception e){
                    logger.error(LOG_TAG + "操作redis异常：", e);
                }
                String skillName = allSkillMap.get(req.getSysQueueid());
                if (StringUtil.isNotEmpty(skillName)){
                    try {
                        skillAgentMap = redissonUtilL.getStringMap(req.getCompany_code() + ":agent_skill_relation");
                    }catch (Exception e){
                        logger.error(LOG_TAG + "操作redis异常：", e);
                    }
                    String agentidsValue = skillAgentMap.get(skillName);
                    JSONObject jsonObject = JSONObject.parseObject(agentidsValue);
                    JSONArray agentIds = jsonObject.getJSONArray("agentIds");
                    try {
                        allAgentStatusMap = redissonUtilL.getStringMap("cloudcc:allAgentStatus:" + req.getCompany_code());
                    }catch (Exception e){
                        logger.error(LOG_TAG + "操作redis异常：", e);
                    }
                    List<String> statusList = new ArrayList<>();
                    for (Object agentId : agentIds) {
                        if(agentId == null){
                            continue;
                        }
                        logger.info(LOG_TAG+ "agentid:" + agentId);
                        String agentStatus = allAgentStatusMap.get(agentId);
                        if (StringUtil.isNotEmpty(agentStatus)){
                            logger.info(LOG_TAG+ "agentStatus:" + agentStatus);
                            JSONObject jsons = JSONObject.parseObject(agentStatus);
                            String agentstatus = jsons.get("targetStatus")+"";
                            if ("FREE".equals(agentstatus)){
                                res.setQueueStatus("1");
                                return res;
                            }else if ("OFFLINE".equals(agentstatus)){
                                statusList.add("3");
                            }else{
                                statusList.add("2");
                            }
                        }
                    }
                    if (statusList.contains("2")){
                        res.setQueueStatus("2");
                    }else {
                        res.setQueueStatus("3");
                    }
                }else {
                    res.setQueueStatus("3");
                }
            }
        }catch (Exception e){
            res.setResult("处理异常");
            logger.error("处理异常：", e);
        }
        logger.info(LOG_TAG + "返回信息 | " + JSON.toJSONString(res));
        return res;

    }

    @RequestMapping(value = "/judgeWeight", method = RequestMethod.POST)
    @ApiOperation("判断货物重量")
    public JudgeWeightRes judgeWeight(JudgeWeightReq req) {
        //weightcode  1:输入错误     2：正常    3：超过或低于
        String LOG_TAG = UUID.randomUUID().toString() + " | 判断货物重量 | ";
        logger.info(LOG_TAG + "req:" + req);
        JudgeWeightRes res = new JudgeWeightRes();
        res.setCode("0");
        res.setResult("yes");
        try{
            if (StringUtil.isNotEmpty(req.getMaxWeight())){
                if (StringUtil.isNumber(req.getWeightPushKey())){
                    if (Integer.parseInt(req.getWeightPushKey()) > Integer.parseInt(req.getMaxWeight())){
                        res.setWeightCode("3");
                    }else{
                        res.setWeightCode("2");
                    }
                }else {
                    res.setWeightCode("1");
                }
            }else if (StringUtil.isNotEmpty(req.getMinWeight())){
                if (StringUtil.isNumber(req.getWeightPushKey())){
                    if (Integer.parseInt(req.getWeightPushKey()) < Integer.parseInt(req.getMinWeight())){
                        res.setWeightCode("3");
                    }else{
                        res.setWeightCode("2");
                    }
                }else {
                    res.setWeightCode("1");
                }
            }else {
                res.setWeightCode("1");
            }
        }catch (Exception e){
            res.setResult("处理异常");
            logger.error("处理异常：", e);
        }
        logger.info(LOG_TAG + "返回信息 | " + JSON.toJSONString(res));
        return res;

    }

    @RequestMapping(value = "/addIvrTrackParametersData", method = RequestMethod.POST)
    @ApiOperation(value= "添加ivr随路数据供接口调用")
    public SubmissionRes addIvrTrackParametersData(IvrTrackParametersData req) {
        String LOG_TAG = UUID.randomUUID().toString() + " | 添加ivr随路数据供接口调用 | ";
        logger.info(LOG_TAG + "req:" + req);
        SubmissionRes res = new SubmissionRes();
        res.setCode("0");
        res.setResult("yes");
        try{
            QueueStatusReq queueStatusReq = new QueueStatusReq();
            queueStatusReq.setCompany_code(req.getCompany_code());
            queueStatusReq.setSysQueueid(req.getSysQueueid());
            String queueName = commIvrService.getQueueNameBySysQueueId(queueStatusReq);
            req.setQueueName(queueName);
            BuryingPointReq bpr = new BuryingPointReq();
            String month = StringUtil.getDate("yyyyMM");
            bpr.setMonth(month);
            bpr.setCompany_code(req.getCompany_code());
            bpr.setUuid(req.getUuid());
            logger.info(LOG_TAG + "bpr:" + bpr);
            List<BuryingPointReq> list = commIvrService.getBuryingPointInfo(bpr);
            if (list.size() > 0) {
                String ivrType = list.get(0).getIvrType();
                req.setIvrType(ivrType);
                req.setProperty(req.getProperty());
            }
            String gtAreaCode = "";
            String isMACO = req.getIsMACO();
            if (StringUtil.isEmpty(isMACO)){
                isMACO = "";
            }
            if ("1".equals(isMACO)) {
                gtAreaCode = "853";
            } else if ("850012".equals(req.getCompany_code())) {
                gtAreaCode = "886";
            } else {
                gtAreaCode = "852";
            }
            req.setAreaCode(gtAreaCode);
            if (StringUtil.isNotEmpty(req.getCustomerTypeResult())){
                req.setCustomerType(req.getCustomerTypeResult());
            }
            Integer expireTime = Integer.parseInt(dynamicConfig.getUuidexpiretime());
            redissonUtilL.set("cc_ivr_track_data_" + req.getUuid(), JSON.toJSONString(req));
            redissonUtilL.setTTL("cc_ivr_track_data_" + req.getUuid(), 60 * 60 * expireTime);
        }catch (Exception e){
            res.setResult("处理异常");
            logger.error("处理异常:", e);
        }
        logger.info(LOG_TAG + "返回信息 | " + JSON.toJSONString(res));
        return res;

    }

    @RequestMapping(value = "/getIvrTrackParametersData", method = RequestMethod.GET)
    @ApiOperation(value= "获取ivr随路数据供接口调用")
    public TableResult getIvrTrackParametersData(String uuid) {
        String LOG_TAG = UUID.randomUUID().toString() + " | 获取ivr随路数据供接口调用 | ";
        logger.info(LOG_TAG + "uuid:" + uuid);
        TableResult tableResult = new TableResult();
        tableResult.setMessage("获取成功");
        tableResult.setStatus(0);
        String jsonStr = "";
        try{
            jsonStr = redissonUtilL.get("cc_ivr_track_data_" + uuid);
            logger.info(LOG_TAG + "jsonStr:" + jsonStr);
            if (!UT.zstr(jsonStr)){
                IvrTrackParametersData ivrTrackParametersData = com.alibaba.fastjson.JSONObject.parseObject(jsonStr, IvrTrackParametersData.class);
                logger.info(LOG_TAG + "ivrTrackParametersData:" + JSONObject.toJSONString(ivrTrackParametersData));
                tableResult.setResult(ivrTrackParametersData);
            }
        }catch (Exception e){
            logger.error("redis获取异常:", e);
            tableResult.setMessage("redis获取异常");
            tableResult.setStatus(500);
        }
        logger.info(LOG_TAG + "返回信息 | " + JSON.toJSONString(tableResult));
        return tableResult;

    }

    @RequestMapping(value = "/addCommonIvrTrackParametersData", method = RequestMethod.POST)
    @ApiOperation(value= "添加通用ivr随路数据供接口调用")
    public SubmissionRes addCommonIvrTrackParametersData(HttpServletRequest request) {
        String LOG_TAG = UUID.randomUUID().toString() + " | 添加通用ivr随路数据供接口调用 | ";
        SubmissionRes res = new SubmissionRes();
        res.setCode("0");
        res.setResult("yes");
        Map<String, String> map = new HashMap();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();

            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    System.out.println("参数：" + paramName + "=" + paramValue);
                    map.put(paramName, paramValue);

                }
            }
        }
        try{
            String callerNum = map.get("real_caller");
            String phoneArea = map.get("phoneArea");
            if ("0".equals(phoneArea)){
                phoneArea = LocalOrLongUtils.getNumberCode(callerNum, redissonUtilL);
                map.put("phoneArea" , phoneArea);
            }
            Integer expireTime = Integer.parseInt(dynamicConfig.getUuidexpiretime());
            redissonUtilL.set("cc_ivr_track_data_" + map.get("uuid"), JSON.toJSONString(map));
            redissonUtilL.setTTL("cc_ivr_track_data_" + map.get("uuid"), 60 * 60 * expireTime);
        }catch (Exception e){
            res.setResult("处理异常");
            logger.error("处理异常:", e);
        }
        logger.info(LOG_TAG + "map:" + JSON.toJSONString(map));
        logger.info(LOG_TAG + "uuid:" + map.get("uuid"));

        logger.info(LOG_TAG + "返回信息 | " + JSON.toJSONString(res));
        return res;

    }


    @RequestMapping(value = "/getCommonIvrTrackParametersData", method = RequestMethod.GET)
    @ApiOperation(value= "获取通用ivr随路数据供接口调用")
    public TableResult getCommonIvrTrackParametersData(String uuid) {
        String LOG_TAG = UUID.randomUUID().toString() + " | 获取通用ivr随路数据供接口调用 | ";
        logger.info(LOG_TAG + "uuid:" + uuid);
        TableResult tableResult = new TableResult();
        tableResult.setMessage("获取成功");
        tableResult.setStatus(0);
        String jsonStr = "";
        try{
            jsonStr = redissonUtilL.get("cc_ivr_track_data_" + uuid);
            logger.info(LOG_TAG + "jsonStr:" + jsonStr);
            if (!UT.zstr(jsonStr)){
                tableResult.setResult(JSON.parseObject(jsonStr));
            }
        }catch (Exception e){
            logger.error("redis获取异常:", e);
            tableResult.setMessage("redis获取异常");
            tableResult.setStatus(500);
        }
        logger.info(LOG_TAG + "返回信息 | " + JSON.toJSONString(tableResult));
        return tableResult;

    }


    @RequestMapping(value = "/addAppendIvrTrackParametersData", method = RequestMethod.POST)
    @ApiOperation(value= "添加追加ivr随路数据供接口调用")
    public SubmissionRes addAppendIvrTrackParametersData(HttpServletRequest request) {
        String LOG_TAG = UUID.randomUUID().toString() + " | 添加追加ivr随路数据供接口调用 | ";
        SubmissionRes res = new SubmissionRes();
        res.setCode("0");
        res.setResult("yes");
        Map<String, String> map = new HashMap();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();

            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    System.out.println("参数：" + paramName + "=" + paramValue);
                    map.put(paramName, paramValue);

                }
            }
        }
        try{
            String clearCallData = map.get("clearCallTrackData");
            String callerNum = map.get("real_caller");
            String phoneArea = map.get("phoneArea");
            if ("0".equals(phoneArea)){
                phoneArea = LocalOrLongUtils.getNumberCode(callerNum, redissonUtilL);
                map.put("phoneArea" , phoneArea);
            }
            if ("0".equals(clearCallData)){
                redissonUtilL.delKey("cc_ivr_track_data_" + map.get("uuid"));
            }else {
                Integer expireTime = Integer.parseInt(dynamicConfig.getUuidexpiretime());
                String value = redissonUtilL.get("cc_ivr_track_data_" + map.get("uuid"));
                if (StringUtil.isNotEmpty(value)){
                    JSONObject jsonObject = JSONObject.parseObject(value);
                    Set keyset = map.keySet();
                    if (keyset.size() > 0){
                        for(Object key:keyset){
                            jsonObject.put(key.toString(), map.get(key));
                        }
                    }
                    redissonUtilL.set("cc_ivr_track_data_" + map.get("uuid"), JSON.toJSONString(jsonObject));
                }else {
                    redissonUtilL.set("cc_ivr_track_data_" + map.get("uuid"), JSON.toJSONString(map));
                }
                redissonUtilL.setTTL("cc_ivr_track_data_" + map.get("uuid"), 60 * 60 * expireTime);
            }
        }catch (Exception e){
            res.setResult("处理异常");
            logger.error("处理异常:", e);
        }
        logger.info(LOG_TAG + "map:" + JSON.toJSONString(map));
        logger.info(LOG_TAG + "uuid:" + map.get("uuid"));

        logger.info(LOG_TAG + "返回信息 | " + JSON.toJSONString(res));
        return res;

    }

}
