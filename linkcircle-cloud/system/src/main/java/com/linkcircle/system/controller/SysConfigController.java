package com.linkcircle.system.controller;

import com.linkcircle.basecom.annotation.OperateLog;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.system.dto.SysConfigAddDTO;
import com.linkcircle.system.dto.SysConfigQueryDTO;
import com.linkcircle.system.entity.SysConfig;
import com.linkcircle.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @Description: 登录
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Tag(name = "系统参数")
@RestController
@RequestMapping("config")
public class SysConfigController  {

    @Resource
    private SysConfigService sysConfigService;

    @Operation(summary = "分页查询")
    @PostMapping("query")
    public Result<PageResult<SysConfig>> query(@Valid @RequestBody SysConfigQueryDTO dto) {
        return sysConfigService.query(dto);
    }

    @Operation(summary = "新增")
    @PostMapping("add")
    @OperateLog(content = "系统参数新增")
    public Result<String> add(@Valid @RequestBody SysConfigAddDTO dto) {
        return sysConfigService.add(dto);
    }

    @Operation(summary = "修改")
    @PostMapping("edit")
    @OperateLog(content = "系统参数修改")
    public Result<String> edit(@Valid @RequestBody SysConfig sysConfig) {
        return sysConfigService.edit(sysConfig);
    }

    @Operation(summary = "查询")
    @GetMapping("queryByKey")
    public Result<SysConfig> queryByKey(@RequestParam String key) {
        return Result.ok(sysConfigService.getConfig(key));
    }

    @Operation(summary = "删除")
    @PostMapping("batchDelete")
    @OperateLog(content = "系统参数删除")
    public Result<String> batchDelete(@RequestBody List<Long> idList) {
        return sysConfigService.batchDelete(idList);
    }
}
