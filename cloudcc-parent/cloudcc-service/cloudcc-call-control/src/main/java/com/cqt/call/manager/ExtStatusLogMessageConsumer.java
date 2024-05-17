package com.cqt.call.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import com.cqt.base.contants.CommonConstant;
import com.cqt.base.contants.RocketMqConstant;
import com.cqt.call.mapper.ExtStatusLogMapper;
import com.cqt.model.cdr.entity.ExtStatusLog;
import com.cqt.mybatisplus.config.core.RequestDataHelper;
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
@RocketMQMessageListener(topic = RocketMqConstant.CLOUD_CC_EXT_LOG_TOPIC,
        consumerGroup = RocketMqConstant.EXT_LOG_GROUP)
public class ExtStatusLogMessageConsumer implements RocketMQListener<ExtStatusLog> {

    private final ExtStatusLogMapper extStatusLogMapper;

    private final Snowflake snowflake;

    @Override
    public void onMessage(ExtStatusLog extStatusLog) {
        try {
            String month = DateUtil.format(DateUtil.date(extStatusLog.getTargetTimestamp()), CommonConstant.MONTH_FORMAT);
            RequestDataHelper.setRequestData(CommonConstant.COMPANY_CODE_KEY, extStatusLog.getCompanyCode());
            RequestDataHelper.setRequestData(CommonConstant.MONTH_KEY, month);
            long logId = snowflake.nextId();
            extStatusLog.setLogId(logId);
            int insert = extStatusLogMapper.insert(extStatusLog);
            log.debug("[分机日志] logId: {}, 入库: {}", logId, insert);
        } catch (Exception e) {
            log.error("[分机日志] data: {}, 入库异常: ", extStatusLog, e);
        } finally {
            RequestDataHelper.remove();
        }
    }

}
