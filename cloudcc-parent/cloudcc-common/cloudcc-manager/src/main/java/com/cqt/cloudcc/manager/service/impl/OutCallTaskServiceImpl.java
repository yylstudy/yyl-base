package com.cqt.cloudcc.manager.service.impl;

import com.cqt.base.enums.calltask.CallTaskEnum;
import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.service.OutCallTaskService;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-10-31 14:08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutCallTaskServiceImpl implements OutCallTaskService {

    private final RedissonUtil redissonUtil;

    @Override
    public void removeNumber(String taskId, String member, CallTaskEnum callTaskEnum) {
        if (CallTaskEnum.IVR.equals(callTaskEnum)) {
            String ivrTaskNumberKey = CacheUtil.getIvrTaskNumberKey(taskId);
            redissonUtil.removeZsetObject(ivrTaskNumberKey, member);
            return;
        }
        if (CallTaskEnum.PREDICT_TASK.equals(callTaskEnum)) {
            String predictTaskNumberKey = CacheUtil.getPredictTaskNumberKey(taskId);
            redissonUtil.removeZsetObject(predictTaskNumberKey, member);
            String taskNumberCallStatusKey = CacheUtil.getPredictTaskNumberCallStatusKey(taskId);
            redissonUtil.removeHashItem(taskNumberCallStatusKey, member);
        }
    }
}
