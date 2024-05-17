package com.cqt.base.contants;

/**
 * @author linshiqiang
 * date:  2023-10-25 9:37
 * 外呼任务缓存
 */
public interface CallTaskCacheConstant {

    /**
     * ivr外呼号码列表
     * redis zset
     * key: cloudcc:ivrTask:{task_id}
     * member: {number_id}:{number}
     * score: 0
     */
    String IVR_TASK_NUMBER_KEY = CacheConstant.PREFIX + "ivrTask:{}";

    /**
     * ivr外呼号码呼叫状态
     * redis hash
     * key: cloudcc:ivrTaskCallStatus:{task_id}
     * value: {timestamp}
     */
    String IVR_TASK_NUMBER_CALL_STATUS_KEY = CacheConstant.PREFIX + "ivrTaskCallStatus:{}";

    /**
     * 预测外呼号码列表
     * redis zset
     * key: cloudcc:predictTask:{task_id}
     * member: {number_id}:{number}
     * score: 0
     */
    String PREDICT_TASK_NUMBER_KEY = CacheConstant.PREFIX + "predictTask:{}";

    /**
     * 预测外呼-坐席列表
     * redis set
     * key: cloudcc:predictTaskAgents:{task_id}
     * member: {agent_id}
     */
    String PREDICT_TASK_AGENT_KEY = CacheConstant.PREFIX + "predictTaskAgents:{}";

    /**
     * ivr外呼号码呼叫状态
     * redis hash
     * key: cloudcc:ivrTaskCallStatus:{task_id}
     * value: {timestamp}
     */
    String PREDICT_TASK_NUMBER_CALL_STATUS_KEY = CacheConstant.PREFIX + "predictTaskCallStatus:{}";
}
