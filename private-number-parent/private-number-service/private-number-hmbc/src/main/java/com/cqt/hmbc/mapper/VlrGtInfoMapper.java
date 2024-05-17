package com.cqt.hmbc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.hmbc.entity.VlrGtInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * VlrGtInfoMapper
 *
 * @author Xienx
 * @date 2023年02月13日 9:58
 */
@Mapper
public interface VlrGtInfoMapper extends BaseMapper<VlrGtInfo> {

    /**
     * 查询GT信息
     *
     * @return Map<String, VlrGtInfo>
     * 其中key是GT码, value是GT信息
     */
    @Select(" SELECT gt_code, map_url FROM vlr_gt_info ")
    List<VlrGtInfo> getVlrGtInfos();
}
