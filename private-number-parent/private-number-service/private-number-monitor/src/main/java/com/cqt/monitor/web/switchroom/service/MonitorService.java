package com.cqt.monitor.web.switchroom.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqt.common.enums.ExecuteTypeEnum;
import com.cqt.model.monitor.entity.MonitorConfigInfo;
import com.cqt.model.monitor.entity.MonitorExecuteInfo;
import com.cqt.model.monitor.entity.MonitorPlatformInfo;
import com.cqt.model.monitor.entity.MonitorToggleInfo;
import com.cqt.model.monitor.properties.MonitorProperties;
import com.cqt.monitor.cache.MonitorConfigCache;
import com.cqt.monitor.common.util.DingUtil;
import com.cqt.monitor.web.switchroom.mapper.MonitorExecuteInfoMapper;
import com.cqt.monitor.web.switchroom.mapper.MonitorPlatformInfoMapper;
import com.cqt.monitor.web.switchroom.mapper.MonitorToggleInfoMapper;
import com.cqt.monitor.web.switchroom.redis.RedisClient;
import com.cqt.monitor.web.switchroom.redis.RedisNode;
import com.cqt.monitor.web.switchroom.redis.RedisUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * @author hlx
 * @date 2022-01-21
 */
@Service
@Slf4j
public class MonitorService {

    @Autowired
    private MonitorPlatformInfoMapper platformInfoMapper;

    @Autowired
    private MonitorProperties monitorProperties;

    @Autowired
    private MonitorToggleInfoMapper monitorToggleInfoMapper;

    @Autowired
    private DingUtil dingUtil;

    @Autowired
    private MonitorExecuteInfoMapper executeInfoMapper;







    /**
     * 缓存 rabbitmq 连接 Connection
     */
    private final static Map<String, Connection> CONNECTION_MAP = new ConcurrentHashMap<>(16);

