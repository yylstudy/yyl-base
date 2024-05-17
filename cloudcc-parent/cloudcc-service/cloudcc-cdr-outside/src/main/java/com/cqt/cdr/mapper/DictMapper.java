package com.cqt.cdr.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.cqt.cdr.entity.Dict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Administrator
* @description 针对表【sys_dict】的数据库操作Mapper
* @createDate 2023-08-30 10:10:41
* @Entity com.cqt.cdr.entity.Dict
*/
@Mapper
@DS("ms")
public interface DictMapper extends BaseMapper<Dict> {

}




