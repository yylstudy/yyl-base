package com.cqt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.number.entity.NumberInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author linshiqiang
 * date:  2023-07-25 14:34
 */
@Mapper
public interface NumberInfoMapper extends BaseMapper<NumberInfo> {

    /**
     * 查询来电号码白名单
     *
     * @param companyCode  企业号码
     * @param callerNumber 来电号码
     * @return 用户等级
     */
    @Select("select level from tx_company_custom_white_list where tenant_id = #{companyCode} and phone = #{callerNumber}")
    Integer getClientPriority(String companyCode, String callerNumber);
}
