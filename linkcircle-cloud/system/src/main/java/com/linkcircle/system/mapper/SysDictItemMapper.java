package com.linkcircle.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.system.dto.SysDictItemQueryDTO;
import com.linkcircle.system.entity.SysDictItem;
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
public interface SysDictItemMapper extends BaseMapper<SysDictItem> {

    /**
     * 分页查询
     */
    List<SysDictItem> query(Page page, @Param("query") SysDictItemQueryDTO queryForm);

    /**
     * 根据dictId删除
     * @param dictIds
     * @return
     */
    long batchDeleteByDictId(@Param("dictIds") List<Long> dictIds);
}