    /**
     * mq检测client方式
     *
     * @return 是否正常
     */
    private boolean rabbitClientCheck(String platform, MonitorConfigInfo rabbitmqConfig) {
        if (!CONNECTION_MAP.containsKey(platform)) {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(rabbitmqConfig.getHost());
            connectionFactory.setPort(rabbitmqConfig.getPort());
            connectionFactory.setVirtualHost("fsbc");
            connectionFactory.setUsername(rabbitmqConfig.getUsername());
            connectionFactory.setPassword(rabbitmqConfig.getPassword());
            log.info("rabbitmq host: "+rabbitmqConfig.getHost() + "rabbitmq port: "+rabbitmqConfig.getPort());
            try {
                Connection connection = connectionFactory.newConnection();
                CONNECTION_MAP.put(platform, connection);
            } catch (Exception e) {
                log.error("创建业务mq连接失败!");
                e.printStackTrace();
            }
        }
        Connection connection = CONNECTION_MAP.get(platform);

        Channel channel = null;
        try {
            channel = connection.createChannel();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("client{}rabbitmq异常，异常=>", platform, e);
            toggle(platform, "rabbitmq 异常",null);
            return false;
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * mq检测url方式
     *
     * @return 是否正常
     */
    private boolean rabbitUrlCheck(String platform, MonitorConfigInfo rabbitmqConfig) {
        int errCount = 0;
        // 3次请求 全部异常才切换
        for (int i = 0; i < 3; i++) {
            try (HttpResponse httpResponse = HttpRequest.get(rabbitmqConfig.getUrl())
                    .timeout(monitorProperties.getTimeout())
                    .basicAuth(rabbitmqConfig.getUsername(), rabbitmqConfig.getPassword())
                    .execute()) {
                if (!"{\"status\":\"ok\"}".equals(httpResponse.body())) {
                    throw new RuntimeException(httpResponse.body());
                }
            } catch (Exception e) {
                errCount++;
                log.info("url{}rabbitmq异常，第{}次异常，异常=>", platform, errCount, e);
            }
        }
        if (errCount == 3) {
            toggle(platform, "rabbitmq 异常",null);
            return false;
        }
        return true;
    }

    /**
     * ng检测url方式
     *
     * @return 是否正常
     */
    private boolean nginxUrlCheck(String platform, List<MonitorConfigInfo> ngConfigs) {
        for (MonitorConfigInfo ngConfig : ngConfigs) {
            int errCount = 0;
            // 3次请求 全部异常才切换
            for (int i = 0; i < 3; i++) {
                try (HttpResponse httpResponse = HttpRequest.get(ngConfig.getUrl())
                        .timeout(monitorProperties.getTimeout())
                        .execute()) {
                    if (!"ok".equals(httpResponse.body())) {
                        errCount++;
                        log.info("url{}nginx接口返回体异常，第{}次异常，异常=>{}", platform, errCount, httpResponse.body());
                    }
                } catch (Exception e) {
                    errCount++;
                    log.info("url{}nginx异常，第{}次异常，异常=>", platform, errCount, e);
                }
            }
            if (errCount == 3) {
                toggle(platform, "nginx 异常",null);
                return false;
            }
        }
        return true;
    }

    /**
     * 检测开始  redis => rabbitmq => msrn
     *
     * @param platform 平台名
     */
    @Async("threadPool")
    public void checkStart(String platform) {
        List<MonitorConfigInfo> redisConfigList = MonitorConfigCache.REDIS_CONFIG.get(platform);
        if (redisConfigList != null && redisConfigList.size() > 0) {
            long start = System.currentTimeMillis();
            try {
                RedisClient redisClient = RedisUtil.buildByConfigs(redisConfigList);
                List<RedisNode> nodes = redisClient.clusterNodes();
                for (RedisNode node : nodes) {
                    if (!node.getRunStatus() && node.getFlags().contains("master")) {
                        throw new RuntimeException(platform + "存在主节点down的情况");
                    }
                }
                log.info("{}redis检查正常，用时{}毫秒", platform, System.currentTimeMillis() - start);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("{}redis异常，开启准备执行切换，异常=>", platform, e);
                toggle(platform, "redis 异常",null);
                return;
            }
        }

        List<MonitorConfigInfo> monitorConfigInfos = MonitorConfigCache.NG_CONFIG.get(platform);
        if (monitorConfigInfos != null && monitorConfigInfos.size() > 0) {
            long start = System.currentTimeMillis();
            try {
                boolean b = nginxUrlCheck(platform, monitorConfigInfos);
                if (!b) {
                    return;
                }
                log.info("{}nginx检查正常，用时{}毫秒", platform, System.currentTimeMillis() - start);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("{}nginx异常，开启准备执行切换，异常=>", platform, e);
                toggle(platform, "nginx 异常",null);
                return;
            }
        }

        MonitorConfigInfo rabbitmqConfig = MonitorConfigCache.RABBITMQ_CONFIG.get(platform);
        if (rabbitmqConfig != null) {
            long start = System.currentTimeMillis();
            boolean bool = monitorProperties.getMqCheckType() == 0 ?
                    rabbitUrlCheck(platform, rabbitmqConfig) : rabbitClientCheck(platform, rabbitmqConfig);
            if (!bool) {
                return;
            }
            log.info("{}rabbitmq检查正常，用时{}毫秒", platform, System.currentTimeMillis() - start);
        }
        List<MonitorConfigInfo> msrnConfigs = MonitorConfigCache.MSRN_CONFIG.get(platform);
        List<String> gtList = new ArrayList<>();
        if (msrnConfigs == null){
            recover(platform,null);
            return;
        }
        for (MonitorConfigInfo msrnConfig : msrnConfigs) {
            long start = System.currentTimeMillis();
            String url = msrnConfig.getUrl();
            try (HttpResponse httpResponse = HttpRequest.get(url)
                    .timeout(10000)
                    .execute()) {
                log.info("msrn URL: "+msrnConfig.getUrl());
                String number = httpResponse.body();
                log.info("漫游号数量为：" + number);
                int num = Integer.parseInt(number);
                QueryWrapper<MonitorPlatformInfo> wrapper = new QueryWrapper<>();
                String[] split = url.split("=");
                String gt = split[split.length-1];
                wrapper.eq("platform", platform).eq("gt_code",gt).last("limit 1");
                MonitorPlatformInfo platformInfo = platformInfoMapper.selectOne(wrapper);
                QueryWrapper<MonitorPlatformInfo> wrapper1 = new QueryWrapper<>();
                wrapper1.eq("platform", platform).eq("gt_code",gt);
                List<MonitorPlatformInfo> monitorPlatformInfos = platformInfoMapper.selectList(wrapper1);
                if (num == 0) {
                    platformInfo.setErrorNum(platformInfo.getErrorNum() + 1);
                    log.info("{}msrn漫游号异常，漫游号数量为0", platform);

                    for (MonitorPlatformInfo monitorPlatformInfo : monitorPlatformInfos) {
                        monitorPlatformInfo.setErrorNum(platformInfo.getErrorNum() + 1);
                        platformInfoMapper.updateById(monitorPlatformInfo);

                    }
//                    platformInfoMapper.update(platformInfo, wrapper);
                    if (platformInfo.getErrorNum() >= monitorProperties.getMsrnNum()) {
                        toggle(platform, "msrn漫游号为 0",gt);
                    } else {
                        dingUtil.sendMessage("url:" + msrnConfig.getUrl() + "\n"+platform + "漫游号数量异常次数\n" + platformInfo.getErrorNum());
                    }
                    continue;
                }
                gtList.add(gt);
                for (MonitorPlatformInfo monitorPlatformInfo : monitorPlatformInfos) {
                    monitorPlatformInfo.setErrorNum(0);
                    platformInfoMapper.updateById(monitorPlatformInfo);

                }
//                platformInfo.setErrorNum(0);
//                platformInfoMapper.update(platformInfo, wrapper);
                log.info("{}msrn漫游号检查正常，用时{}毫秒", platform, System.currentTimeMillis() - start);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("{}msrn漫游号异常，钉钉告警，异常=>", platform, e);
                dingUtil.sendMessage("url:" + msrnConfig.getUrl() + "\n"
                        + platform + "漫游号检查异常=>\n" + e.getMessage() +
                        "\n该异常不会导致切换，尽快检查是否为网络问题");
                return;
            }
        }
        if (monitorProperties.getAutoRecover()) {
            for (String s : gtList) {
                recover(platform,s);
            }
        }

    }

    /**
     * 执行切换
     */
    public void toggle(String platform, String errorMsg,String gtCode) {

        // 遍历需要执行脚本的设备
        QueryWrapper<MonitorExecuteInfo> executeWrapper = new QueryWrapper<>();
        executeWrapper.eq("platform", platform)
                .eq("type", ExecuteTypeEnum.TOGGLE.name());
        if (StringUtils.isNotEmpty(gtCode)){
            executeWrapper.eq("gt_code",gtCode);
        }
        List<MonitorExecuteInfo> executeInfos = executeInfoMapper.selectList(executeWrapper);
        List<MonitorToggleInfo> executeResults = new ArrayList<>();
        int cout = 0;
        for (MonitorExecuteInfo executeInfo : executeInfos) {
            cout++;
            QueryWrapper<MonitorPlatformInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("platform", platform).eq("gt_code",executeInfo.getGtCode()).eq("execute_url",executeInfo.getExecuteUrl());
            MonitorPlatformInfo platformInfo = platformInfoMapper.selectOne(wrapper);
            platformInfo.setMessage(errorMsg);
            if (platformInfo.getType().equals(0)) {
                log.info("{}已切换 无需动作,gt码：{}", platform,executeInfo.getGtCode());
                dingUtil.sendMessage(platform + " gt:"+executeInfo.getGtCode()+ "\n" +"url:"+executeInfo.getExecuteUrl()+  "\n"+"errorMsg: " + errorMsg + "\n" + platform + "已切换，请尽快恢复");
                platformInfoMapper.update(platformInfo, wrapper);
                continue;
            }
            MonitorToggleInfo info = new MonitorToggleInfo();
            info.setExecuteUrl(executeInfo.getExecuteUrl());
            info.setType(ExecuteTypeEnum.TOGGLE.name());
            info.setExecuteTime(new Date());
            info.setPlatform(platform);
            info.setReason(errorMsg);
            info.setGtCode(executeInfo.getGtCode());
            for (int i = 0; i < monitorProperties.getRetryNum() ; i++) {
                try (HttpResponse httpResponse = HttpRequest.post(executeInfo.getExecuteUrl())
                        .timeout(10000)
                        .contentType("application/json")
                        .body(monitorProperties.getSwitchMaster())
                        .execute()) {
                    String body = httpResponse.body();
                    if (StringUtils.isEmpty(body)){
                        log.info("当前不是master，无法执行切换");
                        dingUtil.sendMessage(platform + " gt:"+executeInfo.getGtCode()+"\n"+"url:"+executeInfo.getExecuteUrl()+ "\n"+"errorMsg: "+  errorMsg + "\n" + platform + "当前不是master，无法执行切换");
                        platformInfo.setType(0);
                        platformInfoMapper.update(platformInfo, wrapper);
                        break;
                    }
                } catch (Exception e) {
                    log.error("查询master进程失败");
                    log.error(e.getMessage());
                    continue;
                }

                try (HttpResponse httpResponse = HttpRequest.post(executeInfo.getExecuteUrl())
                        .timeout(monitorProperties.getTimeout())
                        .contentType("application/json")
                        .body(executeInfo.getBash())
                        .execute()) {
                    String body = httpResponse.body();
                    if (!body.contains("start ok")) {
                        log.info("执行切换脚本异常：" + body);
                        info.setMessage("执行切换脚本异常");
                        continue;
                    }
                    info.setResult(1);
                    info.setMessage("执行成功");
                    log.info("切换脚本执行成功！=>{}  脚本为=>{}", executeInfo.getExecuteUrl(), executeInfo.getBash());
                    // 状态改为 0 已切换
                    platformInfo.setType(0);
                    platformInfoMapper.update(platformInfo, wrapper);
                    break;
                } catch (Exception e) {
                    log.error("切换脚本执行失败！url:{} 脚本为=>{} 异常为=>",
                            executeInfo.getExecuteUrl(), executeInfo.getBash(), e);
                    info.setResult(0);
                    info.setMessage(e.getMessage());
                }
            }

            monitorToggleInfoMapper.insert(info);
            executeResults.add(info);
            if (cout==executeInfos.size()){

                // 推送钉钉
                dingUtil.sendMessage(platform + "业务侧" + errorMsg + "\n" + platform +
                        "业务侧请求已经全部切换至另一机房\n详情:\n" + JSON.toJSONString(executeResults));
            }

        }

    }

    /**
     * 执行恢复
     */
    public void recover(String platform, String gtCode) {

        // 遍历需要执行脚本的设备
        QueryWrapper<MonitorExecuteInfo> executeWrapper = new QueryWrapper<>();
        executeWrapper.eq("platform", platform)
                .eq("type", ExecuteTypeEnum.RECOVER.name());
        if (StringUtils.isNotEmpty(gtCode)){
            executeWrapper.eq("gt_code",gtCode);
        }
        List<MonitorExecuteInfo> executeInfos = executeInfoMapper.selectList(executeWrapper);
        List<MonitorToggleInfo> executeResults = new ArrayList<>();
        int cout = 0;
        for (MonitorExecuteInfo executeInfo : executeInfos) {
            cout++;
            QueryWrapper<MonitorPlatformInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("platform", platform).eq("gt_code",executeInfo.getGtCode()).eq("execute_url",executeInfo.getExecuteUrl());
            MonitorPlatformInfo platformInfo = platformInfoMapper.selectOne(wrapper);
            if (platformInfo.getType().equals(1)) {
                //平台正常 无需动作
                continue;
            }
            MonitorToggleInfo info = new MonitorToggleInfo();
            info.setExecuteUrl(executeInfo.getExecuteUrl());
            info.setType(ExecuteTypeEnum.RECOVER.name());
            info.setExecuteTime(new Date());
            info.setPlatform(platform);
            info.setGtCode(executeInfo.getGtCode());
            info.setReason("redis rabbitmq 漫游号均恢复正常");
            for (int i = 0; i < monitorProperties.getRetryNum() ; i++) {
                try (HttpResponse httpResponse = HttpRequest.post(executeInfo.getExecuteUrl())
                        .timeout(10000)
                        .contentType("application/json")
                        .body(monitorProperties.getSwitchSlave())
                        .execute()) {
                    String body = httpResponse.body();
                    if (StringUtils.isEmpty(body)){
                        log.info("当前不是slave，无法执行恢复");
                        dingUtil.sendMessage(platform + " gt:"+executeInfo.getGtCode()+" url:"+executeInfo.getExecuteUrl()+ "\n" + platform + "当前不是slave，无法执行恢复");
                        platformInfo.setType(1);
                        platformInfo.setMessage("正常");
                        platformInfoMapper.update(platformInfo, wrapper);
                        break;
                    }
                } catch (Exception e) {
                    log.error("查询slave进程失败");
                    log.error(e.getMessage());
                    continue;
                }


                try (HttpResponse httpResponse = HttpRequest.post(executeInfo.getExecuteUrl())
                        .body(executeInfo.getBash())
                        .contentType("application/json")
                        .timeout(monitorProperties.getTimeout())
                        .execute()) {
                    String body = httpResponse.body();
                    if (!body.contains("start ok")) {
                        log.info("执行恢复脚本异常：" + body);
                        info.setMessage("执行恢复脚本异常");
                        continue;
                    }
                    info.setResult(1);
                    info.setMessage("执行成功");
                    log.info("恢复脚本执行成功！=>{} 脚本为=>{}", executeInfo.getExecuteUrl(), executeInfo.getBash());
                    // 状态改为 1 已恢复
                    platformInfo.setType(1);
                    platformInfo.setMessage("正常");
                    platformInfoMapper.update(platformInfo, wrapper);
                    break;
                } catch (Exception e) {
                    log.error("恢复脚本执行失败！url:{} 脚本为=>{} 异常为=>", executeInfo.getExecuteUrl(), executeInfo.getBash(), e);
                    info.setResult(0);
                    info.setMessage(e.getMessage());
                }
            }

            monitorToggleInfoMapper.insert(info);
            executeResults.add(info);
            if (cout==executeInfos.size()){
                // 推送钉钉
                dingUtil.sendMessage(platform + "执行恢复，以下为脚本执行结果!\n" + JSON.toJSONString(executeResults));
            }

        }

    }


    public void clearRabbitCacheMap() {
        // 释放不需要的 connection
        Map<String, MonitorConfigInfo> rabbitmqConfigs = MonitorConfigCache.RABBITMQ_CONFIG;
        Set<String> currentSet = CONNECTION_MAP.keySet();
        for (String key : currentSet) {
//            if (!rabbitmqConfigs.containsKey(key)) {
            Connection connection = CONNECTION_MAP.get(key);
            CONNECTION_MAP.remove(key);
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    log.error("rabbitmq connection close exception");
                    e.printStackTrace();
                }
            }
//            }
        }
    }


