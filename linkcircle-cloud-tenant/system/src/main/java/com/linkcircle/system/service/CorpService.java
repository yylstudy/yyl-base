package com.linkcircle.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.system.dto.CorpAddDTO;
import com.linkcircle.system.dto.CorpQueryDTO;
import com.linkcircle.system.dto.CorpResDTO;
import com.linkcircle.system.dto.CorpUpdateDTO;
import com.linkcircle.system.entity.Corp;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/2 22:31
 */

public interface CorpService extends IService<Corp> {
    /**
     * 分页查询系统配置
     */
    Result<PageResult<CorpResDTO>> query(CorpQueryDTO dto);
    /**
     * 添加系统配置
     *
     */
    Result<String> add(CorpAddDTO dto);
    /**
     * 更新系统配置
     *
     */
    Result<String> edit(CorpUpdateDTO dto);

    /**
     * 删除
     */
    Result<String> delete(String id);
}
