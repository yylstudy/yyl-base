package com.linkcircle.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.system.common.CommonConstant;
import com.linkcircle.system.config.SystemLoginUserInfoHolder;
import com.linkcircle.system.dto.SysRoleDTO;
import com.linkcircle.system.mapper.SysRoleMapper;
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
        String corpId = SystemLoginUserInfoHolder.getCorpId();
        SysRole existSysRole = getSysRoleByRoleNameAndCorpId(dto.getRoleName(),corpId);
        if (null != existSysRole) {
            return Result.error("角色名称重复");
        }
        existSysRole = getSysRoleByRoleCodeAndCorpId(dto.getRoleCode(),corpId);
        if (null != existSysRole) {
            return Result.error("角色编码重复，重复的角色为：" + existSysRole.getRoleName());
        }
        SysRole sysRole = sysRoleMapStruct.convert(dto);
        sysRole.setCorpAdmin(false);
        sysRole.setCorpId(corpId);
        save(sysRole);
        return Result.ok();
    }
    /**
     * 更新
     */
    @Override
    public Result<String> edit(SysRoleUpdateDTO dto) {
        SysRole existSysRole = getSysRoleByRoleNameAndCorpId(dto.getRoleName(),dto.getCorpId());
        if (null != existSysRole && !existSysRole.getId().equals(dto.getId())) {
            return Result.error("角色名称重复");
        }
        existSysRole = getSysRoleByRoleCodeAndCorpId(dto.getRoleName(),dto.getCorpId());
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
        SysRole sysRole = getById(roleId);
        if(sysRole.isCorpAdmin()){
            return Result.error("企业管理员角色无法删除");
        }
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
        String corpId = SystemLoginUserInfoHolder.getCorpId();
        List<SysRole> sysRoleList = getAllRoleByCorpId(corpId);
        List<SysRoleDTO> roleList = sysRoleMapStruct.convert(sysRoleList);
        return Result.ok(roleList);
    }

    /**
     * 获取企业下的所有角色
     * @return
     */
    @Override
    public Result<List<SysRoleDTO>> getCorpRole() {
        String corpId = SystemLoginUserInfoHolder.getCorpId();
        List<SysRole> sysRoleList = getByCorpId(corpId);
        List<SysRoleDTO> roleList = sysRoleMapStruct.convert(sysRoleList);
        return Result.ok(roleList);
    }

    public SysRole getSysRoleByRoleNameAndCorpId(String roleName, String corpId){
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysRole::getRoleName,roleName);
        wrapper.eq(SysRole::getCorpId,corpId);
        return getOne(wrapper);
    }
    @Override
    public SysRole getSysRoleByRoleCodeAndCorpId(String roleCode,String corpId){
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysRole::getRoleCode,roleCode);
        wrapper.eq(SysRole::getCorpId,corpId);
        return getOne(wrapper);
    }

    public List<SysRole> getAllRoleByCorpId(String corpId){
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.lambdaQuery();
        if(CommonConstant.ADMIN_CORP_ID.equals(corpId)){
            wrapper.eq(SysRole::getCorpId,corpId);
            wrapper.or().eq(SysRole::isCorpAdmin,true);
        }else{
            wrapper.eq(SysRole::getCorpId,corpId);
        }
        return list(wrapper);
    }

    @Override
    public List<SysRole> getByCorpId(String corpId) {
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysRole::getCorpId,corpId);
        return list(wrapper);
    }

    @Override
    public List<SysRole> getCorpAdminRole(List<String> corpIdList) {
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysRole::isCorpAdmin,true);
        if(!CollectionUtil.isEmpty(corpIdList)){
            wrapper.in(SysRole::getCorpId,corpIdList.toArray());
        }
        return list(wrapper);
    }
}
