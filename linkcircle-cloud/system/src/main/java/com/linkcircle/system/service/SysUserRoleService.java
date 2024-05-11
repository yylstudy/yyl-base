package com.linkcircle.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.system.dto.SysRoleDTO;
import com.linkcircle.system.dto.SysUserDTO;
import com.linkcircle.system.dto.SysUserRoleQueryDTO;
import com.linkcircle.system.dto.SysUserRoleUpdateDTO;
import com.linkcircle.system.entity.SysUserRole;

import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/1 15:16
 */

public interface SysUserRoleService extends IService<SysUserRole> {
    /**
     * 通过角色id，分页获取成员用户列表
     */
    Result<PageResult<SysUserDTO>> queryUserByRole(SysUserRoleQueryDTO dto);

    /**
     * 根据角色ID获取用户
     * @param roleId
     * @return
     */
    List<SysUserDTO> getUserByRoleId(Long roleId);

    /**
     * 根据角色ID删除
     * @param roleId
     */
    void deleteByRoleId(long roleId);

    /**
     * 移除用户角色
     */
    Result<String> deleteSysUserRole(Long userId, Long roleId);
    /**
     * 批量删除角色的成员用户
     *
     */
    Result<String> batchDeleteSysUserRole(SysUserRoleUpdateDTO dto);
    /**
     * 批量添加角色的成员用户
     */
    Result<String> batchAddSysUserRole(SysUserRoleUpdateDTO dto);
    /**
     * 根据用户id 查询角色集合
     *
     */
    List<SysRoleDTO> selectRoleByUserId(Long userId);

    /**
     * 根据用户ID查询角色ID
     * @param userId
     */
    List<Long> selectRoleIdsByUserId(Long userId);

    /**
     * 根据用户ID和角色ID删除
     * @param userId
     * @param roleIds
     */
    void deleteByRoleIdsAndUserId(Long userId,List<Long> roleIds);

    /**
     * 根据角色ID获取用户ID集合
     * @param roleId
     * @return
     */
    List<Long> getUserIdByRoleId(Long roleId);
}
