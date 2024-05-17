package com.cqt.monitor.web.callevent.controller;

import com.cqt.common.constants.SystemConstant;
import com.cqt.model.common.Result;
import com.cqt.monitor.web.callevent.service.CallEventService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author huweizhong
 * date  2023/10/30 14:57
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "查询监控项")
@Slf4j
public class CallEventController {

    private final CallEventService callEventService;

    @ApiOperation(value = "查询接通率")
    @GetMapping("/get-pickup")
    public Result cdr(String vccId,String time) {

        return callEventService.getPickUp(vccId, time);
    }

    @ApiOperation(value = "查询话单数量")
    @GetMapping("/get-count")
    public Result getCount(String vccId, String month) {

        return callEventService.getCount(vccId, month);
    }

    @ApiOperation(value = "查询企业并发")
    @GetMapping("/get-concurrency")
    public Result getConcurrency(String vccId,String time) {

        return callEventService.getConcurrency(vccId, time);
    }
}
