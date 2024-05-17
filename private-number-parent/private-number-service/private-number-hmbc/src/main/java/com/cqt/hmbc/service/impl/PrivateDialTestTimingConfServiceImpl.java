package com.cqt.hmbc.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.common.constants.HmbcConstants;
import com.cqt.hmbc.mapper.PrivateDialTestTimingConfMapper;
import com.cqt.hmbc.service.PrivateDialTestNumberConfService;
import com.cqt.hmbc.service.PrivateDialTestTimingConfService;
import com.cqt.model.hmbc.dto.DialTestNumberDTO;
import com.cqt.model.hmbc.dto.DialTestNumberQueryDTO;
import com.cqt.model.hmbc.entity.PrivateDialTestTimingConf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 定时拨测任务配置管理
 *
 * @author jeecg-boot
 * @date 2022-07-07
 * @since V2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateDialTestTimingConfServiceImpl extends ServiceImpl<PrivateDialTestTimingConfMapper, PrivateDialTestTimingConf> implements PrivateDialTestTimingConfService {

    private final PrivateDialTestNumberConfService privateDialTestNumberConfService;

    /**
     * 根据jobId 查询对应的定时拨测配置
     *
     * @param jobId 任务id
     * @return PrivateDialTestTimingConf 定时拨测配置
     */
    @Override
    public PrivateDialTestTimingConf getByJobId(Long jobId) {
        return getOne(Wrappers.<PrivateDialTestTimingConf>lambdaQuery()
                .eq(PrivateDialTestTimingConf::getJobId, jobId)
                .last(" limit 1 "));
    }

    /**
     * 号码列表查询
     *
     * @param queryDTO 分页查询条件
     * @return IPage<DialTestNumberDTO> 号码分页结果
     */
    @Override
    public List<DialTestNumberDTO> findNumbers(DialTestNumberQueryDTO queryDTO) {
        if (HmbcConstants.NUMBER_RANGE_CUSTOMIZE.equals(queryDTO.getNumberRange())) {
            return privateDialTestNumberConfService.findList4Config(queryDTO);
        }
        return privateDialTestNumberConfService.findList4All(queryDTO);
    }
}
