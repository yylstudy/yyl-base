package com.cqt.cdr.mapper;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.cdr.entity.LeaveMessage;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Administrator
* @description 针对表【cloudcc_leave_message(留言)】的数据库操作Mapper
* @createDate 2023-10-09 16:57:01
* @Entity com.cqt.cdr.entity.LeaveMessage
*/
@Mapper
@DS("cdr")
public interface LeaveMessageMapper extends BaseMapper<LeaveMessage> {

}




