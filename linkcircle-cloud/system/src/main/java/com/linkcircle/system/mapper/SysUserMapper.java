package com.linkcircle.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.system.dto.SysRoleUserQueryReqDTO;
import com.linkcircle.system.entity.SysUser;
import com.linkcircle.system.dto.SysUserQueryReqDTO;
import com.linkcircle.system.dto.SysUserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * 用户列表
     */
    List<SysUserDTO> querySysUser(Page page, @Param("queryForm") SysUserQueryReqDTO queryForm, @Param("departmentIdList") List<Long> departmentIdList);

    /**
     * 查询用户
     */
    List<SysUserDTO> selectUserByDisabledAndDeleted(@Param("disabledFlag") Boolean disabledFlag, @Param("deletedFlag") Boolean deletedFlag);

    /**
     * 用户列表
     */
    List<SysUserDTO> queryNotInRoleUserByRoleId(Page page, @Param("queryForm") SysRoleUserQueryReqDTO queryForm);

}