package com.cqt.hmyc.web.corpinfo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.corpinfo.dto.ExpireTimeDTO;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    @Select("select expire_start_time, expire_end_time from private_corp_info where vcc_id = #{vccId}")
    ExpireTimeDTO selectExpireTime(@Param("vccId") String vccId);

}
