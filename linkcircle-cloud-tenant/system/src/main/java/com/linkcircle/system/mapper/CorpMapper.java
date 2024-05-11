package com.linkcircle.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.system.dto.CorpQueryDTO;
import com.linkcircle.system.dto.CorpResDTO;
import com.linkcircle.system.entity.Corp;
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
public interface CorpMapper extends BaseMapper<Corp> {
    /**
     * 分页查询
     */
    List<CorpResDTO> queryByPage(Page page, @Param("query") CorpQueryDTO queryForm);
}
