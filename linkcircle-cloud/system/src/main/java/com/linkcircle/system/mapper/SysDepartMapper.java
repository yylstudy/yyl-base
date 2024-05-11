package com.linkcircle.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linkcircle.system.dto.SysDepartDTO;
import com.linkcircle.system.entity.SysDepart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Mapper
public interface SysDepartMapper extends BaseMapper<SysDepart> {
    /**
     * 获取全部部门列表
     */
    List<SysDepartDTO> listAll();

}
