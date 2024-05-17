package com.cqt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.skill.entity.SkillInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author Xienx
 * date 2023-07-28 10:43:10:43
 */
@Mapper
public interface SkillInfoMapper extends BaseMapper<SkillInfo> {

    /**
     * 根据文件id查询文件目录
     *
     * @param fileId 文件id
     * @return 文件目录
     */
    @Select("select file_path from tx_file where file_id = #{fileId}")
    String getFilePath(String fileId);

    /**
     * 查询来电号码白名单
     *
     * @param companyCode  企业号码
     * @param callerNumber 来电号码
     * @return 用户等级
     */
    @Select("select level from tx_company_custom_white_list where tenant_id = #{companyCode} and phone = #{callerNumber}")
    Integer getUserLevel(String companyCode, String callerNumber);
}
