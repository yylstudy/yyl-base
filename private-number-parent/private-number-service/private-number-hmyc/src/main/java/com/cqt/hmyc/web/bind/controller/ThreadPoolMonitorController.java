package com.cqt.hmyc.web.bind.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author linshiqiang
 * date:  2023-02-24 16:08
 */
@RequiredArgsConstructor
@RestController
public class ThreadPoolMonitorController {

    private final ThreadPoolTaskExecutor saveExecutor;

    private final ThreadPoolTaskExecutor bindExecutor;

    private final ThreadPoolTaskExecutor otherExecutor;

    @GetMapping("getPoolInfo")
    public Map<String, Object> getPoolInfo() {
        Map<String, Object> result = new HashMap<>(16);
        ThreadPoolExecutor save = saveExecutor.getThreadPoolExecutor();
        Map<String, Object> saveMap = new HashMap<>(16);
        result.put("save", getInfo(save, saveMap));

        ThreadPoolExecutor bind = bindExecutor.getThreadPoolExecutor();
        result.put("bind", getInfo(bind, saveMap));

        ThreadPoolExecutor other = otherExecutor.getThreadPoolExecutor();
        result.put("other", getInfo(other, saveMap));

        return result;
    }

    private Map<String, Object> getInfo(ThreadPoolExecutor save, Map<String, Object> map) {
        map.put("activeCount", save.getActiveCount());
        map.put("completedTaskCount", save.getCompletedTaskCount());
        map.put("taskCount", save.getTaskCount());
        map.put("queueSize", save.getQueue().size());
        map.put("largestPoolSize", save.getLargestPoolSize());
        map.put("maximumPoolSize", save.getMaximumPoolSize());
        map.put("poolSize", save.getPoolSize());
        map.put("corePoolSize", save.getCorePoolSize());
        return map;
    }
}
