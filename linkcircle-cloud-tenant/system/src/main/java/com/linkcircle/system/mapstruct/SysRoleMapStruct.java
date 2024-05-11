package com.linkcircle.system.mapstruct;

import com.linkcircle.system.dto.SysRoleAddDTO;
import com.linkcircle.system.dto.SysRoleDTO;
import com.linkcircle.system.dto.SysRoleUpdateDTO;
import com.linkcircle.system.entity.SysRole;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/1 9:39
 */
@Mapper(componentModel = "spring")
public interface SysRoleMapStruct {
    SysRole convert(SysRoleAddDTO dto);
    SysRole convert(SysRoleUpdateDTO dto);
    SysRoleDTO convert(SysRole sysRole);
    List<SysRoleDTO> convert(List<SysRole> sysRoles);

}
