package com.cqt.vccidhmyc.web.manager;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 隐私号号码信息表(PrivateNumberInfo)表数据库访问层
 *
 * @author makejava
 * @since 2022-05-16 19:25:19
 */
@Mapper
public interface PrivateNumberInfoMapper extends BaseMapper<PrivateNumberInfo> {

}

