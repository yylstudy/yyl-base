package com.linkcircle.system.controller;

import com.linkcircle.basecom.annotation.OperateLog;
import com.linkcircle.basecom.common.DictModel;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.system.dto.*;
import com.linkcircle.system.entity.SysDict;
import com.linkcircle.system.entity.SysDictItem;
import com.linkcircle.system.mapstruct.SysDictMapStruct;
import com.linkcircle.system.service.SysDictItemService;
import com.linkcircle.system.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Tag(name = "数据字典")
@RestController
@RequestMapping("dict")
public class SysDictController {
    @Autowired
    private SysDictService sysDictService;
    @Autowired
    private SysDictItemService sysDictItemService;

    @Operation(summary = "分页查询数据字典KEY")
    @PostMapping("query")
    public Result<PageResult<SysDict>> query(@Valid @RequestBody SysDictQueryDTO dto) {
        return sysDictService.query(dto);
    }

    @Operation(summary = "新增")
    @PostMapping("add")
    @OperateLog(content = "数据字典新增")
    public Result<String> add(@Valid @RequestBody SysDictAddDTO dto) {
        return sysDictService.add(dto);
    }

    @Operation(summary = "字典项新增")
    @PostMapping("itemAdd")
    @OperateLog(content = "数据字典项新增")
    public Result<String> itemAdd(@Valid  @RequestBody SysDictItemAddDTO dto) {
        SysDict sysDict = sysDictService.getById(dto.getDictId());
        return sysDictService.itemAdd(sysDict.getDictCode(),dto);
    }

    @Operation(summary = "字典修改")
    @PostMapping("edit")
    @OperateLog(content = "数据字典修改")
    public Result<String> edit(@Valid @RequestBody SysDictUpdateDTO dto) {
        return sysDictService.edit(dto);
    }

    @Operation(summary = "字典项修改")
    @PostMapping("itemEdit")
    @OperateLog(content = "数据字典项修改")
    public Result<String> itemEdit(@Valid @RequestBody SysDictItemUpdateDTO valueUpdateForm) {
        SysDict sysDict = sysDictService.getById(valueUpdateForm.getDictId());
        return sysDictService.itemEdit(sysDict.getDictCode(),valueUpdateForm);
    }

    @Operation(summary = "字典删除")
    @PostMapping("delete")
    @OperateLog(content = "数据字典删除")
    public Result<String> delete(@Valid @RequestBody List<Long> idList) {
        return sysDictService.delete(idList);
    }

    @Operation(summary = "字典项删除")
    @PostMapping("itemDelete")
    @OperateLog(content = "数据字典项删除")
    public Result<String> itemDelete(@Valid @RequestBody List<Long> itemIdList) {
        if (CollectionUtils.isEmpty(itemIdList)) {
            return Result.ok();
        }
        SysDictItem sysDictItem = sysDictItemService.getById(itemIdList.get(0));
        Long dictId = sysDictItem.getDictId();
        SysDict sysDict = sysDictService.getById(dictId);
        return sysDictService.itemDelete(sysDict.getDictCode(),itemIdList);
    }

    @Operation(summary = "查询全部字典")
    @GetMapping("queryAll")
    public Result<List<SysDict>> queryAll() {
        return Result.ok(sysDictService.list());
    }

    @Operation(summary = "分页查询数据字典项")
    @PostMapping("item/query")
    public Result<PageResult<SysDictItem>> itemValue(@Valid @RequestBody SysDictItemQueryDTO dto) {
        return sysDictService.itemValue(dto);
    }

    @Operation(summary = "根据dictCode查询字典项")
    @GetMapping("getItemByDictCode")
    public Result<List<DictModel>> getItemByDictCode(@RequestParam("dictCode") String dictCode) {
        List<DictModel> list = sysDictService.getDictItemByDictCode(dictCode);
        return Result.ok(list);
    }

}
