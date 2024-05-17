package com.cqt.monitor.web.callevent.jobhandler;


import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.monitor.constants.RedisConstant;
import com.cqt.monitor.cache.AreaCodeCache;
import com.cqt.monitor.cache.CorpBusinessConfigCache;
import com.cqt.monitor.common.util.DingUtil;
import com.cqt.monitor.web.callevent.entity.*;
import com.cqt.monitor.web.callevent.rabbitmq.SenderJob;
import com.cqt.redis.util.RedissonUtil;
import com.google.common.collect.Lists;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
@RequiredArgsConstructor
public class WarningJob {


    private final PlatProperty platProperty;

    private final DingUtil dingUtil;

    private final SenderJob senderjob;


    private final RedissonUtil redissonUtil;


    @Resource(name = "threadPool")
    private ThreadPoolTaskExecutor saveExecutor;


    @XxlJob("testJob")
    public void makeData() {
        String jobParam = XxlJobHelper.getJobParam();
        int i1 = Integer.parseInt(jobParam);
        saveExecutor.execute(() -> {
            for (int i = 0; i < i1; i++) {
                Callstat callstat = new Callstat();
                callstat.setGroupnumber("4042");
                callstat.setKey3("025");
                callstat.setSpecificchargedpar("18662714289");
                callstat.setServicekey("900007");
                callstat.setDuration("17");
                callstat.setExtforwardnumber("10");
                senderjob.sendTest(callstat);
            }
        });

        log.info("发送完成");

    }


    /**
     * 通过话单存储告警基础信息
     */
    @XxlJob("countPrivateInfo")
    public void countPrivateInfo() {
        log.info("countPrivateInfo开始执行");
        String lastOneTime = getTimeByPattern(System.currentTimeMillis() - 60 * 1000L, "yyyyMMddHHmm");
        String keyInCorp;
        String formValue = platProperty.getFormValue();
        if ("nj".equals(platProperty.getFormValue())) {
            keyInCorp = RedisConstant.GRANULARITY_IN_CORPNJ;
        } else {
            keyInCorp = RedisConstant.GRANULARITY_IN_CORPYZ;
        }
        Set<String> strings = AreaCodeCache.all().keySet();
        ArrayList<String> list = new ArrayList<>(strings);
        List<List<String>> partition = Lists.partition(list, 100);
        for (Map.Entry<String, PrivateCorpBusinessInfoDTO> corpEntry : CorpBusinessConfigCache.all().entrySet()) {
            for (List<String> stringList : partition) {
                saveExecutor.execute(() -> {
                    for (String area : stringList) {
                        Set<String> set = redissonUtil.getSet(RedisConstant.SUPPLIER_INFO);
                        for (String supplierId : set) {
                            String format = String.format(keyInCorp, corpEntry.getKey(), area, lastOneTime, supplierId);
                            int cdr = 0;
                            int ring = 0;
                            int pick = 0;
                            if (redissonUtil.isExistString(formValue, format)) {
                                if (redissonUtil.getHashByItem(formValue, format, "cdr") != null) {
                                    cdr = Integer.parseInt(redissonUtil.getHashByItem(formValue, format, "cdr").toString());
                                }
                                if (redissonUtil.getHashByItem(formValue, format, "ring") != null) {
                                    ring = Integer.parseInt(redissonUtil.getHashByItem(formValue, format, "ring").toString());
                                }
                                if (redissonUtil.getHashByItem(formValue, format, "pick") != null) {
                                    pick = Integer.parseInt(redissonUtil.getHashByItem(formValue, format, "pick").toString());
                                }
                                if (cdr != 0) {
                                    try {
                                        EventInMin event = new EventInMin();
                                        event.setCdrCount(cdr);
                                        event.setPickUpCount(pick);
                                        event.setRinging(ring);
                                        event.setVccId(corpEntry.getKey());
                                        event.setAreaCode(area);
                                        event.setSupplierId(supplierId);
                                        event.setPlatForm(platProperty.getFormValue());
                                        event.setRingRate(new BigDecimal((float) ring / cdr).setScale(2, RoundingMode.HALF_UP));
                                        event.setPickUpRate(new BigDecimal((float) pick / cdr).setScale(2, RoundingMode.HALF_UP));
                                        try {
                                            String time = lastOneTime + "00";
                                            Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(time);
                                            String stime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                                            event.setCurrentMin(stime);
                                            senderjob.send(event);
                                            log.info("发送消息到队列private_warn_event_queues：" + JSONUtil.toJsonStr(event));
                                        } catch (Exception e) {
                                            log.error("基础数据发送mq异常 {} ", e.getMessage());
                                        }

                                    } catch (Exception ex) {
                                        log.info("數據計算異常");
                                        ex.printStackTrace();
                                    }
                                }

                            }
                        }

                    }

                });
            }
        }
    }



