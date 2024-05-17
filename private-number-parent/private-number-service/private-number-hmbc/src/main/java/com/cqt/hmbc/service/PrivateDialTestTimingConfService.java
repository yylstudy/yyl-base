package com.cqt.hmbc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqt.model.hmbc.dto.DialTestNumberDTO;
import com.cqt.model.hmbc.dto.DialTestNumberQueryDTO;
import com.cqt.model.hmbc.entity.PrivateDialTestTimingConf;

import java.util.List;

/**
 * 定时拨测任务配置管理
 *
 * @author jeecg-boot
 * @date 2022-07-07
 * @since V2.1.0
 */
public interface PrivateDialTestTimingConfService extends IService<PrivateDialTestTimingConf> {

    /**
     * 根据jobId 查询对应的定时拨测配置
     *
     * @param jobId 任务id
     * @return PrivateDialTestTimingConf 定时拨测配置
     */
    PrivateDialTestTimingConf getByJobId(Long jobId);

    /**
     * 号码列表查询
     *
     * @param queryDTO 分页查询条件
     * @return IPage<DialTestNumberDTO> 号码分页结果
     */
    List<DialTestNumberDTO> findNumbers(DialTestNumberQueryDTO queryDTO);
}
