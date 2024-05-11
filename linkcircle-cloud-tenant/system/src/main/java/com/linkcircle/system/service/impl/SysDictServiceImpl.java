package com.linkcircle.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.basecom.common.DictModel;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.basecom.page.PageUtil;
import com.linkcircle.basecom.util.JsonUtil;
import com.linkcircle.redis.config.RedisUtil;
import com.linkcircle.system.common.CommonConstant;
import com.linkcircle.system.common.RedisKeyFormat;
import com.linkcircle.system.dto.*;
import com.linkcircle.system.entity.SysDict;
import com.linkcircle.system.entity.SysDictItem;
import com.linkcircle.system.mapper.SysDictItemMapper;
import com.linkcircle.system.mapper.SysDictMapper;
import com.linkcircle.system.mapstruct.SysDictMapStruct;
import com.linkcircle.system.service.SysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description: 这里可以使用spring cache的相关注解，但是为了防止RedsiCacheManager的序列化器和RedisTemplate不一致，
 *               还是都是用RedisTemplate去操作吧
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {
    @Autowired
    private SysDictMapStruct sysDictMapStruct;
    @Resource
    private SysDictMapper sysDictMapper;
    @Resource
    private SysDictItemMapper sysDictItemMapper;
    @Autowired
    private RedisUtil redisUtil;
    /**
     * 新增
     * @return
     */
    @Override
    public Result<String> add(SysDictAddDTO dto) {
        SysDict existsSysDict = getSysDictByCode(dto.getDictCode());
        if (existsSysDict != null) {
            return Result.error("编码已存在");
        }
        SysDict sysDict = sysDictMapStruct.convert(dto);
        save(sysDict);
        redisUtil.del(RedisKeyFormat.getDictKey(dto.getDictCode()));
        return Result.ok();
    }

    /**
     * 根据编码获取配置
     * @param dictCode
     * @return
     */
    @Override
    public SysDict getSysDictByCode(String dictCode){
        LambdaQueryWrapper<SysDict> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDict::getDictCode,dictCode);
        return this.getOne(wrapper);
    }

    /**
     * 配置项新增
     * @return
     */
    @Override
    public Result<String> itemAdd(String dictCode, SysDictItemAddDTO dto) {
        SysDictItem existsSysDictItem = getSysDictItem(dto.getDictId(),dto.getItemValue());
        if (existsSysDictItem != null) {
            return Result.error("编码已存在");
        }
        SysDictItem sysDictItem = sysDictMapStruct.convert(dto);
        sysDictItemMapper.insert(sysDictItem);
        redisUtil.del(RedisKeyFormat.getDictKey(dictCode));
        return Result.ok();
    }

    /**
     * 获取配置项
     * @param dictId
     * @param itemValue
     * @return
     */
    @Override
    public SysDictItem getSysDictItem(long dictId,String itemValue){
        LambdaQueryWrapper<SysDictItem> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDictItem::getDictId,dictId);
        wrapper.eq(SysDictItem::getItemValue,itemValue);
        SysDictItem existsSysDictItem = sysDictItemMapper.selectOne(wrapper);
        return existsSysDictItem;
    }

    /**
     * 编辑
     * @return
     */
    @Override
    public Result<String> edit(SysDictUpdateDTO dto) {
        SysDict sysDict = getSysDictByCode(dto.getDictCode());
        if (sysDict != null && !sysDict.getId().equals(dto.getId())) {
            return Result.error("编码已存在");
        }
        SysDict dictKeyUpdateEntity = sysDictMapStruct.convert(dto);
        sysDictMapper.updateById(dictKeyUpdateEntity);
        redisUtil.del(RedisKeyFormat.getDictKey(dto.getDictCode()));
        return Result.ok();
    }

    /**
     * 配置项修改
     * @return
     */
    @Override
    public Result<String> itemEdit(String dictCode, SysDictItemUpdateDTO dto) {
        SysDictItem existsSysDictItem = getSysDictItem(dto.getDictId(),dto.getItemValue());
        if (existsSysDictItem != null && !existsSysDictItem.getId().equals(existsSysDictItem.getId())) {
            return Result.error("编码已存在");
        }
        SysDictItem dictValueUpdateEntity = sysDictMapStruct.convert(dto);
        sysDictItemMapper.updateById(dictValueUpdateEntity);
        redisUtil.del(RedisKeyFormat.getDictKey(dictCode));
        return Result.ok();
    }

    /**
     * 删除
     * @param idList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> delete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return Result.ok();
        }
        String[] delKeys = this.listByIds(idList).stream().map(dict->RedisKeyFormat.getDictKey(dict.getDictCode())).toArray(String[]::new);
        removeByIds(idList);
        sysDictItemMapper.batchDeleteByDictId(idList);
        redisUtil.del(delKeys);
        return Result.ok();
    }

    /**
     * 配置项删除
     * @return
     */
    @Override
    public Result<String> itemDelete(String dictCode,List<Long> itemIdList) {
        sysDictItemMapper.deleteBatchIds(itemIdList);
        redisUtil.del(RedisKeyFormat.getDictKey(dictCode));
        return Result.ok();
    }

    /**
     * 查询
     * @return
     */
    @Override
    public Result<PageResult<SysDict>> query(SysDictQueryDTO dto) {
        Page<?> page = PageUtil.convert2PageQuery(dto);
        List<SysDict> list = sysDictMapper.query(page, dto);
        PageResult<SysDict> pageResult = PageUtil.convert2PageResult(page, list);
        if (pageResult.getEmptyFlag()) {
            return Result.ok(pageResult);
        }
        return Result.ok(pageResult);
    }
    /**
     * 配置项查询
     * @param queryForm
     * @return
     */
    @Override
    public Result<PageResult<SysDictItem>> itemValue(SysDictItemQueryDTO queryForm) {
        Page<?> page = PageUtil.convert2PageQuery(queryForm);
        List<SysDictItem> list = sysDictItemMapper.query(page, queryForm);
        PageResult<SysDictItem> pageResult = PageUtil.convert2PageResult(page, list);
        if (pageResult.getEmptyFlag()) {
            return Result.ok(pageResult);
        }
        return Result.ok(pageResult);
    }
    @Override
    public List<DictModel> getDictItemByDictCode(String dictCode) {
        String cacheKey = RedisKeyFormat.getDictKey(dictCode);
        try{
            String valueStr = (String)redisUtil.get(cacheKey);
            if(StringUtils.hasText(valueStr)){
                return JsonUtil.parseList(valueStr,DictModel.class);
            }
            List<SysDictItem> list = sysDictMapper.getDictItemByDictCode(dictCode);
            List<DictModel> dtoList = sysDictMapStruct.convert(list);
            String str = JsonUtil.toJSONString(dtoList);
            redisUtil.set(RedisKeyFormat.getDictKey(dictCode),str, CommonConstant.DICT_EXPIRE_SECOND);
            return dtoList;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}