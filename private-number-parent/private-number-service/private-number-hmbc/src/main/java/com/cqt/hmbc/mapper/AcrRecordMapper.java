package com.cqt.hmbc.mapper;

import com.cqt.model.hmbc.dto.AcrRecordQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * AcrRecordMapper
 *
 * @author Xienx
 * @date 2023年02月08日 10:28
 */
@Mapper
public interface AcrRecordMapper {

    /**
     * 查询数据库中话单是否存在
     *
     * @param acrRecordQueryDTO 查询条件
     * @return Long 符合条件的话单数量
     */
    Long acrQuery(@Param("query") AcrRecordQueryDTO acrRecordQueryDTO);

    /**
     * 查询号码对应的GT编码
     *
     * @param number 号码信息
     * @return String gt编码
     */
    @Select(" SELECT gt_code FROM private_number_info WHERE number = #{number} LIMIT 1 ")
    String getGtCodeByNumber(String number);
}
