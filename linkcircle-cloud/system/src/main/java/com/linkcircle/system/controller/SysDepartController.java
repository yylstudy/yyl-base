package com.linkcircle.system.controller;

import com.linkcircle.basecom.annotation.OperateLog;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.system.dto.*;
import com.linkcircle.system.dto.SysDepartDTO;
import com.linkcircle.system.dto.SysDepartTreeDTO;
import com.linkcircle.system.service.SysDepartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@RestController
@Tag(name = "部门管理")
@RequestMapping("depart")
public class SysDepartController {
    @Resource
    private SysDepartService sysDepartService;

    @Operation(summary = "查询部门列表")
    @GetMapping("listAll")
    public Result<List<SysDepartDTO>> listAll() {
        List<SysDepartDTO> list = sysDepartService.listAll();
        return Result.ok(list);
    }


    @Operation(summary = "查询部门树形列表")
    @GetMapping("treeList")
    public Result<List<SysDepartTreeDTO>> departTree() {
        List<SysDepartTreeDTO> list = sysDepartService.departTree();
        return Result.ok(list);
    }

    @Operation(summary = "新增")
    @PostMapping("add")
    @OperateLog(content = "部门新增")
    public Result<String> add(@Valid @RequestBody SysDepartAddDTO dto) {
        return sysDepartService.add(dto);
    }

    @Operation(summary = "修改")
    @PostMapping("update")
    @OperateLog(content = "部门修改")
    public Result<String> edit(@Valid @RequestBody SysDepartUpdateDTO dto) {
        return sysDepartService.edit(dto);
    }

    @Operation(summary = "删除")
    @PostMapping("delete/{id}")
    @OperateLog(content = "部门删除")
    public Result<String> deleteDepart(@PathVariable Long id) {
        sysDepartService.deleteDepartById(id);
        return Result.ok();
    }

}
