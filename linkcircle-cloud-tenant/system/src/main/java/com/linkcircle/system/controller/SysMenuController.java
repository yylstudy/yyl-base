package com.linkcircle.system.controller;

import com.linkcircle.basecom.annotation.OperateLog;
import com.linkcircle.basecom.common.DictModel;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.system.dto.*;
import com.linkcircle.system.entity.SysDictItem;
import com.linkcircle.system.service.SysMenuService;
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
@Tag(name = "菜单管理")
@RequestMapping("menu")
public class SysMenuController {
    @Resource
    private SysMenuService sysMenuService;

    @Operation(summary = "新增")
    @PostMapping("add")
    @OperateLog(content = "菜单新增")
    public Result<String> addMenu(@RequestBody @Valid SysMenuAddDTO menuAddForm) {
        return sysMenuService.addMenu(menuAddForm);
    }

    @Operation(summary = "修改")
    @PostMapping("update")
    @OperateLog(content = "菜单修改")
    public Result<String> updateMenu(@RequestBody @Valid SysMenuUpdateDTO menuUpdateForm) {
        return sysMenuService.edit(menuUpdateForm);
    }

    @Operation(summary = "批量删除")
    @PostMapping("batchDelete")
    @OperateLog(content = "菜单删除")
    public Result<String> batchDeleteMenu(@RequestParam("idList") List<Long> idList) {
        return sysMenuService.batchDeleteMenu(idList);
    }

    @Operation(summary = "菜单列表")
    @GetMapping("query")
    public Result<List<SysMenuDTO>> queryMenuList() {
        return Result.ok(sysMenuService.queryMenuList());
    }

    @Operation(summary = "查询菜单详情")
    @GetMapping("detail/{id}")
    public Result<SysMenuDTO> getMenuDetail(@PathVariable Long id) {
        return sysMenuService.getMenuDetail(id);
    }

    @Operation(summary = "查询菜单树")
    @GetMapping("tree")
    public Result<List<SysMenuTreeDTO>> queryMenuTree(@RequestParam("onlyMenu") Boolean onlyMenu) {
        return sysMenuService.queryMenuTree(onlyMenu);
    }

    @Operation(summary = "获取菜单范围")
    @GetMapping("queryMenuScopeList")
    public Result<List<DictModel>> queryMenuScopeList(@RequestParam("parentId") Long parentId) {
        return sysMenuService.queryMenuScopeList(parentId);
    }
}
