package com.cqt.recycle.web.numpool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.numpool.dto.NumberPoolQueryDTO;
import com.cqt.model.numpool.entity.PrivateCorpNumberPool;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.model.numpool.vo.NumberPoolVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author linshiqiang
 * @date 2022/6/20 16:47
 */
@Mapper
public interface PrivateCorpNumberPoolMapper extends BaseMapper<PrivateCorpNumberPool> {

    @Select("select DISTINCT area_code from private_corp_number_pool where business_type = 'AXB'")
    List<String> selectAreaCode();

    @Select("select master_num from private_corp_number_pool where vcc_id = #{vccId} and area_code= #{areaCode} and business_type = 'AXB' limit 1")
    Integer getMasterNum(@Param("vccId") String vccId, @Param("areaCode") String areaCode);

    @Select("select slave_num from private_corp_number_pool where vcc_id = #{vccId} and area_code= #{areaCode} and business_type = 'AXB' limit 1")
    Integer getSlaveNum(@Param("vccId") String vccId, @Param("areaCode") String areaCode);

    List<PrivateNumberInfo> queryNumberPool(@Param("vccId") String vccId, @Param("query") NumberPoolQueryDTO queryDTO);

    List<NumberPoolVO> queryNumberAreaTotal(@Param("vccId") String vccId, @Param("query") NumberPoolQueryDTO queryDTO);
}