    public void toggleByIp(String id) {

        MonitorPlatformInfo platformInfo = platformInfoMapper.selectById(id);
        QueryWrapper<MonitorExecuteInfo> wrapper = new QueryWrapper<>();
        String platform = platformInfo.getPlatform();
        wrapper.eq("platform", platform).eq("gt_code", platformInfo.getGtCode())
                .eq("execute_url", platformInfo.getExecuteUrl()).eq("type", ExecuteTypeEnum.TOGGLE.name());
        MonitorExecuteInfo executeInfo = executeInfoMapper.selectOne(wrapper);
        String errorMsg = "手动执行切换";
        platformInfo.setMessage(errorMsg);
        if (platformInfo.getType().equals(0)) {
            log.info("{}已切换 无需动作,gt码：{}", platform, executeInfo.getGtCode());
            dingUtil.sendMessage(platform + " gt:" + executeInfo.getGtCode() + "\n" + "url:" + executeInfo.getExecuteUrl() + "\n" + "errorMsg: " + errorMsg + "\n" + platform + "已切换，请尽快恢复");
            platformInfoMapper.updateById(platformInfo);

        }
        MonitorToggleInfo info = new MonitorToggleInfo();
        info.setExecuteUrl(executeInfo.getExecuteUrl());
        info.setType(ExecuteTypeEnum.TOGGLE.name());
        info.setExecuteTime(new Date());
        info.setPlatform(platform);
        info.setReason(errorMsg);
        info.setGtCode(executeInfo.getGtCode());
        for (int i = 0; i < monitorProperties.getRetryNum(); i++) {
            try (HttpResponse httpResponse = HttpRequest.post(executeInfo.getExecuteUrl())
                    .timeout(10000)
                    .contentType("application/json")
                    .body(monitorProperties.getSwitchMaster())
                    .execute()) {
                String body = httpResponse.body();
                if (StringUtils.isEmpty(body)) {
                    log.info("当前不是master，无法执行切换");
                    dingUtil.sendMessage(platform + " gt:" + executeInfo.getGtCode() + "\n" + "url:" + executeInfo.getExecuteUrl() + "\n" + "errorMsg: " + errorMsg + "\n" + platform + "当前不是master，无法执行切换");
                    platformInfo.setType(0);
                    platformInfoMapper.updateById(platformInfo);
                    break;
                }
            } catch (Exception e) {
                log.error("查询master进程失败");
                log.error(e.getMessage());
                continue;
            }

            try (HttpResponse httpResponse = HttpRequest.post(executeInfo.getExecuteUrl())
                    .timeout(monitorProperties.getTimeout())
                    .contentType("application/json")
                    .body(executeInfo.getBash())
                    .execute()) {
                String body = httpResponse.body();
                if (!body.contains("start ok")) {
                    log.info("执行切换脚本异常：" + body);
                    info.setMessage("执行切换脚本异常");
                    continue;
                }
                info.setResult(1);
                info.setMessage("执行成功");
                log.info("切换脚本执行成功！=>{}  脚本为=>{}", executeInfo.getExecuteUrl(), executeInfo.getBash());
                // 状态改为 0 已切换
                platformInfo.setType(0);
                platformInfoMapper.updateById(platformInfo);
                break;
            } catch (Exception e) {
                log.error("切换脚本执行失败！url:{} 脚本为=>{} 异常为=>",
                        executeInfo.getExecuteUrl(), executeInfo.getBash(), e);
                info.setResult(0);
                info.setMessage(e.getMessage());
            }
        }

        monitorToggleInfoMapper.insert(info);

        // 推送钉钉
        dingUtil.sendMessage(platform + "业务侧" + errorMsg + "\n" + platform +
                "手动执行切换操作\n详情:\n" + JSON.toJSONString(info));

    }

