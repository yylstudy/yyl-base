package com.linkcircle.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.basecom.page.PageUtil;
import com.linkcircle.system.dto.SysOperateLogQueryDTO;
import com.linkcircle.system.entity.SysOperateLog;
import com.linkcircle.system.mapper.SysOperateLogMapper;
import com.linkcircle.system.service.SysOperateLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Service
public class SysOperateLogServiceImpl extends ServiceImpl<SysOperateLogMapper, SysOperateLog> implements SysOperateLogService {

    @Resource
    private SysOperateLogMapper sysOperateLogMapper;

    /**
     * @description 分页查询
     */
    @Override
    public Result<PageResult<SysOperateLog>> queryByPage(SysOperateLogQueryDTO dto) {
        Page page = PageUtil.convert2PageQuery(dto);
        List<SysOperateLog> logEntityList = sysOperateLogMapper.queryByPage(page, dto);
        PageResult<SysOperateLog> pageResult = PageUtil.convert2PageResult(page, logEntityList);
        return Result.ok(pageResult);
    }

}
