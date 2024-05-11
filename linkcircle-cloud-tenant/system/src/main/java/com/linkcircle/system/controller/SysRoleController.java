package com.linkcircle.system.controller;

import com.linkcircle.basecom.annotation.OperateLog;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.system.dto.*;
import com.linkcircle.system.service.SysRoleMenuService;
import com.linkcircle.system.service.SysRoleService;
import com.linkcircle.system.service.SysUserRoleService;
import com.linkcircle.system.service.impl.SysRoleMenuServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@RestController
@Tag(name = "角色管理")
@RequestMapping("role")
public class SysRoleController {
    @Autowired
    private SysRoleMenuService roleMenuService;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Operation(summary = "新增")
    @PostMapping("add")
    @OperateLog(content = "角色新增")
    public Result<String> addRole(@Valid @RequestBody SysRoleAddDTO sysRoleAddDto) {
        return sysRoleService.add(sysRoleAddDto);
    }

    @Operation(summary = "删除")
    @PostMapping("delete/{roleId}")
    @OperateLog(content = "角色删除")
    public Result<String> deleteRole(@PathVariable Long roleId) {
        return sysRoleService.deleteRole(roleId);
    }

    @Operation(summary = "修改")
    @PostMapping("update")
    @OperateLog(content = "角色修改")
    public Result<String> updateRole(@Valid  @RequestBody SysRoleUpdateDTO roleUpdateDTO) {
        return sysRoleService.edit(roleUpdateDTO);
    }

    @Operation(summary = "获取角色数据")
    @GetMapping("get/{roleId}")
    public Result<SysRoleDTO> getRole(@PathVariable("roleId") Long roleId) {
        return sysRoleService.getRoleById(roleId);
    }

    @Operation(summary = "获取所有角色")
    @GetMapping("getAll")
    public Result<List<SysRoleDTO>> getAllRole() {
        return sysRoleService.getAllRole();
    }
    @Operation(summary = "获取企业下的所有角色")
    @GetMapping("getCorpRole")
    public Result<List<SysRoleDTO>> getCorpRole() {
        return sysRoleService.getCorpRole();
    }

    @Operation(summary = "更新角色权限")
    @PostMapping("menu/updateRoleMenu")
    @OperateLog(content = "更新角色权限")
    public Result<String> updateRoleMenu(@Valid @RequestBody SysRoleMenuUpdateDTO updateDTO) {
        return roleMenuService.updateRoleMenu(updateDTO);
    }

    @Operation(summary = "获取角色关联菜单权限")
    @GetMapping("menu/getRoleSelectedMenu/{roleId}")
    public Result<SysRoleMenuTreeDto> getRoleSelectedMenu(@PathVariable Long roleId) {
        return roleMenuService.getRoleSelectedMenu(roleId);
    }

    @Operation(summary = "查询某个角色下的用户列表 ")
    @PostMapping("user/queryUserByRole")
    public Result<PageResult<SysUserDTO>> queryUserByRole(@Valid @RequestBody SysUserRoleQueryDTO sysUserRoleQueryDto) {
        return sysUserRoleService.queryUserByRole(sysUserRoleQueryDto);
    }

    @Operation(summary = "获取某个角色下的所有用户")
    @GetMapping("user/getAllUserByRoleId/{roleId}")
    public Result<List<SysUserDTO>> listAllUserRoleId(@PathVariable Long roleId) {
        return Result.ok(sysUserRoleService.getUserByRoleId(roleId));
    }

    @Operation(summary = "从角色成员列表中移除用户")
    @PostMapping("user/removeSysUserRole")
    @OperateLog(content = "从角色成员列表中移除用户")
    public Result<String> removeSysUserRole(@RequestParam("userId") Long userId, @RequestParam("roleId") Long roleId) {
        return sysUserRoleService.deleteSysUserRole(userId, roleId);
    }

    @Operation(summary = "从角色成员列表中批量移除用户")
    @PostMapping("user/batchDeleteSysUserRole")
    @OperateLog(content = "从角色成员列表中批量移除用户")
    public Result<String> batchDeleteSysUserRole(@Valid @RequestBody SysUserRoleUpdateDTO dto) {
        return sysUserRoleService.batchDeleteSysUserRole(dto);
    }

    @Operation(summary = "角色成员列表中批量添加用户")
    @PostMapping("user/batchAddSysUserRole")
    @OperateLog(content = "角色成员列表中批量添加用户")
    public Result<String> batchAddSysUserRole(@Valid @RequestBody SysUserRoleUpdateDTO dto) {
        return sysUserRoleService.batchAddSysUserRole(dto);
    }

}