    @XxlJob("privateWarningJob")
    public void privateWarning() throws ParseException {
        String jobId = String.valueOf(XxlJobHelper.getJobId());
        log.info("jobid:"+jobId);
        String formValue = platProperty.getFormValue();
        String confId = redissonUtil.getString(formValue,RedisConstant.JOB_ID+jobId);
        String o1 = redissonUtil.getString(formValue,RedisConstant.WARN_CONFIG + confId);
        Set<String> set1 = redissonUtil.getSet(formValue,RedisConstant.WARN_RULE + confId);
        log.info("configID:"+confId);
        WarningConfig warningConfig = JSONUtil.toBean(JSONUtil.toJsonStr(o1),WarningConfig.class);
        String startTime = warningConfig.getStartTime();
        String endTime = warningConfig.getEndTime();
        if (!startTime.equals(endTime)){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            Date target = sdf.parse(sdf.format(new Date()));
            boolean in = DateUtil.isIn(target, start, end);
            if (!in){
                log.info("当前告警配置ID：{} 不在生效时间（{}~{}）",warningConfig.getId(),startTime,endTime);
                return;
            }
        }
        String platForm = warningConfig.getPlatForm();
        String njPlat = null;
        String yzPlat = null;
        if (platForm.contains(",")){
            String[] split = platForm.split(",");
            if ("0".equals(split[0])){
                njPlat=split[0];
                yzPlat=split[1];
            }else {
                njPlat = split[1];
                yzPlat = split[0];
            }
        }else {
            if ("0".equals(platForm)){
                njPlat = platForm;
            }else {
                yzPlat = platForm;
            }
        }
        String cityGroup = warningConfig.getAreaCodes();
        String vccId = warningConfig.getVccId();
        String[] split = cityGroup.split(",");
        Set<String> cities = new HashSet<>(Arrays.asList(split));
        if (cityGroup.contains("0000")){
            cities = AreaCodeCache.all().keySet();
        }
        Integer granularity = warningConfig.getGranularity();

        //查询出该条企业下对应的所有告警规则
        List<WarningRule> warningRules = new ArrayList<>();
        for (String uid : set1) {
           warningRules.add( JSONUtil.toBean(JSONUtil.toJsonStr(uid),WarningRule.class));
        }
        //总计的告警规则
        List<WarningRule> totalRule = new ArrayList<>();
        //不总计
        List<WarningRule> untotalRule = new ArrayList<>();
        for (WarningRule warningRule : warningRules) {
            if (warningRule.getCycleCount().equals(0)){
                totalRule.add(warningRule);
            }else {
                untotalRule.add(warningRule);
            }
        }
        //获取告警规则的统计周期和统计条件,同一告警配置下的所有规则的统计周期和统计条件相同
        WarningRule warningRule = warningRules.get(0);
        //统计周期
        Integer countCycle = warningRule.getCountCycle();
        //周期条件
        Integer cycleCondition = warningRule.getCycleCondition();
        String[] supplierIds = warningConfig.getSupplierId().split(",");
        //周期计算
        // 0:企业，1：企业号码
        //生效颗粒度为企业
        if (granularity.equals(0)) {
            log.info("生效颗粒度为企业");
            for (String areaCode : cities) {
                for (String supplierId : supplierIds) {
                    int pickUpNj = 0;
                    int ringNj = 0;
                    int cdrNj = 0;
                    int pickYz = 0;
                    int ringYz = 0;
                    int cdrYz = 0;
                    for (int i = 1; i <= countCycle; i++) {
                        String lastOneTime = getTimeByPattern(System.currentTimeMillis() - 60 * 1000L * i, "yyyyMMddHHmm");
                        //统计南京平台数据
                        if (njPlat != null) {
                            String format = String.format(RedisConstant.GRANULARITY_IN_CORPNJ, vccId, areaCode, lastOneTime, supplierId);
                            if (redissonUtil.isExistString(formValue, format)) {
                                if (redissonUtil.getHashByItem(formValue, format, "cdr") != null) {
                                    cdrNj += Integer.parseInt(String.valueOf(redissonUtil.getHashByItem(formValue, format, "cdr")));
                                }
                                if (redissonUtil.getHashByItem(formValue, format, "ring") != null) {
                                    ringNj += Integer.parseInt(String.valueOf(redissonUtil.getHashByItem(formValue, format, "ring")));

                                }
                                if (redissonUtil.getHashByItem(formValue, format, "pick") != null) {
                                    pickUpNj += Integer.parseInt(String.valueOf(redissonUtil.getHashByItem(formValue, format, "pick")));

                                }
                            }
                        }
                        //统计扬州平台数据
                        if (yzPlat != null) {
                            String format = String.format(RedisConstant.GRANULARITY_IN_CORPYZ, vccId, areaCode, lastOneTime, supplierId);
                            if (redissonUtil.isExistString(formValue, format)) {
                                if (redissonUtil.getHashByItem(formValue, format, "cdr") != null) {
                                    cdrYz += Integer.parseInt(String.valueOf(redissonUtil.getHashByItem(formValue, format, "cdr")));
                                }
                                if (redissonUtil.getHashByItem(formValue, format, "ring") != null) {
                                    ringYz += Integer.parseInt(String.valueOf(redissonUtil.getHashByItem(formValue, format, "ring")));

                                }
                                if (redissonUtil.getHashByItem(formValue, format, "pick") != null) {
                                    pickYz += Integer.parseInt(String.valueOf(redissonUtil.getHashByItem(formValue, format, "pick")));

                                }
                            }

                        }
                    }
                    if (yzPlat != null) {
                        isWarn(platForm, warningConfig, warningRules, totalRule, untotalRule, pickYz, ringYz, cdrYz, cycleCondition, areaCode, supplierId);
                    }
                    if (njPlat != null) {
                        isWarn(platForm, warningConfig, warningRules, totalRule, untotalRule, pickUpNj, ringNj, cdrNj, cycleCondition, areaCode, supplierId);
                    }
                }

            }

        }
        //生效颗粒度为企业号码
        if (granularity.equals(1)) {
            for (String areaCode : cities) {
                for (String supplierId : supplierIds) {
                    String format = String.format(RedisConstant.NUMBER_CORP, vccId, areaCode);
                    if (njPlat != null) {
                        log.info("生效颗粒度为企业号码(nj)");
                        Set<String> set = redissonUtil.getSet(formValue, format);
                        for (String o : set) {
                            int pickNj = 0;
                            int ringNj = 0;
                            int cdrNj = 0;
                            String num = String.valueOf(o);
                            for (int i = 1; i <= countCycle; i++) {
                                String lastOneTime = getTimeByPattern(System.currentTimeMillis() - 60 * 1000L * i, "yyyyMMddHHmm");
                                String format1 = String.format(RedisConstant.GRANULARITY_IN_NUMNJ, vccId, areaCode, num, lastOneTime, supplierId);
                                if (redissonUtil.getHashByItem(formValue, format1, "cdr") != null) {
                                    cdrNj += Integer.parseInt(String.valueOf(redissonUtil.getHashByItem(formValue, format1, "cdr")));
                                }
                                if (redissonUtil.getHashByItem(formValue, format1, "ring") != null) {
                                    ringNj += Integer.parseInt(String.valueOf(redissonUtil.getHashByItem(formValue, format1, "ring")));
                                }
                                if (redissonUtil.getHashByItem(formValue, format1, "pick") != null) {
                                    pickNj += Integer.parseInt(String.valueOf(redissonUtil.getHashByItem(formValue, format1, "pick")));

                                }
                            }

                            isWarnForNum(platForm, warningConfig, warningRules, totalRule, untotalRule, pickNj, ringNj, cdrNj, cycleCondition, num, areaCode, supplierId);
                        }
                    }
                    if (yzPlat != null) {
                        log.info("生效颗粒度为企业号码(yz)");
                        Set<String> set = redissonUtil.getSet(formValue, format);
                        for (String o : set) {
                            int pickYz = 0;
                            int ringYz = 0;
                            int cdrYz = 0;
                            String num = String.valueOf(o);
                            for (int i = 1; i <= countCycle; i++) {
                                String lastOneTime = getTimeByPattern(System.currentTimeMillis() - 60 * 1000L * i, "yyyyMMddHHmm");
                                String format1 = String.format(RedisConstant.GRANULARITY_IN_NUMYZ, vccId, areaCode, num, lastOneTime, supplierId);
                                if (redissonUtil.getHashByItem(formValue, format1, "cdr") != null) {
                                    cdrYz += Integer.parseInt(String.valueOf(redissonUtil.getHashByItem(formValue, format1, "cdr")));
                                }
                                if (redissonUtil.getHashByItem(formValue, format1, "ring") != null) {
                                    ringYz += Integer.parseInt(String.valueOf(redissonUtil.getHashByItem(formValue, format1, "ring")));

                                }
                                if (redissonUtil.getHashByItem(formValue, format1, "pick") != null) {
                                    pickYz += Integer.parseInt(String.valueOf(redissonUtil.getHashByItem(formValue, format1, "pick")));

                                }
                            }

                            isWarnForNum(platForm, warningConfig, warningRules, totalRule, untotalRule, pickYz, ringYz, cdrYz, cycleCondition, num, areaCode, supplierId);


                        }
                    }
                }


            }
            }

        }

