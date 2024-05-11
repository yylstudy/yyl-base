package com.linkcircle.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.system.entity.SysOperateLog;
import com.linkcircle.system.dto.SysOperateLogQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Mapper
public interface SysOperateLogMapper extends BaseMapper<SysOperateLog> {

    /**
     * 分页查询
     * @param page
     * @param queryForm
     */
    List<SysOperateLog> queryByPage(Page page, @Param("query") SysOperateLogQueryDTO queryForm);


}
