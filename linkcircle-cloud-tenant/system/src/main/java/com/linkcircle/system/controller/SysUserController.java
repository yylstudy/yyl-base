package com.linkcircle.system.controller;

import com.linkcircle.basecom.annotation.OperateLog;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.filter.LoginUserInfoHolder;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.system.dto.*;
import com.linkcircle.system.service.SysUserService;
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
@Tag(name = "用户管理")
@RequestMapping("user")
public class SysUserController {

    @Resource
    private SysUserService sysUserService;


    @PostMapping("queryNotInRoleUserByRoleId")
    @Operation(summary = "查询不在角色下的用户")
    public Result<PageResult<SysUserDTO>> queryNotInRoleUserByRoleId(@Valid @RequestBody SysRoleUserQueryReqDTO dto) {
        return sysUserService.queryNotInRoleUserByRoleId(dto);
    }

    @PostMapping("query")
    @Operation(summary = "用户查询")
    public Result<PageResult<SysUserDTO>> query(@Valid @RequestBody SysUserQueryReqDTO dto) {
        return sysUserService.query(dto);
    }

    @Operation(summary = "新增")
    @PostMapping("add")
    @OperateLog(content = "用户新增")
    public Result<String> add(@Valid @RequestBody SysUserAddDTO dto) {
        return sysUserService.add(dto);
    }

    @Operation(summary = "更新")
    @PostMapping("update")
    @OperateLog(content = "用户更新")
    public Result<String> edit(@Valid @RequestBody SysUserUpdateDTO dto) {
        return sysUserService.edit(dto);
    }

    @Operation(summary = "禁用/启用用户")
    @GetMapping("update/disabled/{id}")
    @OperateLog(content = "禁用/启用用户")
    public Result<String> updateDisableFlag(@PathVariable Long id) {
        return sysUserService.updateDisableFlag(id);
    }

    @Operation(summary = "批量删除用户")
    @PostMapping("update/batch/delete")
    @OperateLog(content = "批量删除用户")
    public Result<String> batchUpdateDeleteFlag(@Valid @RequestBody List<Long> idList) {
        return sysUserService.batchUpdateDeleteFlag(idList);
    }

    @Operation(summary = "批量调整用户部门")
    @OperateLog(content = "批量调整用户部门")
    @PostMapping("update/batch/depart")
    public Result<String> batchUpdateDepart(@Valid @RequestBody SysUserBatchUpdateDepartDTO dto) {
        return sysUserService.batchUpdateDepart(dto);
    }
    @Operation(summary = "修改密码")
    @PostMapping("update/password")
    @OperateLog(content = "修改密码")
    public Result<String> updatePassword(@Valid @RequestBody SysUserUpdatePasswordDTO dto) {
        dto.setUserId(LoginUserInfoHolder.get().getId());
        return sysUserService.updatePassword(dto);
    }

    @Operation(summary = "重置密码")
    @PostMapping("resetPassword/{id}")
    @OperateLog(content = "重置密码")
    public Result<String> resetPassword(@PathVariable Long id) {
        return sysUserService.resetPassword(id);
    }

    @Operation(summary = "查询所有用户")
    @GetMapping("queryAll")
    public Result<List<SysUserDTO>> queryAllUser(@RequestParam(value = "disabledFlag", required = false) Boolean disabledFlag) {
        return sysUserService.queryAllUser(disabledFlag);
    }

}
