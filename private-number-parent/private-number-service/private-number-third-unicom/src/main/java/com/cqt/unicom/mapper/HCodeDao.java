package com.cqt.unicom.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.unicom.entity.Hcode;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HCodeDao extends BaseMapper<Hcode> {

	/**
	 * 初始化H码
	 * @author yy
	 *
	 */
	List<Hcode> inithcode();
	List<Hcode> selectHcodeTemp();


}
