package com.cqt.cloudcc.manager.service;

import com.cqt.base.enums.calltask.CallTaskEnum;

/**
 * @author linshiqiang
 * date:  2023-10-31 14:07
 */
public interface OutCallTaskService {

    /**
     * 号码达到呼叫次数上限 移除zset, 接通移除zset
     *
     * @param taskId       任务id
     * @param member       成员
     * @param callTaskEnum 任务类型
     */
    void removeNumber(String taskId, String member, CallTaskEnum callTaskEnum);
}
