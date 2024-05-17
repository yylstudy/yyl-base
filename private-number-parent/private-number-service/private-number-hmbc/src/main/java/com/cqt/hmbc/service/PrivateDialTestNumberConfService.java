package com.cqt.hmbc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqt.model.hmbc.dto.DialTestNumberDTO;
import com.cqt.model.hmbc.dto.DialTestNumberQueryDTO;
import com.cqt.model.hmbc.entity.PrivateDialTestNumberConf;

import java.util.List;

/**
 * 定时拨测任务号码配置
 *
 * @author jeecg-boot
 * @date 2022-07-07
 * @since V2.1.0
 */
public interface PrivateDialTestNumberConfService extends IService<PrivateDialTestNumberConf> {

    /**
     * 查询出企业已经配置的号码
     *
     * @param queryDTO 查询条件
     * @return IPage<DialTestNumberDTO> 号码列表信息
     */
    List<DialTestNumberDTO> findList4Config(DialTestNumberQueryDTO queryDTO);

    /**
     * 查询出企业所有的号码
     *
     * @param queryDTO 查询条件
     * @return IPage<DialTestNumberDTO> 号码列表信息
     */
    List<DialTestNumberDTO> findList4All(DialTestNumberQueryDTO queryDTO);
}
