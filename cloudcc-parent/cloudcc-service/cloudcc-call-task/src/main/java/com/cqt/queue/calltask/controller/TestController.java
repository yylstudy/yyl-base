package com.cqt.queue.calltask.controller;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.util.TraceIdUtil;
import com.cqt.mapper.task.IvrOutboundTaskMapper;
import com.cqt.mapper.task.PredictOutboundTaskMapper;
import com.cqt.model.calltask.entity.IvrOutboundTask;
import com.cqt.model.calltask.entity.PredictOutboundTask;
import com.cqt.queue.calltask.service.impl.IvrOutboundCallTaskStrategyImpl;
import com.cqt.queue.calltask.service.impl.PredictOutboundCallTaskStrategyImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linshiqiang
 * date:  2023-11-22 10:52
 */
@RestController
@RequiredArgsConstructor
public class TestController {

    private final IvrOutboundCallTaskStrategyImpl ivrOutboundCallTaskStrategyImpl;

    private final PredictOutboundCallTaskStrategyImpl predictOutboundCallTaskStrategyImpl;

    private final IvrOutboundTaskMapper ivrOutboundTaskMapper;

    private final PredictOutboundTaskMapper predictOutboundTaskMapper;

    @GetMapping("testIvr")
    public void testIvr(String taskId) throws Exception {
        IvrOutboundTask ivrOutboundTask = ivrOutboundTaskMapper.selectById(taskId);
        try {
            String companyCode = ivrOutboundTask.getCompanyCode();
            TraceIdUtil.setTraceId(companyCode + StrUtil.AT + taskId);
            ivrOutboundCallTaskStrategyImpl.execute(ivrOutboundTask);
        } finally {
            TraceIdUtil.remove();
        }
    }

    @GetMapping("testPredict")
    public void testPredict(String taskId) throws Exception {
        PredictOutboundTask predictOutboundTask = predictOutboundTaskMapper.selectById(taskId);
        try {
            String companyCode = predictOutboundTask.getCompanyCode();
            TraceIdUtil.setTraceId(companyCode + StrUtil.AT + taskId);
            predictOutboundCallTaskStrategyImpl.execute(predictOutboundTask);
        } finally {
            TraceIdUtil.remove();
        }
    }

}
