package com.cqt.cdr.cloudccsfaftersales.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.cqt.cdr.cloudccsfaftersales.entity.DictItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Administrator
* @description 针对表【sys_dict_item】的数据库操作Mapper
* @createDate 2023-09-06 19:17:55
* @Entity com.cqt.cdr.entity.DictItem
*/
@Mapper
@DS("ms")
public interface DictItemMapper extends BaseMapper<DictItem> {

}




