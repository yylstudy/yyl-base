package com.linkcircle.system.controller;

import com.linkcircle.basecom.annotation.OperateLog;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.system.dto.CorpAddDTO;
import com.linkcircle.system.dto.CorpQueryDTO;
import com.linkcircle.system.dto.CorpResDTO;
import com.linkcircle.system.dto.CorpUpdateDTO;
import com.linkcircle.system.service.CorpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @Description: 登录
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Tag(name = "企业管理")
@RestController
@RequestMapping("corp")
public class CorpController {

    @Resource
    private CorpService corpService;

    @Operation(summary = "分页查询")
    @PostMapping("query")
    public Result<PageResult<CorpResDTO>> query(@Valid @RequestBody CorpQueryDTO dto) {
        return corpService.query(dto);
    }

    @Operation(summary = "新增")
    @PostMapping("add")
    @OperateLog(content = "企业新增")
    public Result<String> add(@Valid @RequestBody CorpAddDTO dto) {
        return corpService.add(dto);
    }

    @Operation(summary = "修改")
    @PostMapping("edit")
    @OperateLog(content = "企业修改")
    public Result<String> edit(@Valid @RequestBody CorpUpdateDTO dto) {
        return corpService.edit(dto);
    }

    @Operation(summary = "删除")
    @PostMapping("delete")
    @OperateLog(content = "企业删除")
    public Result<String> delete(@RequestParam("id") String id) {
        return corpService.delete(id);
    }
}