    @Async("threadPool")
    public void isWarn(String platform, WarningConfig warningConfig, List<WarningRule> warningRules, List<WarningRule> totalRule,
                       List<WarningRule> untotalRule, int pickUpNj, int ringNj, int cdrNj, int cycleCondition,
                       String areaCode, String supplierId) {
        String formValue = platProperty.getFormValue();
        String format;
        String key;
        if ("0".equals(platform)) {
            format = String.format(RedisConstant.CORPTOTALCOUNTNJ, warningConfig.getId(), areaCode, supplierId);
            key = RedisConstant.CORPTOTALCOUNTVALUENJ;
        } else {
            format = String.format(RedisConstant.CORPTOTALCOUNTYZ, warningConfig.getId(), areaCode, supplierId);
            key = RedisConstant.CORPTOTALCOUNTVALUEYZ;
        }
        if (cdrNj == 0) {
            redissonUtil.delKey(formValue, format);
            for (WarningRule warningRule : totalRule) {
                String format1 = String.format(key, warningRule.getId(), areaCode, supplierId);
                redissonUtil.delKey(formValue, format1);
            }
            return;
        }
        log.info("话单量：{},振铃量：{},接通量：{}", cdrNj, ringNj, pickUpNj);
        WarningRule warningRule2 = warningRules.get(0);
        Integer coutCy = warningRule2.getCountCycle();
        int i = 0;
        redissonUtil.increment(formValue, format);
        String str = redissonUtil.getString(formValue, format);
        String ringKey = "_monitor_ring_" + warningConfig.getId() + "_" + areaCode + "_" + supplierId;
        String pickKey = "_monitor_pick_" + warningConfig.getId() + "_" + areaCode + "_" + supplierId;
        redissonUtil.setObject(formValue, str + ringKey, ringNj, 14400, TimeUnit.SECONDS);
        redissonUtil.setObject(formValue, str + pickKey, pickUpNj, 14400, TimeUnit.SECONDS);
        log.info("当前为第 {} 周期,地市-{}，供应商-{}", str, areaCode, supplierId);
        for (WarningRule warnRule : totalRule) {
            BigDecimal value1 = getValue(warnRule, pickUpNj, ringNj, cdrNj);
            redissonUtil.setObject(formValue, str + "_monitor_rule_" + warnRule.getId() + "_" + areaCode + "_" + supplierId, value1, 14400, TimeUnit.SECONDS);
            //总计
            if (warnRule.getCycleCount().equals(0)) {
                String valueKey = String.format(key, warnRule.getId(), areaCode, supplierId);
                BigDecimal value = getValue(warnRule, pickUpNj, ringNj, cdrNj);

                if (!redissonUtil.isExistString(formValue, valueKey)) {
                    redissonUtil.setObject(formValue, valueKey, value, 14400, TimeUnit.SECONDS);
                } else {
                    //过去几个周期基础数据的累加
                    BigDecimal bigDecimal = getBigDecimal(redissonUtil.getString(formValue, valueKey));
                    if (bigDecimal != null) {
                        BigDecimal add = bigDecimal.add(value);
                        redissonUtil.setObject(formValue, valueKey, add, 14400, TimeUnit.SECONDS);
                    }
                }

            }

        }

        for (WarningRule warningRule : untotalRule) {
            boolean warn = getWarn(warningRule, pickUpNj, ringNj, cdrNj);
            if (warn) {
                BigDecimal value = getValue(warningRule, pickUpNj, ringNj, cdrNj);
                //将每个周期的各个规则的值存入redis
                String s = String.valueOf(redissonUtil.getString(formValue, format));
                redissonUtil.setObject(formValue, s + "_monitor_rule_" + warningRule.getId() + "_" + areaCode + "_" + supplierId, value, 14400, TimeUnit.SECONDS);
                i++;
            }
        }

        if (i == untotalRule.size()) {
            if (Integer.parseInt(redissonUtil.getString(formValue, format)) >= cycleCondition) {
                //已经到了周期，无论是否告警，计数器清零
                redissonUtil.delKey(formValue, format);
                int x = 0;
                for (WarningRule warningRule1 : totalRule) {
                    String format1 = String.format(key, warningRule1.getId(), areaCode, supplierId);
                    //总计的值
                    String s = redissonUtil.getString(formValue, format1);
                    redissonUtil.delKey(formValue, format1);
                    log.info("总计的值为：{},阈值为：{}", s, warningRule1.getThreshold());
                    boolean threshold = isThreshold(new BigDecimal(s), warningRule1.getCompareCondition(), new BigDecimal(warningRule1.getThreshold()));
                    if (threshold) {
                        x++;
                    }
                }

                //总计的规则到达阈值
                if (x == totalRule.size()) {
                    //告警
                    log.info("告警");
                    StringBuilder detail = new StringBuilder();
                    for (int j = 1; j < cycleCondition + 1; j++) {
                        String lastOneTime = getTimeByPattern(System.currentTimeMillis() - 60 * 1000L * ((long) (cycleCondition - j) * coutCy + 1), "HH:mm");
                        StringBuilder eveMin = new StringBuilder("\r\n" + lastOneTime + ": 【");
                        int remake = 0;
                        int remake1 = 0;
                        for (WarningRule warningRule : warningRules) {
                            if (warningRule.getBasicData().equals(4) || warningRule.getBasicData().equals(5)) {
                                remake = 1;
                            }
                            if (warningRule.getBasicData().equals(6) || warningRule.getBasicData().equals(7)) {
                                remake1 = 1;
                            }
                            String s = warnCon(warningRule.getBasicData());
                            if (warningRule.getBasicData().equals(0)) {
                                String o1 = redissonUtil.getString(formValue, j + "_monitor_rule_" + warningRule.getId() + "_" + areaCode + "_" + supplierId);
                                eveMin.append(s).append(o1).append(",");
                            } else {
                                String o1 = redissonUtil.getString(formValue, j + "_monitor_rule_" + warningRule.getId() + "_" + areaCode + "_" + supplierId);
                                DecimalFormat df = new DecimalFormat("0.00%");
                                String format1 = df.format(Double.parseDouble(o1));
                                eveMin.append(s).append(format1).append(",");
                            }
                        }
                        if (remake == 1) {
                            Object o1 = redissonUtil.getString(formValue, j + ringKey);
                            eveMin.append("振铃量：").append(o1).append(",");
                        }
                        if (remake1 == 1) {
                            Object o1 = redissonUtil.getString(formValue, j + pickKey);
                            eveMin.append("接通量：").append(o1).append(",");
                        }
                        String substring = eveMin.substring(0, eveMin.length() - 1);
                        substring += "】";
                        detail.append(substring);
                    }

                    dingUtil.sendWarnMessage(warningConfig, null, detail.toString(), areaCode, supplierId);
                    return;
                }
                log.info("所有不总计的规则到达阈值，但总计规则未到达阈值，计数器清零");
                redissonUtil.delKey(formValue, format);
                return;
            }
            //所有不总计的规则都到达阈值且没有到达周期条件，计数器++
            log.info("所有不总计的规则都到达阈值且没有到达周期条件，计数器++");
        } else {
            log.info("不总计的规则没有全部到达阈值，计数器清零，总计的值也清零");
            //不总计的规则没有全部到达阈值，计数器清零
            redissonUtil.delKey(formValue, format);
        }
    }


