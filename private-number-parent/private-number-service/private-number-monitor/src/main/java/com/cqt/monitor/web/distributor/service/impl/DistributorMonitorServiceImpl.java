package com.cqt.monitor.web.distributor.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.common.future.AsyncTask;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.monitor.cache.SbcDistributorMonitorConfigCache;
import com.cqt.monitor.config.TestRabbitmqConfig;
import com.cqt.monitor.web.distributor.SbcDistributorMonitorConfig;
import com.cqt.monitor.web.distributor.event.DingtalkAlarmEvent;
import com.cqt.monitor.web.distributor.event.RecordFailNodeEvent;
import com.cqt.monitor.web.distributor.model.dto.UpdateDisConfigDTO;
import com.cqt.monitor.web.distributor.model.vo.UpdateDisConfigVO;
import com.cqt.monitor.web.distributor.service.DistributorMonitorService;
import com.cqt.redis.util.RedissonUtil;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * @since 2022-12-02 10:14
 * 中间号 监控切换dis组
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DistributorMonitorServiceImpl implements DistributorMonitorService {

    private final RabbitTemplate rabbitTemplate;

    private final ThreadPoolTaskExecutor disMonitorExecutor;

    private final RedissonUtil redissonUtil;

    @Override
    public void startMonitor() {
        SbcDistributorMonitorConfig config = SbcDistributorMonitorConfigCache.get();
        // 监控检查
        boolean passHealthCheck = passHealthCheck(config.getHealthUrl());
        // mq检测
        boolean checkRabbitmq = checkRabbitmq();
        if (passHealthCheck && checkRabbitmq) {
            String disConnectNodeKey = PrivateCacheUtil.getDisConnectNodeKey(SbcDistributorMonitorConfigCache.getServerIp());
            boolean exist = redissonUtil.isExistString(disConnectNodeKey);
            if (exist) {
                // 恢复开关
                if (config.getEnableRecover()) {
                    doDeal(config, config.getNormalWeight());
                }
                SpringUtil.publishEvent(new RecordFailNodeEvent(this, OperateTypeEnum.DELETE));
            }
            return;
        }

        doDeal(config, config.getErrWeight());
    }

    /**
     * 处理dis组 更新权重
     */
    private void doDeal(SbcDistributorMonitorConfig config, String newWeight) {
        SbcDistributorMonitorConfig.DistributorConfig operator = config.getOperator();
        SbcDistributorMonitorConfig.DistributorConfig customer = config.getCustomer();

        // 开启切换SBC dis组权重
        if (operator.getEnable()) {
            dealDistributor(operator, newWeight);
        }
        if (customer.getEnable()) {
            dealDistributor(customer, newWeight);
        }

    }

    /**
     * 处理客户或运营商SBC dis组配置
     *
     * @param distributorConfig dis组配置
     */
    private void dealDistributor(SbcDistributorMonitorConfig.DistributorConfig distributorConfig, String newWeight) {

        List<UpdateDisConfigVO> updateDisConfigVOList = updateDisConfig(distributorConfig, newWeight);
        alarm(updateDisConfigVOList);
    }

    /**
     * 告警
     */
    private void alarm(List<UpdateDisConfigVO> updateDisConfigVoList) {
        // 钉钉告警
        // 要告警的消息
        List<UpdateDisConfigVO> list = updateDisConfigVoList.stream()
                .filter(UpdateDisConfigVO::getAlarm)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(list)) {
            return;
        }

        List<UpdateDisConfigVO> mergeList = new ArrayList<>();
        for (UpdateDisConfigVO updateDisConfigVO : list) {
            if (!updateDisConfigVO.getMergeAlarm()) {
                SpringUtil.publishEvent(new DingtalkAlarmEvent(this, updateDisConfigVO.getMessage()));
                continue;
            }
            mergeList.add(updateDisConfigVO);
        }

        // 合并消息
        if (CollUtil.isEmpty(mergeList)) {
            return;
        }
        String message = mergeList.stream().filter(UpdateDisConfigVO::getMergeAlarm)
                .map(UpdateDisConfigVO::getMessage)
                .collect(Collectors.joining(StrUtil.LF));
        SpringUtil.publishEvent(new DingtalkAlarmEvent(this, message));
    }

    /**
     * 检查健康检测接口是否正常
     *
     * @param healthUrl 健康检测接口列表
     * @return 结果列表
     */
    private Boolean passHealthCheck(List<String> healthUrl) {

        AsyncTask<Boolean, String> asyncTask = new AsyncTask<>();
        CheckHealthUrlTaskLoaderImpl healthUrlTaskLoader = new CheckHealthUrlTaskLoaderImpl();
        List<Boolean> resultList = asyncTask.sendAsyncBatch(healthUrl, healthUrlTaskLoader, disMonitorExecutor);

        Optional<Boolean> booleanOptional = resultList.stream().filter(Boolean.FALSE::equals).findFirst();
        // 存在false, 说明有接口调用失败
        return !booleanOptional.isPresent();
    }

    /**
     * mq 发送消息是否成功
     */
    private Boolean checkRabbitmq() {

        try {
            rabbitTemplate.convertAndSend(TestRabbitmqConfig.HEALTH_EXCHANGE, TestRabbitmqConfig.HEALTH_QUEUE, DateUtil.date());
        } catch (AmqpException e) {
            log.error("mq消息测试, 发送失败: ", e);
            return false;
        }
        return true;
    }

    /**
     * 修改SBC dis组配置并重新加载
     *
     * @param distributorConfig dis组配置信息
     * @return 结果
     */
    private List<UpdateDisConfigVO> updateDisConfig(SbcDistributorMonitorConfig.DistributorConfig distributorConfig, String newWeight) {
        // 当前设备ip
        String currentIp = SbcDistributorMonitorConfigCache.getServerIp();
        List<UpdateDisConfigDTO> updateDisConfigDTOList = new ArrayList<>();
        List<String> serverList = distributorConfig.getServer();
        String disListName = distributorConfig.getDisListName();
        Map<String, List<String>> nodeInfo = distributorConfig.getNodeInfo();
        List<String> nodeNameList = nodeInfo.get(currentIp);
        if (CollUtil.isEmpty(nodeNameList)) {
            log.warn("当前设备IP: {}, 没有在nacos配置中找到对应的节点", SbcDistributorMonitorConfigCache.getServerIp());
            return Lists.newArrayList();
        }
        String distributorConfigPath = distributorConfig.getDistributorConfigPath();
        serverList.forEach(serverIp -> {
            updateDisConfigDTOList.add(UpdateDisConfigDTO.init(serverIp, nodeNameList, distributorConfigPath, disListName, newWeight));
        });

        AsyncTask<UpdateDisConfigVO, UpdateDisConfigDTO> asyncTask = new AsyncTask<>();
        UpdateDisConfigTaskLoaderImpl updateDisConfigTaskLoader = new UpdateDisConfigTaskLoaderImpl();

        return asyncTask.sendAsyncBatch(updateDisConfigDTOList, updateDisConfigTaskLoader, disMonitorExecutor);
    }

}
