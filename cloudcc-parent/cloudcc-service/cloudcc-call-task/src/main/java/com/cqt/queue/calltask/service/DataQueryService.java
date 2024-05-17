package com.cqt.queue.calltask.service;

import org.redisson.client.protocol.ScoredEntry;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-07-18 18:09
 */
public interface DataQueryService {

    List<ScoredEntry<String>> getPredictNumberList(String taskId, Integer limit, Integer maxAttemptCount);

    List<ScoredEntry<String>> getIvrNumberList(String taskId, Integer limit, Integer maxAttemptCount);

}
