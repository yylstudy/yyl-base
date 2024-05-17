package com.cqt.hmyc.web.numpool.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.model.numpool.vo.NumberAreaCodeCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 隐私号号码信息表(PrivateNumberInfo)表数据库访问层
 *
 * @author makejava
 * @since 2022-05-16 19:25:19
 */
@Mapper
public interface PrivateNumberInfoMapper extends BaseMapper<PrivateNumberInfo> {

    /**
     * 查询axe号码 每个地市多少个号码
     *
     * @param vccId          vccId
     * @param areaCode       区号
     * @param poolType       号码池类型
     * @param allocationFlag 是否分配
     * @return list
     */
    List<NumberAreaCodeCountVO> getNumberCountGroupByAreaCode(@Param("vccId") String vccId,
                                                              @Param("areaCode") String areaCode,
                                                              @Param("poolType") String poolType,
                                                              @Param("allocationFlag") Integer allocationFlag);

    List<String> getAxyNumberList(@Param("vccId") String vccId,
                                  @Param("areaCode") String areaCode,
                                  @Param("poolType") String poolType,
                                  @Param("allocationFlag") Integer allocationFlag);
}

