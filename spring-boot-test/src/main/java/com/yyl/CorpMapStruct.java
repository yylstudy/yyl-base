package com.yyl;

import org.mapstruct.Mapper;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/2 22:04
 */
@Mapper(componentModel = "spring")
public interface CorpMapStruct {
    DownloadData convert(DownloadData dto);
}
