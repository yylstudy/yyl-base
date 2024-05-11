package com.linkcircle.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.basecom.common.DictModel;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.system.dto.*;
import com.linkcircle.system.dto.SysMenuDTO;
import com.linkcircle.system.dto.SysMenuTreeDTO;
import com.linkcircle.system.entity.SysDictItem;
import com.linkcircle.system.entity.SysMenu;

import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/1 13:29
 */

public interface SysMenuService extends IService<SysMenu> {
    /**
     * 查询菜单列表
     *
     */
    List<SysMenuDTO> queryMenuList();
    /**
     * 新增
     */
    Result<String> addMenu(SysMenuAddDTO dto);
    /**
     * 修改
     */
    Result<String> edit(SysMenuUpdateDTO dto);
    /**
     * 批量删除菜单
     */
    Result<String> batchDeleteMenu(List<Long> idList);

    /**
     * 查询菜单详情
     */
    Result<SysMenuDTO> getMenuDetail(Long id);
    /**
     * 查询菜单树
     * @param onlyMenu 是否只查询菜单
     */
    Result<List<SysMenuTreeDTO>> queryMenuTree(Boolean onlyMenu);

    /**
     * 根据parentId获取菜单
     * @param parentId
     */
    List<SysMenu> selectMenuByParentIdList(List<Long> parentId);

    /**
     * 根据菜单ID获取菜单范围
     * @param parentId
     * @return
     */
    Result<List<DictModel>> queryMenuScopeList(long parentId);
}
