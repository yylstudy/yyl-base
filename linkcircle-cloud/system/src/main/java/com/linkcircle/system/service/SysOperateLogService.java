package com.linkcircle.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.system.dto.SysOperateLogQueryDto;
import com.linkcircle.system.entity.SysOperateLog;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/4 10:49
 */

public interface SysOperateLogService  extends IService<SysOperateLog> {
    /**
     * @description 分页查询
     */
    Result<PageResult<SysOperateLog>> queryByPage(SysOperateLogQueryDto dto);
}
