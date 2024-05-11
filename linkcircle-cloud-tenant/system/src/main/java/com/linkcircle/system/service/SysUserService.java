package com.linkcircle.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.system.dto.*;
import com.linkcircle.system.entity.SysUser;

import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/1 13:10
 */

public interface SysUserService extends IService<SysUser> {
    /**
     * 查询用户列表
     *
     */
    Result<PageResult<SysUserDTO>> query(SysUserQueryReqDTO sysUserQueryReqDto);

    /**
     * 查询不在角色下的用户
     */
    Result<PageResult<SysUserDTO>> queryNotInRoleUserByRoleId(SysRoleUserQueryReqDTO dto);

    /**
     * 新增用户
     *
     */
    Result<String> add(SysUserAddDTO sysUserAddDto);
    /**
     * 更新
     */
    Result<String> edit(SysUserUpdateDTO sysUserUpdateDto);
    /**
     * 更新禁用/启用状态
     */
    Result<String> updateDisableFlag(Long id);
    /**
     * 批量删除用户
     */
    Result<String> batchUpdateDeleteFlag(List<Long> idList);
    /**
     * 批量更新部门
     */
    Result<String> batchUpdateDepart(SysUserBatchUpdateDepartDTO batchUpdateDepartmentForm);
    /**
     * 更新密码
     */
    Result<String> updatePassword(SysUserUpdatePasswordDTO updatePasswordDto);
    /**
     * 重置密码
     */
    Result<String> resetPassword(Long id);
    /**
     * 查询全部用户
     *
     */
    Result<List<SysUserDTO>> queryAllUser(Boolean disabledFlag);


    /**
     * 根据手机号查询
     * @param phoneOrEmail
     * @return
     */
    SysUser getByPhoneOrEmail(String phoneOrEmail);

    /**
     * 根据手机号查询
     * @param phone
     * @return
     */
    SysUser getByPhone(String phone);
    /**
     * 根据邮箱查询
     * @param email
     * @return
     */
    SysUser getByEmail(String email);
        
}
