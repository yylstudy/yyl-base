package com.linkcircle.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.system.dto.SysUserRoleDTO;
import com.linkcircle.system.dto.SysRoleDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.linkcircle.system.dto.SysUserDTO;
import com.linkcircle.system.entity.SysUserRole;
import com.linkcircle.system.dto.SysUserRoleQueryDTO;

import java.util.List;


@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 根据用户ID查询所有的角色
     */
    List<SysRoleDTO> selectRoleByUserId(@Param("userId") Long userId);

    /**
     * 根据用户id 查询所有的角色
     */
    List<SysUserRoleDTO> selectRoleByUserIdList(@Param("userIdList") List<Long> userIdList);

    /**
     * 获取用户
     */
    List<SysUserDTO> selectSysUser(Page page, @Param("queryForm") SysUserRoleQueryDTO sysUserRoleQueryDto);

    /**
     * 根据roleId获取用户
     */
    List<SysUserDTO> getUserByRoleId(@Param("roleId") Long roleId);
}
