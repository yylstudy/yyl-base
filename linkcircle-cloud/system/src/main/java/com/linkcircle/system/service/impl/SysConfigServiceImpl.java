package com.linkcircle.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.basecom.page.PageUtil;
import com.linkcircle.system.dto.SysConfigAddDTO;
import com.linkcircle.system.dto.SysConfigQueryDTO;
import com.linkcircle.system.entity.SysConfig;
import com.linkcircle.system.mapper.SysConfigMapper;
import com.linkcircle.system.mapstruct.SysConfigMapStruct;
import com.linkcircle.system.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Slf4j
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {
    @Autowired
    private SysConfigMapStruct sysConfigMapStruct;
    @Resource
    private SysConfigMapper sysConfigMapper;
    /**
     * 分页查询系统配置
     */
    @Override
    public Result<PageResult<SysConfig>> query(SysConfigQueryDTO queryForm) {
        Page<?> page = PageUtil.convert2PageQuery(queryForm);
        List<SysConfig> entityList = sysConfigMapper.queryByPage(page, queryForm);
        PageResult<SysConfig> pageResult = PageUtil.convert2PageResult(page, entityList);
        return Result.ok(pageResult);
    }
    /**
     * 查询配置缓存
     */
    @Override
    public SysConfig getConfig(String key) {
        LambdaQueryWrapper<SysConfig> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysConfig::getKey,key);
        return this.getOne(wrapper);
    }

    /**
     * 添加系统配置
     *
     */
    @Override
    public Result<String> add(SysConfigAddDTO dto) {
        SysConfig existsSysConfig = getConfig(dto.getKey());
        if (existsSysConfig !=null) {
            return Result.error("编码已存在");
        }
        SysConfig sysConfig = sysConfigMapStruct.convert(dto);
        save(sysConfig);
        return Result.ok();
    }
    /**
     * 更新系统配置
     *
     */
    @Override
    public Result<String> edit(SysConfig sysConfig) {
        SysConfig alreadyEntity = getConfig(sysConfig.getKey());
        if (alreadyEntity != null && !Objects.equals(sysConfig.getId(), alreadyEntity.getId())) {
            return Result.error("key已存在");
        }
        updateById(sysConfig);
        return Result.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchDelete(List<Long> idList) {
        removeByIds(idList);
        return Result.ok();
    }
}
