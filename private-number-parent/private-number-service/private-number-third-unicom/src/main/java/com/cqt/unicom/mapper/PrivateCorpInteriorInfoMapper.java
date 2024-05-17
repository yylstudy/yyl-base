package com.cqt.unicom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.unicom.entity.PrivateCorpInteriorInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author zhengsuhao
 * @date 2022/12/19
 */
@Mapper
public interface PrivateCorpInteriorInfoMapper extends BaseMapper<PrivateCorpInteriorInfo> {

    /**
     * 根据VCCID查询数据库绑定用户URL
     *
     * @param vccId
     * @return PrivateCorpInteriorInfo
     */
    @Select("SELECT voice_cdr_url FROM private_corp_interior_info where vcc_id = #{vccId} and service_key = #{serviceKey} limit 1 ")
    String selectByVccId(@Param("vccId") String vccId, @Param("serviceKey") String serviceKey);

}