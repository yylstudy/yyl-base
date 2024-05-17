package com.cqt.vccidhmyc.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-03-27 14:30
 */
@Mapper
public interface VlrMsrnInfoMapper {

    /**
     * 查询漫游号列表
     *
     * @return List
     */
    @Select("select msrn from vlr_msrn_info")
    List<String> getMsrnList();
}
