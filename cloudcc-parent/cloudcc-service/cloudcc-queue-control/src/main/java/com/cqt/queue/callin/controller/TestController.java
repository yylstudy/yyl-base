package com.cqt.queue.callin.controller;

import com.cqt.queue.callin.service.queue.IdleQueueStrategyTask;
import com.cqt.queue.callin.service.queue.QueueStrategyTask;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linshiqiang
 * date:  2023-11-22 10:52
 */
@RestController
@RequiredArgsConstructor
public class TestController {

    private final QueueStrategyTask queueStrategyTask;

    private final IdleQueueStrategyTask idleQueueStrategyTask;

    @PostMapping("queueTest")
    public void queueTest(String companyCode) {
        queueStrategyTask.executeTask(companyCode);
    }

    @PostMapping("idleTest")
    public void idleTest(String companyCode) {
        idleQueueStrategyTask.executeTask(companyCode);
    }

    @PostMapping("idlePollTask")
    public void idlePollTask() {
        idleQueueStrategyTask.idlePollTask();
    }

}
