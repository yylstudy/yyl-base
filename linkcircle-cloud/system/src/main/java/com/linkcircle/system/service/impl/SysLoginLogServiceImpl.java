package com.linkcircle.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.basecom.page.PageUtil;
import com.linkcircle.system.dto.SysLoginLogDTO;
import com.linkcircle.system.dto.SysLoginLogQueryDTO;
import com.linkcircle.system.entity.SysLoginLog;
import com.linkcircle.system.mapper.SysLoginLogMapper;
import com.linkcircle.system.service.SysLoginLogService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    @Resource
    private SysLoginLogMapper sysLoginLogMapper;

    /**
     * @description 分页查询
     */
    @Override
    public Result<PageResult<SysLoginLogDTO>> queryByPage(SysLoginLogQueryDTO queryForm) {
        Page page = PageUtil.convert2PageQuery(queryForm);
        List<SysLoginLogDTO> logList = sysLoginLogMapper.queryByPage(page, queryForm);
        PageResult<SysLoginLogDTO> pageResult = PageUtil.convert2PageResult(page, logList);
        return Result.ok(pageResult);
    }

}
