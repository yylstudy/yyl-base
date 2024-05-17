package com.cqt.cdr.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.cdr.entity.CallCenterSubCdr;
import org.apache.ibatis.annotations.Mapper;


/**
 *
 * @author ld
 * @since 2023-08-15
 */
@Mapper
@DS("cdr")
public interface SubCdrMapper extends BaseMapper<CallCenterSubCdr> {

}
