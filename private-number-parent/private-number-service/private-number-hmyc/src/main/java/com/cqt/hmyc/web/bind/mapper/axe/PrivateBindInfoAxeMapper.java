package com.cqt.hmyc.web.bind.mapper.axe;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.bind.axe.dto.AxeUtilizationDTO;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.axe.vo.AxeUtilizationVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author linshiqiang
 * @date 2022/2/18 17:14
 */
@Mapper
public interface PrivateBindInfoAxeMapper extends BaseMapper<PrivateBindInfoAxe> {

    /**
     * 统计分机号余量
     *
     * @param axeUtilizationDTO 条件
     * @return 结果
     */
    List<AxeUtilizationVO> queryAxeUtilizationVO(AxeUtilizationDTO axeUtilizationDTO);
}
