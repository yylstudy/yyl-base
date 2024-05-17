package com.cqt.cdr.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.cqt.model.cdr.entity.CallCenterMainCdr;
import com.cqt.model.cdr.entity.LeaveMessage;

/**
* @author Administrator
* @description 针对表【cloudcc_leave_message(留言)】的数据库操作Service
* @createDate 2023-10-09 16:57:01
*/
public interface LeaveMessageService extends IService<LeaveMessage> {

    void insertLeaveMessage(String logTag, CallCenterMainCdr mainCdr, String companyCode, String month);
}
