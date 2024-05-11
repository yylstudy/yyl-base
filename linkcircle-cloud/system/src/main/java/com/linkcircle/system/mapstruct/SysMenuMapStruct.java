package com.linkcircle.system.mapstruct;

import com.linkcircle.system.dto.*;
import com.linkcircle.system.entity.SysMenu;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/1 9:39
 */
@Mapper(componentModel = "spring")
public interface SysMenuMapStruct {
    List<SysMenuDTO> convert(List<SysMenu> sysMenuList);
    SysMenuDTO convert(SysMenu sysMenu);
    SysMenu convert(SysMenuAddDTO dto);
    SysMenu convert(SysMenuUpdateDTO dto);
    SysMenuTreeDTO convert(SysMenuDTO dto);
    SysMenuSimpleTreeDTO convertToSimpleTreeDto(SysMenuDTO dto);
}
