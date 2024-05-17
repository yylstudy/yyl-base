package com.cqt.push.controller;


import com.cqt.model.common.Result;
import com.cqt.model.hmbc.vo.HmbcResult;
import com.cqt.push.service.DialTestResultService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;


/**
 * 拨测结果接收控制器
 *
 * @author Xienx
 * @date 2023年02月08日 15:03
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(value = "拨测结果接收", tags = "拨测结果接收")
public class DialTestResultController {

    private final DialTestResultService dialTestResultService;

    @ApiOperation(value = "号码拨测结果接收")
    @PostMapping(value = "hmbc-result")
    public Result hmbcResult(@RequestBody HmbcResult hmbcResult, HttpServletResponse response) {
        return dialTestResultService.hmbcResult(hmbcResult, response);
    }
}
