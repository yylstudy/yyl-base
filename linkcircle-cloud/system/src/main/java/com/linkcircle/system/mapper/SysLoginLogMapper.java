package com.linkcircle.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.system.entity.SysLoginLog;
import com.linkcircle.system.dto.SysLoginLogQueryDTO;
import com.linkcircle.system.dto.SysLoginLogDTO;
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
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {

    /**
     * 分页查询
     * @param page
     * @param queryForm
     * @return LoginLogVO
     */
    List<SysLoginLogDTO> queryByPage(Page page, @Param("query") SysLoginLogQueryDTO queryForm);

}
