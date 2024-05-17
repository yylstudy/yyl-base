package com.cqt.monitor.web.switchroom.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.bind.entity.PrivateAreaLocation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author linshiqiang
 * @since 2021/10/11 15:50
 */
@Mapper
@DS("ms")
public interface PrivateAreaLocationMapper extends BaseMapper<PrivateAreaLocation> {

    /**
     * 批量更新
     *
     * @param areaLocationList list
     * @return 结果
     */
    int updateBatch(List<PrivateAreaLocation> areaLocationList);
}
