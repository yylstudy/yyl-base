package com.linkcircle.system.mapstruct;

import com.linkcircle.system.dto.SysLoginResDTO;
import com.linkcircle.system.dto.SysUserAddDTO;
import com.linkcircle.system.dto.SysUserUpdateDTO;
import com.linkcircle.system.entity.SysUser;
import com.linkcircle.system.common.SystemLoginUserInfo;
import org.mapstruct.Mapper;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/1 9:20
 */
@Mapper(componentModel = "spring")
public interface SysUserMapStruct {
    SysLoginResDTO convert(SystemLoginUserInfo systemLoginUserInfo);
    SysUser convert(SysUserAddDTO sysUserAddDto);
    SysUser convert(SysUserUpdateDTO sysUserUpdateDto);
    SystemLoginUserInfo convert(SysUser sysUser);
}
