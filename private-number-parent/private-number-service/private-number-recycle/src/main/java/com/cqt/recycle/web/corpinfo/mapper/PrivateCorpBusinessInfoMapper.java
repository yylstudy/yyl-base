package com.cqt.recycle.web.corpinfo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.corpinfo.dto.CorpInfoVO;
import com.cqt.model.corpinfo.dto.ExpireTimeDTO;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.model.corpinfo.entity.PrivateCorpSuppliers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author linshiqiang
 * @date 2022/5/25 9:35
 * 企业业务配置
 */
@Mapper
public interface PrivateCorpBusinessInfoMapper extends BaseMapper<PrivateCorpBusinessInfo> {

    /**
     * 查询企业的有效期
     *
     * @param vccId 企业id
     * @return 有效期
     */
    @Select("select expire_start_time, expire_end_time, vcc_name from private_corp_info where vcc_id = #{vccId}")
    ExpireTimeDTO selectExpireTime(@Param("vccId") String vccId);


    /**
     * 查詢企业的使用供应商的地市编码
     *
     * @param vccId 企业id
     * @return 列表
     */
    @Select("select  city, GROUP_CONCAT(supplier_id) supplierId  from private_corp_suppliers where vcc_id = #{vccId} group by city")
    List<PrivateCorpSuppliers> selectCorpSuppliers(@Param("vccId") String vccId);

    @Select("select t0.vcc_id, t0.vcc_name from private_corp_info t0\n" +
            "left join private_corp_business_info t1 on t0.vcc_id = t1.vcc_id\n" +
            "where t1.business_type is not null;")
    List<CorpInfoVO> getStatsCorpInfo();
}
