package com.linkcircle.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.system.dto.SysRoleAddDTO;
import com.linkcircle.system.dto.SysRoleDTO;
import com.linkcircle.system.dto.SysRoleUpdateDTO;
import com.linkcircle.system.entity.SysRole;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/1 14:47
 */

public interface SysRoleService extends IService<SysRole> {
    /**
     * 新增
     */
    Result<String> add(SysRoleAddDTO dto);
    /**
     * 更新
     */
    Result<String> edit(SysRoleUpdateDTO dto);
    /**
     * 根据角色id 删除
     */
    @Transactional(rollbackFor = Exception.class)
    Result<String> deleteRole(Long roleId);
    /**
     * 根据id获取角色数据
     */
    Result<SysRoleDTO> getRoleById(Long roleId);
    /**
     * 获取所有角色列表
     */
    Result<List<SysRoleDTO>> getAllRole();

    SysRole getSysRoleByRoleCode(String roleCode);

}
