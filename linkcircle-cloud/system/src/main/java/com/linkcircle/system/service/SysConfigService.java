package com.linkcircle.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.system.dto.SysConfigAddDTO;
import com.linkcircle.system.dto.SysConfigQueryDTO;
import com.linkcircle.system.entity.SysConfig;

import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/2 22:31
 */

public interface SysConfigService extends IService<SysConfig> {
    /**
     * 分页查询系统配置
     */
    Result<PageResult<SysConfig>> query(SysConfigQueryDTO queryForm);
    /**
     * 查询配置缓存
     *
     */
    SysConfig getConfig(String key);
    /**
     * 添加系统配置
     *
     */
    Result<String> add(SysConfigAddDTO dto);
    /**
     * 更新系统配置
     *
     */
    Result<String> edit(SysConfig sysConfig);

    /**
     * 删除
     */
    Result<String> batchDelete(List<Long> idList);
}
