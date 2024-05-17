package com.cqt.sdk.client.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import com.cqt.base.contants.CommonConstant;
import com.cqt.base.contants.RocketMqConstant;
import com.cqt.model.cdr.entity.AgentStatusLog;
import com.cqt.mybatisplus.config.core.RequestDataHelper;
import com.cqt.sdk.client.mapper.AgentStatusLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * date:  2023-07-05 17:58
 * 分机状态迁移日志入库
 */
@Slf4j
@RequiredArgsConstructor
@Component
@RocketMQMessageListener(topic = RocketMqConstant.AGENT_STATUS_LOG_TOPIC,
        consumerGroup = RocketMqConstant.AGENT_LOG_GROUP)
public class AgentStatusLogMessageConsumer implements RocketMQListener<AgentStatusLog> {

    private final AgentStatusLogMapper agentStatusLogMapper;

    private final Snowflake snowflake;

    @Override
    public void onMessage(AgentStatusLog agentStatusLog) {
        try {
            String month = DateUtil.format(DateUtil.date(agentStatusLog.getTargetTimestamp()), CommonConstant.MONTH_FORMAT);
            RequestDataHelper.setRequestData(CommonConstant.COMPANY_CODE_KEY, agentStatusLog.getCompanyCode());
            RequestDataHelper.setRequestData(CommonConstant.MONTH_KEY, month);
            long logId = snowflake.nextId();
            agentStatusLog.setLogId(logId);
            int insert = agentStatusLogMapper.insert(agentStatusLog);
            log.debug("[坐席状态迁移日志] logId: {}, 入库: {}", logId, insert);
        } catch (Exception e) {
            log.error("[坐席状态迁移日志] data: {}, 入库异常: ", agentStatusLog, e);
        } finally {
            RequestDataHelper.remove();
        }
    }

}
