package com.cqt.hmyc.web.bind.service.recycle;

import cn.hutool.core.util.StrUtil;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.hmyc.config.rabbitmq.RabbitMqConfig;
import com.cqt.hmyc.web.bind.manager.PrivateMqProducer;
import com.cqt.model.bind.bo.MqBindInfoBO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.common.properties.HideProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2021/9/26 12:08
 */
@Service
@Slf4j
public class SaveBindService {

    private final HideProperties hideProperties;

    @Resource(name = "saveExecutor")
    private ThreadPoolTaskExecutor saveExecutor;

    private final NumberRecycleService numberRecycleService;

    private final PrivateMqProducer mqProducer;

    public SaveBindService(HideProperties hideProperties, NumberRecycleService numberRecycleService, PrivateMqProducer mqProducer) {
        this.hideProperties = hideProperties;
        this.numberRecycleService = numberRecycleService;
        this.mqProducer = mqProducer;
    }

    public void saveBind(Optional<BindRecycleDTO> bindRecycleDtoOptional, Integer expiration, MqBindInfoBO mqBindInfoBO) {

        saveExecutor.execute(() -> {
            try {
                if (bindRecycleDtoOptional.isPresent()) {
                    BindRecycleDTO bindRecycleDTO = bindRecycleDtoOptional.get();
                    String numType = bindRecycleDTO.getNumType().toLowerCase();
                    // 发生到号码回收延时队列
                    try {
                        mqProducer.sendLazy(bindRecycleDTO, expiration,
                                String.format(RabbitMqConfig.BIND_RECYCLE_EXCHANGE, numType),
                                String.format(RabbitMqConfig.BIND_RECYCLE_DELAY_QUEUE, numType),
                                bindRecycleDTO.getNumType());
                    } catch (Exception e) {
                        log.error("data: {}, 发送延时队列异常: ", mqBindInfoBO, e);
                    }
                }

                if (hideProperties.getSwitchs().getSaveDb()) {
                    if (hideProperties.getSwitchs().getBindInfoAsyncSaveDb()) {
                        try {
                            numberRecycleService.saveBindInfo(mqBindInfoBO);
                        } catch (Exception e) {
                            log.error("data: {}, 绑定关系入库异常: ", mqBindInfoBO, e);
                        }
                        return;
                    }
                    // 数据库操作发送到mq
                    String routingKey = getRoutingKey(mqBindInfoBO.getOperateType());
                    if (StrUtil.isNotEmpty(routingKey)) {
                        mqProducer.sendMessage(RabbitMqConfig.BIND_DB_EXCHANGE, routingKey, mqBindInfoBO);
                    }
                }
            } catch (Exception e) {
                log.error("推mq异常: ", e);
            }
        });
    }

    public String getRoutingKey(String operateType) {
        OperateTypeEnum operateTypeEnum = OperateTypeEnum.valueOf(operateType);
        switch (operateTypeEnum) {
            case INSERT:
                return RabbitMqConfig.BIND_DB_INSERT_QUEUE;
            case UPDATE:
                return RabbitMqConfig.BIND_DB_UPDATE_QUEUE;
            case DELETE:
                return RabbitMqConfig.BIND_DB_DELETE_QUEUE;
            default:
                return "";
        }
    }

}
