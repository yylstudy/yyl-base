package com.linkcircle.system.mapstruct;

import com.linkcircle.system.dto.*;
import com.linkcircle.system.dto.SysDepartDTO;
import com.linkcircle.system.dto.SysDepartTreeDTO;
import com.linkcircle.system.entity.SysDepart;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/1 9:39
 */
@Mapper(componentModel = "spring")
public interface SysDepartMapStruct {
    SysDepart convert(SysDepartAddDTO dto);
    SysDepart convert(SysDepartUpdateDTO dto);
    SysDepartTreeDTO convert(SysDepartDTO dto);
    List<SysDepartTreeDTO> convert(List<SysDepartDTO> sysMenuList);
}
