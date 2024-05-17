package com.cqt.broadnet.web.x.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.numpool.entity.HCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author linshiqiang
 * date:  2023-02-17 9:51
 */
@Mapper
public interface TelCodeMapper extends BaseMapper<HCode> {

    @Select("select areacode from t_hcode where telcode = #{telCode} limit 1")
    String getAreaCodeByTelCode(String telCode);
}
