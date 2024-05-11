package com.linkcircle.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.system.mapper.SysRoleMapper;
import com.linkcircle.system.dto.SysRoleDTO;
import com.linkcircle.system.entity.SysRole;
import com.linkcircle.system.dto.SysRoleAddDTO;
import com.linkcircle.system.dto.SysRoleUpdateDTO;
import com.linkcircle.system.mapstruct.SysRoleMapStruct;
import com.linkcircle.system.service.SysRoleMenuService;
import com.linkcircle.system.service.SysRoleService;
import com.linkcircle.system.service.SysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
    @Autowired
    private SysRoleMapStruct sysRoleMapStruct;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    /**
     * 新增
     */
    @Override
    public Result<String> add(SysRoleAddDTO dto) {
        SysRole existSysRole = getSysRoleByRoleName(dto.getRoleName());
        if (null != existSysRole) {
            return Result.error("角色名称重复");
        }
        existSysRole = getSysRoleByRoleCode(dto.getRoleName());
        if (null != existSysRole) {
            return Result.error("角色编码重复，重复的角色为：" + existSysRole.getRoleName());
        }
        SysRole sysRole = sysRoleMapStruct.convert(dto);
        save(sysRole);
        return Result.ok();
    }
    /**
     * 更新
     */
    @Override
    public Result<String> edit(SysRoleUpdateDTO dto) {
        SysRole existSysRole = getSysRoleByRoleName(dto.getRoleName());
        if (null != existSysRole && !existSysRole.getId().equals(dto.getId())) {
            return Result.error("角色名称重复");
        }
        existSysRole = getSysRoleByRoleCode(dto.getRoleName());
        if (null != existSysRole&&!existSysRole.getId().equals(dto.getId())) {
            return Result.error("角色编码重复，重复的角色为：" + existSysRole.getRoleName());
        }
        SysRole sysRole = sysRoleMapStruct.convert(dto);
        sysRoleMapper.updateById(sysRole);
        return Result.ok();
    }
    /**
     * 根据角色id 删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteRole(Long roleId) {
        removeById(roleId);
        sysRoleMenuService.deleteByRoleId(roleId);
        sysUserRoleService.deleteByRoleId(roleId);
        return Result.ok();
    }

    /**
     * 根据id获取角色数据
     */
    @Override
    public Result<SysRoleDTO> getRoleById(Long roleId) {
        SysRole sysRole = sysRoleMapper.selectById(roleId);
        SysRoleDTO role = sysRoleMapStruct.convert(sysRole);
        return Result.ok(role);
    }
    /**
     * 获取所有角色列表
     */
    @Override
    public Result<List<SysRoleDTO>> getAllRole() {
        List<SysRole> sysRoleList = list();
        List<SysRoleDTO> roleList = sysRoleMapStruct.convert(sysRoleList);
        return Result.ok(roleList);
    }
    public SysRole getSysRoleByRoleName(String roleName){
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysRole::getRoleName,roleName);
        return getOne(wrapper);
    }
    @Override
    public SysRole getSysRoleByRoleCode(String roleCode){
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysRole::getRoleCode,roleCode);
        return getOne(wrapper);
    }
}
