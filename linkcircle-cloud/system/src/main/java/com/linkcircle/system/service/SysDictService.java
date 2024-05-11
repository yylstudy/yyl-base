package com.linkcircle.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.basecom.common.DictModel;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.system.dto.*;
import com.linkcircle.system.entity.SysDict;
import com.linkcircle.system.entity.SysDictItem;

import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/2 22:20
 */

public interface SysDictService extends IService<SysDict> {
    /**
     * 新增
     * @param dto
     * @return
     */
    Result<String> add(SysDictAddDTO dto);

    /**
     * 根据编码获取配置
     * @param dictCode
     * @return
     */
    SysDict getSysDictByCode(String dictCode);

    /**
     * 配置项新增
     * @param dto
     * @return
     */
    Result<String> itemAdd(String dictCode, SysDictItemAddDTO dto);

    /**
     * 获取配置项
     * @param dictId
     * @param itemValue
     * @return
     */
    SysDictItem getSysDictItem(long dictId, String itemValue);
    /**
     * 编辑
     * @param dto
     * @return
     */
    Result<String> edit(SysDictUpdateDTO dto);

    /**
     * 配置项修改
     * @param dto
     * @return
     */
    Result<String> itemEdit(String dictCode, SysDictItemUpdateDTO dto);

    /**
     * 删除
     * @param idList
     * @return
     */
    Result<String> delete(List<Long> idList);

    /**
     * 配置项删除
     * @param itemIdList
     * @return
     */
    Result<String> itemDelete(String dictCode,List<Long> itemIdList);

    /**
     * 查询
     * @param dto
     * @return
     */
    Result<PageResult<SysDict>> query(SysDictQueryDTO dto);

    /**
     * 配置项查询
     * @param queryForm
     * @return
     */
    Result<PageResult<SysDictItem>> itemValue(SysDictItemQueryDTO queryForm);

    /**
     * 根据dictCode获取DictItem
     */
    List<DictModel> getDictItemByDictCode(String dictCode);
}