    @Async("threadPool")
    public void isWarnForNum(String platform, WarningConfig warningConfig, List<WarningRule> warningRules,
                             List<WarningRule> totalRule, List<WarningRule> untotalRule,
                             int pickUpNj, int ringNj, int cdrNj, int cycleCondition,
                             String num, String areaCode, String supplierId) {
        String format;
        String key;
        if ("0".equals(platform)) {
            format = String.format(RedisConstant.CORPTOTALCOUNTNUMNJ, warningConfig.getId(), num, areaCode, supplierId);
            key = RedisConstant.CORPTOTALCOUNTVALUENUMNJ;
        } else {
            format = String.format(RedisConstant.CORPTOTALCOUNTNUMYZ, warningConfig.getId(), num, areaCode, supplierId);
            key = RedisConstant.CORPTOTALCOUNTVALUENUMYZ;
        }

        String formValue = platProperty.getFormValue();
        if (cdrNj == 0) {
            redissonUtil.delKey(formValue,format);
            for (WarningRule warningRule : totalRule) {
                String format1 = String.format(key, warningRule.getId(), num, areaCode, supplierId);
                redissonUtil.delKey(formValue,format1);
            }
            return;
        }
        log.info("话单量：{},振铃量：{},接通量：{}", cdrNj, ringNj, pickUpNj);
        WarningRule warningRule2 = warningRules.get(0);
        Integer coutCy = warningRule2.getCountCycle();
        int i = 0;

        redissonUtil.increment(formValue, format);
        String o = redissonUtil.getString(format);
        String ringKey = "_monitor_ring_" + warningConfig.getId() + "_" + num + "_" + areaCode + "_" + supplierId;
        String pickKey = "_monitor_pick_" + warningConfig.getId() + "_" + num + "_" + areaCode + "_" + supplierId;
        redissonUtil.setObject(formValue, o + ringKey, ringNj, 14400, TimeUnit.SECONDS);
        redissonUtil.setObject(formValue, o + pickKey, pickUpNj, 14400, TimeUnit.SECONDS);
        log.info("当前为第 {} 周期,地市-{}，供应商-{},号码-{}", o, areaCode, supplierId, num);
        for (WarningRule warnRule : totalRule) {
            String valueKey = String.format(key, warnRule.getId(), num, areaCode, supplierId);
            BigDecimal value = getValue(warnRule, pickUpNj, ringNj, cdrNj);
            redissonUtil.setObject(formValue, o + "_monitor_rule_" + warnRule.getId() + "_" + num + "_" + areaCode + "_" + supplierId, value, 14400, TimeUnit.SECONDS);
            //总计
            if (warnRule.getCycleCount().equals(0)) {


                if (!redissonUtil.isExistString(formValue, valueKey)) {
                    redissonUtil.setObject(formValue, valueKey, value, 14400, TimeUnit.SECONDS);
                } else {
                    //过去几个周期基础数据的累加
                    BigDecimal bigDecimal = getBigDecimal(redissonUtil.getString(formValue, valueKey));
                    if (bigDecimal != null) {
                        BigDecimal add = bigDecimal.add(value);
                        redissonUtil.setObject(formValue, valueKey, add, 14400, TimeUnit.SECONDS);
                    }
                }
            }
        }

        for (WarningRule warningRule : untotalRule) {
            boolean warn = getWarn(warningRule, pickUpNj, ringNj, cdrNj);
            if (warn){
                BigDecimal value = getValue(warningRule, pickUpNj, ringNj, cdrNj);
                //将每个周期的各个规则的值存入redis
                redissonUtil.setObject(formValue, o + "_monitor_rule_" + warningRule.getId() + "_" + num + "_" + areaCode + "_" + supplierId, value, 14400, TimeUnit.SECONDS);
                i++;
            }
        }

        //所有不总计的规则都到达阈值
        if (i==untotalRule.size()){
            if (Integer.parseInt(redissonUtil.getString(formValue,format))==cycleCondition){
                //已经到了周期，无论是否告警，计数器清零
                redissonUtil.delKey(formValue,format);
                int x = 0;
                for (WarningRule warningRule1:totalRule) {
                    String format1 = String.format(key, warningRule1.getId(), num, areaCode, supplierId);
                    String s = String.valueOf(redissonUtil.getString(formValue, format1));
                    log.info("总计的值为：{},阈值为：{}",s,warningRule1.getThreshold());
                    boolean threshold = isThreshold(new BigDecimal(String.valueOf(redissonUtil.getString(formValue,format1))), warningRule1.getCompareCondition(), new BigDecimal(warningRule1.getThreshold()));
                    //总计的值也清零
                    redissonUtil.delKey(formValue,format1);
                    if (threshold){
                        x++;
                    }
                }
                //总计的规则到达阈值
                if (x==totalRule.size()){
                    //告警
                    log.info("触发告警,号码："+num);
                    StringBuilder detail = new StringBuilder();
                    for (int j = 1; j  <cycleCondition+1; j++) {
                        String lastOneTime = getTimeByPattern(System.currentTimeMillis() - 60 * 1000L*((long) (cycleCondition -j)*coutCy+1), "HH:mm");
                        StringBuilder eveMin = new StringBuilder("\r\n" + lastOneTime + ": 【");
                        int remake = 0;
                        int remake1 = 0;
                        for (WarningRule warningRule : warningRules) {
                            if (warningRule.getBasicData().equals(4)||warningRule.getBasicData().equals(5)){
                                remake = 1;
                            }
                            if (warningRule.getBasicData().equals(6)||warningRule.getBasicData().equals(7)){
                                remake1 = 1;
                            }
                            String s = warnCon(warningRule.getBasicData());
                            if (warningRule.getBasicData().equals(0)){
                                Object o1 = redissonUtil.getString(formValue, j + "_monitor_rule_" + warningRule.getId() + "_" + num + "_" + areaCode + "_" + supplierId);
                                eveMin.append(s).append(o1).append(",");
                            }else {
                                String o1 = redissonUtil.getString(formValue, j + "_monitor_rule_" + warningRule.getId() + "_" + num + "_" + areaCode + "_" + supplierId);
                                DecimalFormat df = new DecimalFormat("0.00%");
                                String format1 = df.format(Double.valueOf(o1));
                                eveMin.append(s).append(format1).append(",");
                            }
                        }
                        if (remake==1){
                            Object o1 = redissonUtil.getString(formValue, j + ringKey);
                            eveMin.append("振铃量：").append(o1).append(",");
                        }
                        if (remake1 == 1){
                            Object o1 = redissonUtil.getString(formValue, j + pickKey);
                            eveMin.append("接通量：").append(o1).append(",");
                        }
                        String substring = eveMin.substring(0, eveMin.length() - 1);
                        substring+="】";
                        detail.append(substring);
                    }
                    dingUtil.sendWarnMessage(warningConfig, num, detail.toString(), areaCode, supplierId);
                    return;
                }
                log.info("所有不总计的规则都到达阈值但总计未到达阈值，计数器清零");
                redissonUtil.delKey(formValue,format);
                return;
            }
            //所有不总计的规则都到达阈值且没有到达周期条件，计数器++
            log.info("所有不总计的规则都到达阈值且没有到达周期条件，计数器++");
        }else{
            //不总计的规则没有全部到达阈值，计数器清零
            log.info("不总计的规则没有全部到达阈值，计数器清零");
            redissonUtil.delKey(formValue,format);
        }






    }

