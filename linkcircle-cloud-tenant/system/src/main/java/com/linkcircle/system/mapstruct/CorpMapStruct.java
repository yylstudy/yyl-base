package com.linkcircle.system.mapstruct;

import com.linkcircle.system.dto.CorpAddDTO;
import com.linkcircle.system.dto.CorpUpdateDTO;
import com.linkcircle.system.entity.Corp;
import org.mapstruct.Mapper;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/2 22:04
 */
@Mapper(componentModel = "spring")
public interface CorpMapStruct {
    Corp convert(CorpAddDTO dto);
    Corp convert(CorpUpdateDTO dto);
}
