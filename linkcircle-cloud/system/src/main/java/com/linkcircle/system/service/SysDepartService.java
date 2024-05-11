package com.linkcircle.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.system.dto.SysDepartAddDTO;
import com.linkcircle.system.dto.SysDepartDTO;
import com.linkcircle.system.dto.SysDepartTreeDTO;
import com.linkcircle.system.dto.SysDepartUpdateDTO;
import com.linkcircle.system.entity.SysDepart;

import java.util.List;
import java.util.Map;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/1 16:53
 */

public interface SysDepartService extends IService<SysDepart> {
    /**
     * 部门列表
     */
    List<SysDepartDTO> listAll();
    /**
     * 获取部门树形结构
     */
    List<SysDepartTreeDTO> departTree();

    /**
     * 新增
     * @param sysDepartAddDto
     * @return
     */
    Result<String> add(SysDepartAddDTO sysDepartAddDto);

    /**
     * 修改
     * @param updateDTO
     * @return
     */
    Result<String> edit(SysDepartUpdateDTO updateDTO);

    /**
     * 删除
     * @param id
     * @return
     */
    void deleteDepartById(Long id);

    List<SysDepart> getByParentId(Long parentId);

    /**
     * 自身以及所有下级的部门id列表
     *
     */
    List<Long> selfAndChildrenIdList(Long departId);

    /**
     * 部门的路径名称
     *
     */
    Map<Long, String> getDepartmentPathMap();
}
