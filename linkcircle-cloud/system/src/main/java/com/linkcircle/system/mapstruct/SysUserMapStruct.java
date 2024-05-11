package com.linkcircle.system.mapstruct;

import com.linkcircle.basecom.common.LoginUserInfo;
import com.linkcircle.system.dto.SysLoginResDTO;
import com.linkcircle.system.dto.SysUserAddDTO;
import com.linkcircle.system.dto.SysUserUpdateDTO;
import com.linkcircle.system.entity.SysUser;
import org.mapstruct.Mapper;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/1 9:20
 */
@Mapper(componentModel = "spring")
public interface SysUserMapStruct {
    SysLoginResDTO convert(LoginUserInfo loginUserInfo);
    SysUser convert(SysUserAddDTO sysUserAddDto);
    SysUser convert(SysUserUpdateDTO sysUserUpdateDto);
    LoginUserInfo convert(SysUser sysUser);

}
