package com.linkcircle.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.system.dto.SysMenuDTO;
import com.linkcircle.system.dto.SysRoleMenuTreeDto;
import com.linkcircle.system.dto.SysRoleMenuUpdateDTO;
import com.linkcircle.system.entity.SysMenu;
import com.linkcircle.system.entity.SysRoleMenu;

import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/1 13:35
 */

public interface SysRoleMenuService extends IService<SysRoleMenu> {
    /**
     * 更新角色菜单权限
     * @param sysRoleMenuUpdateDto
     * @return
     */
    Result<String> updateRoleMenu(SysRoleMenuUpdateDTO sysRoleMenuUpdateDto);

    /**
     * 获取角色关联菜单权限
     * @param roleId
     * @return
     */
    Result<SysRoleMenuTreeDto> getRoleSelectedMenu(Long roleId);
    /**
     * 根据菜单ID删除
     */
    void deleteByMenuIdList(List<Long> menuIds);
    /**
     * 根据角色ID删除
     */
    void deleteByRoleId(Long roleId);

    List<Long> getMenuIdByRoleId(Long roleId);

    List<SysMenu> getSysMenuListByCorpAdminRole(String corpId);

    /**
     * 根据角色ID集合获取列表
     * @param roleIdList
     * @return
     */
    List<SysMenuDTO> getMenuList(List<Long> roleIdList);

    /**
     * 根据角色ID和菜单ID删除
     * @param roleId
     * @param menuIds
     */
    void deleteByMenuIdsAndRoleId(Long roleId,List<Long> menuIds);
}
