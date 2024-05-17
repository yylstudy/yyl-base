package com.cqt.cdr.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.cqt.cdr.entity.AcrRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Administrator
 * @description 针对表【clouccc_acr_record】的数据库操作Mapper
 * @createDate 2023-08-21 15:26:31
 * @Entity com.cqt.cdr.entity.AcrRecord
 */
@Mapper
@DS("smp")
public interface AcrRecordMapper extends BaseMapper<AcrRecord> {

}




