package com.linkcircle.system.mapstruct;

import com.linkcircle.system.dto.SysConfigAddDTO;
import com.linkcircle.system.entity.SysConfig;
import org.mapstruct.Mapper;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/2 22:04
 */
@Mapper(componentModel = "spring")
public interface SysConfigMapStruct {
    SysConfig convert(SysConfigAddDTO dto);
}
