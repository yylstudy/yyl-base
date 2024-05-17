package com.cqt.hmbc.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.hmbc.dto.DialTestNumberDTO;
import com.cqt.model.hmbc.dto.DialTestNumberQueryDTO;
import com.cqt.model.hmbc.entity.PrivateDialTestNumberConf;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 定时拨测任务号码配置
 *
 * @author jeecg-boot
 * @date 2022-07-07
 * @since V2.1.0
 */
@Mapper
public interface PrivateDialTestNumberConfMapper extends BaseMapper<PrivateDialTestNumberConf> {

    /**
     * 查询出企业已经配置的号码
     *
     * @param queryDTO 查询条件
     * @return List<DialTestNumberDTO> 号码列表信息
     */
    List<DialTestNumberDTO> findList4Config(@Param("query") DialTestNumberQueryDTO queryDTO);

    /**
     * 查询出企业所有的号码
     *
     * @param queryDTO 查询条件
     * @return List<DialTestNumberDTO> 号码列表信息
     */
    List<DialTestNumberDTO> findList4All(@Param("query") DialTestNumberQueryDTO queryDTO);
}
