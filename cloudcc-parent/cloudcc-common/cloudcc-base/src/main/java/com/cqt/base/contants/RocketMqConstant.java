package com.cqt.base.contants;

/**
 * @author linshiqiang
 * date:  2023-07-05 16:03
 * RocketMQ 主题名称常量
 */
public interface RocketMqConstant {

    String APP_NAME = "${spring.application.name}";

    /**
     * 分机状态迁移日志
     */
    String CLOUD_CC_EXT_LOG_TOPIC = "cloudcc_ext_log_topic";

    String EXT_LOG_GROUP = "extLog";

    /**
     * 坐席状态迁移日志
     */
    String AGENT_STATUS_LOG_TOPIC = "cloudcc_agent_log_topic";

    String AGENT_LOG_GROUP = "agentLog";

    /**
     * 话单入库的主题
     */
    String CDR_STORE_TOPIC = "cloudcc_cdr_store_topic";

    String CDR_INSIDE_TOPIC = "cloudcc_cdr_inside_topic";

    String CDR_OUTSIDE_TOPIC = "cloudcc_cdr_outside_topic";

    /**
     * 外呼任务结果回调
     */
    String CLOUD_CC_OUTBOUND_CALL_TASK = "cloudcc_outbound_call_task";

    String OUTBOUND_CALL_TASK_DB_TAG = "db";

    String CLOUD_CC_EXT_STATUS_TOPIC = "cloudcc_ext_status";

    String EXT_STATUS_GROUP = "cloudcc_ext_status";

}
