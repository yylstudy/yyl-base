package com.cqt.recycle.web.numpool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxbHis;
import com.cqt.model.bind.entity.MtBindInfoAxbHis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @author linshiqiang
 * @date 2021/9/9 14:51
 * AXB
 */
@Mapper
public interface PrivateBindInfoAxbHisMapper extends BaseMapper<PrivateBindInfoAxbHis> {

    /**
     * 根据创建时间和地市编码查询AXB的真实号码AB
     *
     * @param areaCode  地市编码
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return list
     */
    @Select(" select tel_a, tel_b from private_bind_info_axb_his where create_time BETWEEN #{startTime} and #{endTime} and area_code = #{areaCode}")
    List<MtBindInfoAxbHis> selectTelList(@Param("areaCode") String areaCode, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

}
