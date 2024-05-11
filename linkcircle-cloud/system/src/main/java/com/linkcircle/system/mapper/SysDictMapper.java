package com.linkcircle.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.system.dto.SysDictQueryDTO;
import com.linkcircle.system.entity.SysDict;
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
public interface SysDictMapper extends BaseMapper<SysDict> {

    /**
     * 分页查询
     */
    List<SysDict> query(Page page, @Param("dto") SysDictQueryDTO dto);

    List<SysDictItem> getDictItemByDictCode(@Param("dictCode") String dictCode);

}
