package com.linkcircle.system.controller;

import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.system.dto.SysLoginLogQueryDTO;
import com.linkcircle.system.dto.SysLoginLogDTO;
import com.linkcircle.system.service.impl.SysLoginLogServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@RestController
@Tag(name = "登录日志")
public class SysLoginLogController {

    @Resource
    private SysLoginLogServiceImpl sysLoginLogServiceImpl;

    @Operation(summary = "分页查询")
    @PostMapping("/loginLog/page/query")
    public Result<PageResult<SysLoginLogDTO>> queryByPage(@Valid @RequestBody SysLoginLogQueryDTO queryForm) {
        return sysLoginLogServiceImpl.queryByPage(queryForm);
    }


}