    public void recoverByIp(String id) {
        MonitorPlatformInfo platformInfo = platformInfoMapper.selectById(id);
        QueryWrapper<MonitorExecuteInfo> wrapper = new QueryWrapper<>();
        String platform = platformInfo.getPlatform();
        wrapper.eq("platform", platform).eq("gt_code", platformInfo.getGtCode())
                .eq("execute_url", platformInfo.getExecuteUrl()).eq("type", ExecuteTypeEnum.RECOVER.name());
        MonitorExecuteInfo executeInfo = executeInfoMapper.selectOne(wrapper);
        String errorMsg = "手动执行切换";
        platformInfo.setMessage(errorMsg);

        if (platformInfo.getType().equals(1)) {
            //平台正常 无需动作
            return;
        }
        MonitorToggleInfo info = new MonitorToggleInfo();
        info.setExecuteUrl(executeInfo.getExecuteUrl());
        info.setType(ExecuteTypeEnum.RECOVER.name());
        info.setExecuteTime(new Date());
        info.setPlatform(platform);
        info.setGtCode(executeInfo.getGtCode());
        info.setReason("手动执行恢复");
        for (int i = 0; i < monitorProperties.getRetryNum(); i++) {
            try (HttpResponse httpResponse = HttpRequest.post(executeInfo.getExecuteUrl())
                    .timeout(10000)
                    .contentType("application/json")
                    .body(monitorProperties.getSwitchSlave())
                    .execute()) {
                String body = httpResponse.body();
                if (StringUtils.isEmpty(body)) {
                    log.info("当前不是slave，无法执行恢复");
                    dingUtil.sendMessage(platform + " gt:" + executeInfo.getGtCode() + " url:" + executeInfo.getExecuteUrl() + "\n" + platform + "当前不是slave，无法执行恢复");
                    platformInfo.setType(1);
                    platformInfo.setMessage("正常");
                    platformInfoMapper.updateById(platformInfo);
                    break;
                }
            } catch (Exception e) {
                log.error("查询slave进程失败");
                log.error(e.getMessage());
                continue;
            }


            try (HttpResponse httpResponse = HttpRequest.post(executeInfo.getExecuteUrl())
                    .body(executeInfo.getBash())
                    .contentType("application/json")
                    .timeout(monitorProperties.getTimeout())
                    .execute()) {
                String body = httpResponse.body();
                if (!body.contains("start ok")) {
                    log.info("执行恢复脚本异常：" + body);
                    info.setMessage("执行恢复脚本异常");
                    continue;
                }
                info.setResult(1);
                info.setMessage("执行成功");
                log.info("恢复脚本执行成功！=>{} 脚本为=>{}", executeInfo.getExecuteUrl(), executeInfo.getBash());
                // 状态改为 1 已恢复
                platformInfo.setType(1);
                platformInfo.setMessage("正常");
                platformInfoMapper.updateById(platformInfo);
                break;
            } catch (Exception e) {
                log.error("恢复脚本执行失败！url:{} 脚本为=>{} 异常为=>", executeInfo.getExecuteUrl(), executeInfo.getBash(), e);
                info.setResult(0);
                info.setMessage(e.getMessage());
            }
        }

        monitorToggleInfoMapper.insert(info);

        // 推送钉钉
        dingUtil.sendMessage(platform + "执行恢复，以下为脚本执行结果!\n" + JSON.toJSONString(info));


    }
}