    private String warnCon(Integer basicData){
        if (basicData.equals(0)){
            return "话单量：";
        }else if (basicData.equals(4)){
            return "振铃率：";
        } else if (basicData.equals(5)) {
            return "振铃失败率：";
        } else if (basicData.equals(6)) {
            return "接通率：";
        } else  {
            return "接通失败率：";
        }

    }

    /**
     * 获取配置规则中基础数据的值

     */
    private BigDecimal getValue(WarningRule warnRule,int pickUp,int ring,int cdr){
        if (warnRule.getBasicData().equals(0)) {
            return BigDecimal.valueOf(cdr);
        }
        //振铃率
        if (warnRule.getBasicData().equals(4)) {
            double ringRate = BigDecimal.valueOf((float) ring / cdr).setScale(2, RoundingMode.HALF_UP).doubleValue();
            return BigDecimal.valueOf(ringRate).setScale(2, RoundingMode.HALF_UP);
        }
        //振铃失败率
        if (warnRule.getBasicData().equals(5)) {
            double ringRate = BigDecimal.valueOf((float) ring / cdr).setScale(2, RoundingMode.HALF_UP).doubleValue();
            BigDecimal bDecimal = BigDecimal.valueOf(ringRate);
            double ringFailRate = 1 - bDecimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
            return BigDecimal.valueOf(ringFailRate).setScale(2, RoundingMode.HALF_UP);
        }
        //接通率
        if (warnRule.getBasicData().equals(6)) {
            double pickUpRate = BigDecimal.valueOf((float) pickUp / cdr).setScale(2, RoundingMode.HALF_UP).doubleValue();
            return BigDecimal.valueOf(pickUpRate).setScale(2, RoundingMode.HALF_UP);
        }
        //接通失败率
        if (warnRule.getBasicData().equals(7)) {
            double pickRate = BigDecimal.valueOf((float) pickUp / cdr).setScale(2, RoundingMode.HALF_UP).doubleValue();
            BigDecimal bDecimal = BigDecimal.valueOf(pickRate);
            double pickFailRate = 1 - bDecimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
            return BigDecimal.valueOf(pickFailRate).setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(0);
    }

    /**
     * 判断基础数据是否到达告警阈值
     */
    private boolean getWarn(WarningRule warnRule, int pickUp, int ring, int cdr) {
        //话单量
        boolean threshold = false;
        if (warnRule.getBasicData().equals(0)) {
            threshold = isThreshold(BigDecimal.valueOf(cdr), warnRule.getCompareCondition(), new BigDecimal(warnRule.getThreshold()));
        }
        //振铃率
        if (warnRule.getBasicData().equals(4)) {
            BigDecimal ringRate = BigDecimal.valueOf((float) ring / cdr).setScale(2, RoundingMode.HALF_UP);
            threshold = isThreshold(ringRate, warnRule.getCompareCondition(), new BigDecimal(warnRule.getThreshold()));
        }
        //振铃失败率
        if (warnRule.getBasicData().equals(5)) {
            BigDecimal ringRate = BigDecimal.valueOf((float) ring / cdr).setScale(2, RoundingMode.HALF_UP);
            double ringFailRate = 1 - ringRate.setScale(2, RoundingMode.HALF_UP).doubleValue();
            threshold = isThreshold(BigDecimal.valueOf(ringFailRate), warnRule.getCompareCondition(), new BigDecimal(warnRule.getThreshold()));
        }
        //接通率
        if (warnRule.getBasicData().equals(6)) {
            BigDecimal pickUpRate = BigDecimal.valueOf((float) pickUp / cdr).setScale(2, RoundingMode.HALF_UP);
            threshold = isThreshold(pickUpRate, warnRule.getCompareCondition(), new BigDecimal(warnRule.getThreshold()));
        }
        //接通失败率
        if (warnRule.getBasicData().equals(7)) {
            double pickRate = BigDecimal.valueOf((float) pickUp / cdr).setScale(2, RoundingMode.HALF_UP).doubleValue();
            BigDecimal bDecimal = BigDecimal.valueOf(pickRate);
            double pickFailRate = 1 - bDecimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
            threshold = isThreshold(BigDecimal.valueOf(pickFailRate), warnRule.getCompareCondition(), new BigDecimal(warnRule.getThreshold()));
        }

        return threshold;
    }

    private boolean isThreshold(BigDecimal event, String compare, BigDecimal threshold) {
        if (">".equals(compare)) {
            return event.compareTo(threshold) > 0;
        } else if ("<".equals(compare)) {
            return event.compareTo(threshold) < 0;
        } else if ("=".equals(compare)) {
            return event.compareTo(threshold) == 0;
        } else if (">=".equals(compare)) {
            return event.compareTo(threshold) > -1;
        } else if ("<=".equals(compare)) {
            return event.compareTo(threshold) < 1;
        }
        return false;
    }

    public static String getTimeByPattern(long timestamp, String pattern) {
        if (StringUtils.isBlank(pattern)) {
            //默认格式："yyyy-MM-dd HH:mm:ss"
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        Date d = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(d);
    }

    /**
     * Object转BigDecimal类型
     * @param value 要转的object类型
     * @return 转成的BigDecimal类型数据
     */
    public static BigDecimal getBigDecimal(Object value) {
        BigDecimal ret = null;
        if (value != null) {
            if (value instanceof BigDecimal) {
                ret = (BigDecimal) value;
            } else if (value instanceof String) {
                ret = new BigDecimal((String) value);
            } else if (value instanceof BigInteger) {
                ret = new BigDecimal((BigInteger) value);
            } else if (value instanceof Number) {
                ret = BigDecimal.valueOf(((Number) value).doubleValue());
            } else {
                throw new ClassCastException("Not possible to coerce [" + value + "] from class " + value.getClass() + " into a BigDecimal.");
            }
        }
        return ret;
    }


}

