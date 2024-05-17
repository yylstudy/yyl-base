package com.cqt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.company.entity.PlatformConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author linshiqiang
 * date:  2023-11-18 15:46
 * 平台默认配置
 */
@Mapper
public interface PlatformConfigMapper extends BaseMapper<PlatformConfig> {

    @Select("select config_value from cloudcc_platform_config where config_code = #{configCode}")
    String getDefaultToneId(String configCode);
}
