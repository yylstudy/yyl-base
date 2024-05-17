package com.cqt.queue.calltask.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.base.util.CacheUtil;
import com.cqt.mapper.task.IvrOutboundTaskNumberMapper;
import com.cqt.mapper.task.PredictOutboundTaskNumberMapper;
import com.cqt.model.calltask.entity.IvrOutboundTaskNumber;
import com.cqt.model.calltask.entity.PredictOutboundTaskNumber;
import com.cqt.queue.calltask.service.DataQueryService;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-07-18 18:09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataQueryServiceImpl implements DataQueryService {

    private final RedissonUtil redissonUtil;

    private final PredictOutboundTaskNumberMapper predictOutboundTaskNumberMapper;

    private final IvrOutboundTaskNumberMapper ivrOutboundTaskNumberMapper;

    @Override
    public List<ScoredEntry<String>> getPredictNumberList(String taskId, Integer limit, Integer maxAttemptCount) {
        String predictTaskNumberKey = CacheUtil.getPredictTaskNumberKey(taskId);
        List<ScoredEntry<String>> cacheList = redissonUtil.getZsetScoredEntryList(predictTaskNumberKey, 0, limit);
        if (CollUtil.isNotEmpty(cacheList)) {
            return cacheList;
        }
        LambdaQueryWrapper<PredictOutboundTaskNumber> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(PredictOutboundTaskNumber::getNumberId,
                PredictOutboundTaskNumber::getNumber,
                PredictOutboundTaskNumber::getCallCount);
        queryWrapper.eq(PredictOutboundTaskNumber::getTaskId, taskId);
        queryWrapper.eq(PredictOutboundTaskNumber::getCallStatus, 0);
        queryWrapper.lt(PredictOutboundTaskNumber::getCallCount, maxAttemptCount);
        queryWrapper.last("limit " + limit);
        List<PredictOutboundTaskNumber> list = predictOutboundTaskNumberMapper.selectList(queryWrapper);
        List<ScoredEntry<String>> scoredEntryList = new ArrayList<>();
        if (CollUtil.isNotEmpty(list)) {
            list.stream()
                    .sorted(Comparator.comparing(PredictOutboundTaskNumber::getCallCount))
                    .forEach(item -> {
                        String value = item.getNumberId() + StrUtil.COLON + item.getNumber();
                        Double score = Convert.toDouble(item.getCallCount());
                        redissonUtil.addZset(predictTaskNumberKey, value, score);
                        scoredEntryList.add(new ScoredEntry<>(score, value));
                    });
        }
        return scoredEntryList;
    }

    @Override
    public List<ScoredEntry<String>> getIvrNumberList(String taskId, Integer limit, Integer maxAttemptCount) {
        String ivrTaskNumberKey = CacheUtil.getIvrTaskNumberKey(taskId);
        List<ScoredEntry<String>> cacheList = redissonUtil.getZsetScoredEntryList(ivrTaskNumberKey, 0, limit);
        if (CollUtil.isNotEmpty(cacheList)) {
            return cacheList;
        }

        LambdaQueryWrapper<IvrOutboundTaskNumber> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(IvrOutboundTaskNumber::getNumberId,
                IvrOutboundTaskNumber::getNumber,
                IvrOutboundTaskNumber::getCallCount);
        queryWrapper.eq(IvrOutboundTaskNumber::getTaskId, taskId);
        queryWrapper.eq(IvrOutboundTaskNumber::getCallStatus, 0);
        queryWrapper.lt(IvrOutboundTaskNumber::getCallCount, maxAttemptCount);
        queryWrapper.last("limit " + limit);
        List<IvrOutboundTaskNumber> list = ivrOutboundTaskNumberMapper.selectList(queryWrapper);
        List<ScoredEntry<String>> scoredEntryList = new ArrayList<>();
        if (CollUtil.isNotEmpty(list)) {
            list.stream()
                    .sorted(Comparator.comparing(IvrOutboundTaskNumber::getCallCount))
                    .forEach(item -> {
                        String value = item.getNumberId() + StrUtil.COLON + item.getNumber();
                        Double score = Convert.toDouble(item.getCallCount());
                        redissonUtil.addZset(ivrTaskNumberKey, value, score);
                        scoredEntryList.add(new ScoredEntry<>(score, value));
                    });
        }
        return scoredEntryList;
    }
}
