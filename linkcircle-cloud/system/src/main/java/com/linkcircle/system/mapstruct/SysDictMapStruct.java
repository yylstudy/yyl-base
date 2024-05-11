package com.linkcircle.system.mapstruct;

import com.linkcircle.basecom.common.DictModel;
import com.linkcircle.system.dto.SysDictAddDTO;
import com.linkcircle.system.dto.SysDictItemAddDTO;
import com.linkcircle.system.dto.SysDictItemUpdateDTO;
import com.linkcircle.system.dto.SysDictUpdateDTO;
import com.linkcircle.system.entity.SysDict;
import com.linkcircle.system.entity.SysDictItem;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/2 22:04
 */
@Mapper(componentModel = "spring")
public interface SysDictMapStruct {
    SysDict convert(SysDictAddDTO dto);
    SysDict convert(SysDictUpdateDTO dto);
    SysDictItem convert(SysDictItemAddDTO dto);
    SysDictItem convert(SysDictItemUpdateDTO dto);
    DictModel convert(SysDictItem item);
    List<DictModel> convert(List<SysDictItem> items);
}
