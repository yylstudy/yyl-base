package com.linkcircle.system.controller;

import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.system.dto.SysOperateLogQueryDto;
import com.linkcircle.system.entity.SysOperateLog;
import com.linkcircle.system.service.SysOperateLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@RestController
@Tag(name = "用户操作记录")
@RequestMapping("operateLog")
public class SysOperateLogController {

    @Resource
    private SysOperateLogService sysOperateLogService;

    @Operation(summary = "分页查询")
    @PostMapping("page/query")
    public Result<PageResult<SysOperateLog>> queryByPage(@Valid @RequestBody SysOperateLogQueryDto queryForm) {
        return sysOperateLogService.queryByPage(queryForm);
    }

    @Operation(summary = "详情")
    @GetMapping("detail/{id}")
    public Result<SysOperateLog> detail(@PathVariable Long id) {
        SysOperateLog sysOperateLog = sysOperateLogService.getById(id);
        return Result.ok(sysOperateLog);
    }

}
