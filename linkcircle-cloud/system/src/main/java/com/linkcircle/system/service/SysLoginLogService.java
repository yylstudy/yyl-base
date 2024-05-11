package com.linkcircle.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.system.dto.SysLoginLogQueryDTO;
import com.linkcircle.system.dto.SysLoginLogDTO;
import com.linkcircle.system.entity.SysLoginLog;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/4 10:54
 */

public interface SysLoginLogService extends IService<SysLoginLog> {
    /**
     * @description 分页查询
     */
    Result<PageResult<SysLoginLogDTO>> queryByPage(SysLoginLogQueryDTO queryForm);


}
