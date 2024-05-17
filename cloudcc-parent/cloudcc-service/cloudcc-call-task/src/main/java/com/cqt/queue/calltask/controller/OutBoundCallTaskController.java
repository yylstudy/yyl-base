package com.cqt.queue.calltask.controller;

import com.cqt.base.model.ResultVO;
import com.cqt.model.calltask.dto.CallTaskOperateDTO;
import com.cqt.queue.calltask.service.OutBoundCallTaskFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linshiqiang
 * date:  2023-10-25 15:43
 * 外呼任务操作
 */
@Api(tags = "任务执行")
@RestController
@RequestMapping("call-task")
@RequiredArgsConstructor
public class OutBoundCallTaskController {

    private final OutBoundCallTaskFactory outBoundCallTaskFactory;

    @ApiOperation("新增任务")
    @PostMapping("addTask")
    public ResultVO<Integer> addTask(@Validated @RequestBody CallTaskOperateDTO callTaskOperateDTO) {
        return outBoundCallTaskFactory.addTask(callTaskOperateDTO);
    }

    @ApiOperation("启动任务")
    @PostMapping("startTask")
    public ResultVO<Void> startTask(@Validated @RequestBody CallTaskOperateDTO callTaskOperateDTO) {
        return outBoundCallTaskFactory.startTask(callTaskOperateDTO);
    }

    @ApiOperation("暂停任务")
    @PostMapping("stopTask")
    public ResultVO<Void> stopTask(@Validated @RequestBody CallTaskOperateDTO callTaskOperateDTO) {
        return outBoundCallTaskFactory.stopTask(callTaskOperateDTO);
    }

    @ApiOperation("更新任务")
    @PostMapping("updateTask")
    public ResultVO<Void> updateTask(@Validated @RequestBody CallTaskOperateDTO callTaskOperateDTO) {
        return outBoundCallTaskFactory.updateTask(callTaskOperateDTO);
    }

    @ApiOperation("删除任务")
    @PostMapping("delTask")
    public ResultVO<Void> delTask(@Validated @RequestBody CallTaskOperateDTO callTaskOperateDTO) {
        return outBoundCallTaskFactory.delTask(callTaskOperateDTO);
    }
}
