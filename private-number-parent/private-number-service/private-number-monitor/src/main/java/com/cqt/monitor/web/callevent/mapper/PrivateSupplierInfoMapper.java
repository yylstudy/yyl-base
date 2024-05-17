package com.cqt.monitor.web.callevent.mapper;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.supplier.PrivateSupplierInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * (PrivateSupplierInfo)表数据库访问层
 *
 * @author linshiqiang
 * @since 2022-12-21 09:49:20
 */

@Mapper
@DS("ms")
public interface PrivateSupplierInfoMapper extends BaseMapper<PrivateSupplierInfo> {


    @Select("select supplier_name from private_supplier_info where supplier_id = #{supplierId}")
    String getSupplierName(String supplierId);
}

