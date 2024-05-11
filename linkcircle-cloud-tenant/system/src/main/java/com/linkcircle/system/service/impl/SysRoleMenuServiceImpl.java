package com.linkcircle.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.system.common.CommonConstant;
import com.linkcircle.system.config.SystemLoginUserInfoHolder;
import com.linkcircle.system.dto.SysMenuDTO;
import com.linkcircle.system.dto.SysMenuSimpleTreeDTO;
import com.linkcircle.system.dto.SysRoleMenuTreeDto;
import com.linkcircle.system.dto.SysRoleMenuUpdateDTO;
import com.linkcircle.system.entity.SysMenu;
import com.linkcircle.system.entity.SysRole;
import com.linkcircle.system.entity.SysRoleMenu;
import com.linkcircle.system.mapper.SysRoleMenuMapper;
import com.linkcircle.system.mapstruct.SysMenuMapStruct;
import com.linkcircle.system.service.SysMenuService;
import com.linkcircle.system.service.SysRoleMenuService;
import com.linkcircle.system.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {
    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired
    private SysMenuMapStruct sysMenuMapStruct;
    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private SysRoleService sysRoleService;

    @Override
    public void deleteByMenuIdList(List<Long> menuIds) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = Wrappers.lambdaQuery();
        wrapper.in(SysRoleMenu::getMenuId,menuIds.toArray());
        remove(wrapper);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysRoleMenu::getRoleId,roleId);
        remove(wrapper);
    }


    /**
     * 更新角色权限
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<String> updateRoleMenu(SysRoleMenuUpdateDTO sysRoleMenuUpdateDto) {
        String corpId = SystemLoginUserInfoHolder.getCorpId();
        Long roleId = sysRoleMenuUpdateDto.getRoleId();
        SysRole sysRole = sysRoleService.getById(roleId);
        if(sysRole.isCorpAdmin()&&!CommonConstant.ADMIN_CORP_ID.equals(corpId)){
            return Result.error("无法对企业管理员角色授权修改");
        }
        List<Long> dbMenuIds = getMenuIdByRoleId(roleId);
        List<Long> menuIds = sysRoleMenuUpdateDto.getMenuIdList();
        //获取需要删除的菜单
        List<Long> deleteMenuIdList = dbMenuIds.stream().filter(menuId->!menuIds.contains(menuId))
                .collect(Collectors.toList());
        //获取需要添加的菜单
        List<SysRoleMenu> addRoleMenuList = menuIds.stream().filter(menuId->!dbMenuIds.contains(menuId))
                .map(menuId->{
                    SysRoleMenu sysRoleMenu = new SysRoleMenu();
                    sysRoleMenu.setRoleId(roleId);
                    sysRoleMenu.setMenuId(menuId);
                    return sysRoleMenu;
                }).collect(Collectors.toList());
        // 批量添加菜单权限
        if(!addRoleMenuList.isEmpty()){
            saveBatch(addRoleMenuList);
        }
        Set<Long> deleteRoleIds = new HashSet<>();
        deleteRoleIds.add(roleId);
        if(!deleteMenuIdList.isEmpty()){
            //删除角色菜单关联关系
            deleteByMenuIdsAndRoleId(roleId,deleteMenuIdList);
            if(sysRole.isCorpAdmin()){
                //删除该企业下所有角色与删除的菜单的关联关系
                sysRoleMenuMapper.deleteByCorpIdAndMenuIdList(sysRole.getCorpId(),deleteMenuIdList);
            }
        }
        return Result.ok();
    }



    /**
     * 根据角色id集合，查询其所有的菜单权限
     */
    @Override
    public List<SysMenuDTO> getMenuList(List<Long> roleIdList) {
        //非管理员 无角色 返回空菜单
        if (CollectionUtils.isEmpty(roleIdList)) {
            return new ArrayList<>();
        }
        List<SysMenu> sysMenuList = sysRoleMenuMapper.selectMenuListByRoleIdList(roleIdList);
        return sysMenuMapStruct.convert(sysMenuList);
    }

    /**
     * 获取角色关联菜单权限
     */
    @Override
    public Result<SysRoleMenuTreeDto> getRoleSelectedMenu(Long roleId) {
        SysRoleMenuTreeDto res = new SysRoleMenuTreeDto();
        res.setRoleId(roleId);
        //查询角色ID选择的菜单权限
        LambdaQueryWrapper<SysRoleMenu> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysRoleMenu::getRoleId,roleId);
        List<Long> selectedMenuId = list(wrapper).stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        res.setSelectedMenuId(selectedMenuId);
        //查询菜单权限
        List<SysMenuDTO> menuVOList = sysMenuService.queryMenuList();
        Map<Long, List<SysMenuDTO>> parentMap = menuVOList.stream().collect(Collectors.groupingBy(SysMenuDTO::getParentId, Collectors.toList()));
        List<SysMenuSimpleTreeDTO> menuTreeList = this.buildMenuTree(parentMap, CommonConstant.TOP_MENU_PATENT_ID);
        res.setMenuTreeList(menuTreeList);
        return Result.ok(res);
    }

    /**
     * 构建菜单树
     */
    private List<SysMenuSimpleTreeDTO> buildMenuTree(Map<Long, List<SysMenuDTO>> parentMap, Long parentId) {
        // 获取本级菜单树List
        List<SysMenuSimpleTreeDTO> res = parentMap.getOrDefault(parentId, new ArrayList<>()).stream()
                .map(sysMenuMapStruct::convertToSimpleTreeDto).collect(Collectors.toList());
        // 循环遍历下级菜单
        res.forEach(e -> {
            e.setChildren(this.buildMenuTree(parentMap, e.getId()));
        });
        return res;
    }

    @Override
    public List<Long> getMenuIdByRoleId(Long roleId) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = Wrappers.lambdaQuery();
        wrapper.select(SysRoleMenu::getMenuId);
        wrapper.eq(SysRoleMenu::getRoleId,roleId);
        return listObjs(wrapper,o->(Long) o);
    }

    @Override
    public List<SysMenu> getSysMenuListByCorpAdminRole(String corpId) {
        return sysRoleMenuMapper.getSysMenuListByCorpAdminRole(corpId);
    }

    @Override
    public void deleteByMenuIdsAndRoleId(Long roleId,List<Long> menuIds) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = Wrappers.lambdaQuery();
        wrapper.in(SysRoleMenu::getMenuId,menuIds.toArray());
        wrapper.eq(SysRoleMenu::getRoleId,roleId);
        remove(wrapper);
    }
}
