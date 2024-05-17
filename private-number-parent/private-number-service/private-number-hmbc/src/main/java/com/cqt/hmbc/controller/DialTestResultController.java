package com.cqt.hmbc.controller;

import com.alibaba.fastjson.JSON;
import com.cqt.hmbc.service.DialTestResultService;
import com.cqt.model.hmbc.dto.CdrRecordSimpleEntity;
import com.cqt.model.hmbc.vo.DialTestReturnT;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DialTestResultController
 *
 * @author Xienx
 * @date 2023年02月09日 17:39
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "外呼拨测话单管理")
@RequestMapping(value = "/dial-test")
public class DialTestResultController {

    private final DialTestResultService dialTestResultService;

    @ApiOperation(value = "外呼拨测话单接收", notes = "外呼拨测话单接收")
    @PostMapping(value = "result")
    public DialTestReturnT dialTestResult(@RequestBody String json) {
        log.info("外呼拨测话单接收: {}", json);
        CdrRecordSimpleEntity cdrRecord = JSON.parseObject(json, CdrRecordSimpleEntity.class);
        dialTestResultService.dialTestResult(cdrRecord);
        return DialTestReturnT.ok(cdrRecord.getAcrCallId());
    }
}
