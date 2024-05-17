package com.cqt.ivr.controller;

import com.cqt.ivr.entity.vo.JudgeTimeQuantumRes;
import com.cqt.ivr.service.ICompanyPbxtimesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * @author ld
 * @since 2023-07-24
 */
@RestController
@RequestMapping("/sys/serviceTime")
@Api(tags = "服务时间管理")
public class ServiceTimeController {
    @Resource
    ICompanyPbxtimesService companyPbxtimesService;


    @PostMapping("/workingTimeCheck")
    @ApiOperation("效验是否是工作时间")
    public JudgeTimeQuantumRes workingTimeCheck(String timestrategyId) {
        return companyPbxtimesService.validationIsWorkingTime(timestrategyId);
    }
}
