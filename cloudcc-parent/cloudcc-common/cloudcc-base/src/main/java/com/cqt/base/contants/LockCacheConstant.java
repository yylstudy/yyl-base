package com.cqt.base.contants;

/**
 * @author linshiqiang
 * date:  2023-07-20 15:57
 * 分布式锁相关缓存键定义
 */
public interface LockCacheConstant {

    /**
     * 排队定时任务执行锁
     * 同时只有一台设备执行排队逻辑
     */
    String QUEUE_SCHEDULE_LOCK_KEY = CacheConstant.PREFIX + "queueScheduleLock";

    /**
     * 排队 按企业id加锁
     * userQueueLock:{company_code}
     */
    String USER_QUEUE_LOCK_KEY = CacheConstant.PREFIX + "userQueueLock:{}";

    /**
     * 事件消息消费, 按uuid加锁
     * eventConsumerLock:{uuid}
     * ttl 60s
     */
    String EVENT_MESSAGE_CONSUMER_LOCK_KEY = CacheConstant.PREFIX + "eventConsumerLock:{}";

    /**
     * 三方通话剩余通话人数 lock
     * key: threeWayRemain:{companyCode}:{callId}
     * value: 挂断数量
     */
    String THREE_WAY_REMAIN_CALL_LOCK_KEY = CacheConstant.PREFIX + "threeWayRemain:{}:{}";

    /**
     * 通话事件消息幂等处理
     * msgIdempotentLock:{uuid}:{status}
     */
    String MESSAGE_IDEMPOTENT_LOCK = CacheConstant.PREFIX + "msgIdempotentLock:{}:{}";

    /**
     * 坐席签出加锁
     * checkoutLock:{companyCode}:{agentId}
     */
    String CHECKOUT_LOCK = CacheConstant.PREFIX + "checkoutLock:{}:{}";

    /**
     * 话单生成加锁
     * cdrGenerateLock:{companyCode}:{mainCallId}
     */
    String CDR_GENERATE_LOCK = CacheConstant.PREFIX + "cdrGenerateLock:{}:{}";

    /**
     * 坐席状态修改lock
     * agentStatusUpdateLock:{companyCode}:{agentId}
     */
    String AGENT_STATUS_UPDATE_LOCK = CacheConstant.PREFIX + "agentStatusUpdateLock:{}:{}";

    /**
     * uuid上下文状态修改lock
     * agentStatusUpdateLock:{uuid}
     */
    String UUID_CONTEXT_UPDATE_LOCK = CacheConstant.PREFIX + "contextUpdateLock:{}";

    /**
     * 咨询lock
     * consultLock:{uuid}
     */
    String CONSULT_LOCK = CacheConstant.PREFIX + "consultLock:{}";

    /**
     * 外呼任务锁
     * outCallTaskLock:{task_id}
     */
    String OUT_CALL_TASK_LOCK = CacheConstant.PREFIX + "outCallTaskLock:{}";

    /**
     * 外呼锁 内线
     * originateLock:{callee_number}
     * ttl 10s
     */
    String ORIGINATE_LOCK = CacheConstant.PREFIX + "originateLock:{}";
}
