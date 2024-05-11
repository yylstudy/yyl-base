package com.linkcircle.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.basecom.page.PageUtil;
import com.linkcircle.system.dto.SysRoleDTO;
import com.linkcircle.system.dto.SysUserDTO;
import com.linkcircle.system.dto.SysUserRoleQueryDTO;
import com.linkcircle.system.dto.SysUserRoleUpdateDTO;
import com.linkcircle.system.entity.SysRoleMenu;
import com.linkcircle.system.entity.SysUserRole;
import com.linkcircle.system.mapper.SysUserRoleMapper;
import com.linkcircle.system.service.SysUserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    /**
     * 通过角色id，分页获取成员用户列表
     */
    @Override
    public Result<PageResult<SysUserDTO>> queryUserByRole(SysUserRoleQueryDTO dto) {
        Page page = PageUtil.convert2PageQuery(dto);
        List<SysUserDTO> sysUserDTOS = sysUserRoleMapper.selectSysUser(page, dto)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        PageResult<SysUserDTO> pageResult = PageUtil.convert2PageResult(page, sysUserDTOS);
        return Result.ok(pageResult);
    }

    /**
     * 根据角色ID获取用户
     * @param roleId
     * @return
     */
    @Override
    public List<SysUserDTO> getUserByRoleId(Long roleId) {
        return sysUserRoleMapper.getUserByRoleId(roleId);
    }

    /**
     * 移除用户角色
     */
    @Override
    public Result<String> deleteSysUserRole(Long userId, Long roleId) {
        LambdaQueryWrapper<SysUserRole> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUserRole::getUserId,userId);
        wrapper.eq(SysUserRole::getRoleId,roleId);
        remove(wrapper);
        return Result.ok();
    }

    /**
     * 批量删除角色的成员用户
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchDeleteSysUserRole(SysUserRoleUpdateDTO dto) {
        LambdaQueryWrapper<SysUserRole> wrapper = Wrappers.lambdaQuery();
        wrapper.in(SysUserRole::getUserId,dto.getUserIdList().toArray());
        wrapper.eq(SysUserRole::getRoleId,dto.getRoleId());
        remove(wrapper);
        return Result.ok();
    }

    /**
     * 批量添加角色的成员用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchAddSysUserRole(SysUserRoleUpdateDTO dto) {
        Long roleId = dto.getRoleId();
        List<Long> userIdList = dto.getUserIdList();
        // 保存新的角色用户
        List<SysUserRole> sysUserRoles = null;
        if (!CollectionUtils.isEmpty(userIdList)) {
            sysUserRoles = userIdList.stream()
                    .map(userId-> new SysUserRole(roleId,userId))
                    .collect(Collectors.toList());
        }
        List<Long> dbUserIdList = getUserIdByRoleId(roleId);
        Collection<Long> intersectionList = CollectionUtil.intersection(dto.getUserIdList(),dbUserIdList);
        if(!intersectionList.isEmpty()){
            return Result.error("该角色已存在此用户");
        }
        if (!CollectionUtils.isEmpty(sysUserRoles)) {
            this.saveBatch(sysUserRoles);
        }
        return Result.ok();
    }

    /**
     * 根据用户id 查询角色id集合
     */
    @Override
    public List<SysRoleDTO> selectRoleByUserId(Long userId) {
        return sysUserRoleMapper.selectRoleByUserId(userId);
    }

    @Override
    public void deleteByRoleId(long roleId) {
        LambdaQueryWrapper<SysUserRole> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUserRole::getRoleId,roleId);
        remove(wrapper);
    }

    @Override
    public List<Long> selectRoleIdsByUserId(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUserRole::getUserId,userId);
        List<SysUserRole> list = list(wrapper);
        return list.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRoleIdsAndUserId(Long userId, List<Long> roleIds) {
        LambdaQueryWrapper<SysUserRole> wrapper = Wrappers.lambdaQuery();
        wrapper.in(SysUserRole::getRoleId,roleIds);
        wrapper.eq(SysUserRole::getUserId,userId);
        remove(wrapper);
    }

    @Override
    public List<Long> getUserIdByRoleId(Long roleId) {
        LambdaQueryWrapper<SysUserRole> wrapper = Wrappers.lambdaQuery();
        wrapper.select(SysUserRole::getUserId);
        wrapper.eq(SysUserRole::getRoleId,roleId);
        return listObjs(wrapper,o->(Long)o);
    }
}
