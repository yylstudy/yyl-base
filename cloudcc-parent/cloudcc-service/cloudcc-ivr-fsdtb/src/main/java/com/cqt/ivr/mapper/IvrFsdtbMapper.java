package com.cqt.ivr.mapper;
import com.cqt.ivr.entity.Appparabase;
import com.cqt.ivr.entity.Elevaluebase;
import com.cqt.ivr.entity.Ivrbase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
@Mapper
public interface IvrFsdtbMapper {

    //查找Appparabase信息
    ArrayList<Appparabase> appparabaseSelect(@Param("sql")String sql);

    //获取所有企业标识
    ArrayList<String> getAllCompanyCode();

    //查找Appparabase信息
    ArrayList<Ivrbase> ivrbaseSelect(@Param("sql")String sql);

    //查找Appparabase信息
    ArrayList<Elevaluebase> elevaluebaseSelect(@Param("sql")String sql);

}