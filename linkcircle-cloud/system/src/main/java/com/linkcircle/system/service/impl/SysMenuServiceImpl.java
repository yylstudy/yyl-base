package com.linkcircle.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.system.common.CommonConstant;
import com.linkcircle.system.common.MenuTypeEnum;
import com.linkcircle.system.dto.*;
import com.linkcircle.system.entity.SysMenu;
import com.linkcircle.system.mapper.SysMenuMapper;
import com.linkcircle.system.mapstruct.SysMenuMapStruct;
import com.linkcircle.system.service.SysMenuService;
import com.linkcircle.system.service.SysRoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private SysMenuMapStruct sysMenuMapStruct;
    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    /**
     * 新增
     */
    @Override
    public Result<String> addMenu(SysMenuAddDTO dto) {
        // 校验菜单名称
        if (this.validateMenuName(dto)) {
            return Result.error("菜单名称已存在");
        }
        SysMenu sysMenu = sysMenuMapStruct.convert(dto);
        sysMenuMapper.insert(sysMenu);
        return Result.ok();
    }
    /**
     * 修改
     */
    @Override
    public  Result<String> edit(SysMenuUpdateDTO dto) {
        if (this.validateMenuName(dto)) {
            return Result.error("菜单名称已存在");
        }
        if (dto.getId().equals(dto.getParentId())) {
            return Result.error("上级菜单不能为自己");
        }
        SysMenu sysMenu = sysMenuMapStruct.convert(dto);
        sysMenuMapper.updateById(sysMenu);
        return Result.ok();
    }
    /**
     * 批量删除菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public  Result<String> batchDeleteMenu(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return Result.error("所选菜单不能为空");
        }
        List<SysMenu> childrenList = selectMenuByParentIdList(idList);
        if (!CollectionUtil.isEmpty(childrenList)) {
            return Result.error("删除失败，请先删除子菜单");
        }
        this.removeByIds(idList);
        sysRoleMenuService.deleteByMenuIdList(idList);
//        //孩子节点也需要删除
//        this.recursiveDeleteChildren(idList);
        return Result.ok();
    }
    /**
     * 查询菜单列表
     *
     */
    @Override
    public List<SysMenuDTO> queryMenuList() {
        List<SysMenuDTO> menuVOList = sysMenuMapStruct.convert(list());
        //根据ParentId进行分组
        Map<Long, List<SysMenuDTO>> parentMap = menuVOList.stream().collect(Collectors.groupingBy(SysMenuDTO::getParentId, Collectors.toList()));
        return this.filterNoParentMenu(parentMap, CommonConstant.TOP_MENU_PATENT_ID);
    }

    /**
     * 查询菜单详情
     */
    @Override
    public Result<SysMenuDTO> getMenuDetail(Long id) {
        SysMenu selectMenu = this.getById(id);
        sysMenuMapper.selectById(id);
        if (selectMenu == null) {
            return Result.error("菜单不存在");
        }
        SysMenuDTO sysMenuDto = sysMenuMapStruct.convert(selectMenu);
        return Result.ok(sysMenuDto);
    }
    /**
     * 过滤没有上级菜单的菜单列表
     */
    private List<SysMenuDTO> filterNoParentMenu(Map<Long, List<SysMenuDTO>> parentMap, Long parentId) {
        // 获取本级菜单树List
        List<SysMenuDTO> res = parentMap.getOrDefault(parentId, new ArrayList<>());
        List<SysMenuDTO> childMenu = new ArrayList<>();
        // 循环遍历下级菜单
        res.forEach(e -> {
            List<SysMenuDTO> menuList = this.filterNoParentMenu(parentMap, e.getId());
            childMenu.addAll(menuList);
        });
        res.addAll(childMenu);
        return res;
    }
    /**
     * 查询菜单树
     * @param onlyMenu 不查询功能点
     */
    @Override
    public Result<List<SysMenuTreeDTO>> queryMenuTree(Boolean onlyMenu) {
        List<SysMenu> sysMenus;
        if (onlyMenu) {
            LambdaQueryWrapper<SysMenu> wrapper = Wrappers.lambdaQuery();
            wrapper.in(SysMenu::getMenuType,MenuTypeEnum.CATALOG.getCode(), MenuTypeEnum.MENU.getCode());
            sysMenus = list(wrapper);
        }else{
            sysMenus = list();
        }
        List<SysMenuDTO> menuVOList = sysMenuMapStruct.convert(sysMenus);
        Map<Long, List<SysMenuDTO>> parentMap = menuVOList.stream()
                .collect(Collectors.groupingBy(SysMenuDTO::getParentId, Collectors.toList()));
        List<SysMenuTreeDTO> menuTreeVOList = this.buildMenuTree(parentMap, CommonConstant.TOP_MENU_PATENT_ID);
        return Result.ok(menuTreeVOList);
    }
    /**
     * 构建菜单树
     */
    List<SysMenuTreeDTO> buildMenuTree(Map<Long, List<SysMenuDTO>> parentMap, Long parentId) {
        // 获取本级菜单树List
        List<SysMenuTreeDTO> res = parentMap.getOrDefault(parentId, new ArrayList<>()).stream()
                .map(sysMenuMapStruct::convert).collect(Collectors.toList());
        // 循环遍历下级菜单
        res.forEach(e -> {
            e.setChildren(this.buildMenuTree(parentMap, e.getId()));
        });
        return res;
    }
    /**
     * 根据parentId获取菜单
     * @param parentId
     */
    @Override
    public List<SysMenu> selectMenuByParentIdList(List<Long> parentId) {
        LambdaQueryWrapper<SysMenu> wrapper = Wrappers.lambdaQuery();
        wrapper.in(SysMenu::getParentId,parentId.toArray());
        return list(wrapper);
    }

    private void recursiveDeleteChildren(List<Long> idList) {
        List<SysMenu> childrenList = selectMenuByParentIdList(idList);
        if (CollectionUtil.isEmpty(childrenList)) {
            return;
        }
        List<Long> childrenIdList = childrenList.stream().map(SysMenu::getId).collect(Collectors.toList());
        this.removeByIds(childrenIdList);
        sysRoleMenuService.deleteByMenuIdList(childrenIdList);
        recursiveDeleteChildren(childrenIdList);
    }
    /**
     * 校验菜单名称
     */
    public <T extends SysMenuBaseDTO> Boolean validateMenuName(T menuDTO) {
        SysMenu menu = getByMenuname(menuDTO.getMenuName(), menuDTO.getParentId());
        if (menuDTO instanceof SysMenuAddDTO) {
            return menu != null;
        }
        if (menuDTO instanceof SysMenuUpdateDTO) {
            Long menuId = ((SysMenuUpdateDTO) menuDTO).getId();
            return menu != null && menu.getId().longValue() != menuId.longValue();
        }
        return true;
    }
    public SysMenu getByMenuname(String menuName,long parentId){
        LambdaQueryWrapper<SysMenu> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysMenu::getMenuName,menuName);
        wrapper.eq(SysMenu::getParentId,parentId);
        return getOne(wrapper);
    }


}
