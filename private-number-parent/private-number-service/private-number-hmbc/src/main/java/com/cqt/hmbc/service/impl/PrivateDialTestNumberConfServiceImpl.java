package com.cqt.hmbc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.hmbc.mapper.PrivateDialTestNumberConfMapper;
import com.cqt.hmbc.service.PrivateDialTestNumberConfService;
import com.cqt.model.hmbc.dto.DialTestNumberDTO;
import com.cqt.model.hmbc.dto.DialTestNumberQueryDTO;
import com.cqt.model.hmbc.entity.PrivateDialTestNumberConf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 定时拨测任务号码配置
 *
 * @author jeecg-boot
 * @date 2022-07-07
 * @since V2.1.0
 */
@Slf4j
@Service
public class PrivateDialTestNumberConfServiceImpl extends ServiceImpl<PrivateDialTestNumberConfMapper, PrivateDialTestNumberConf> implements PrivateDialTestNumberConfService {

    /**
     * 查询出企业已经配置的号码
     *
     * @param queryDTO 查询条件
     * @return IPage<DialTestNumberDTO> 号码列表信息
     */
    @Override
    public List<DialTestNumberDTO> findList4Config(DialTestNumberQueryDTO queryDTO) {
        return baseMapper.findList4Config(queryDTO);
    }

    /**
     * 查询出企业所有的号码
     *
     * @param queryDTO 查询条件
     * @return IPage<DialTestNumberDTO> 号码列表信息
     */
    @Override
    public List<DialTestNumberDTO> findList4All(DialTestNumberQueryDTO queryDTO) {
        return baseMapper.findList4All(queryDTO);
    }
}
