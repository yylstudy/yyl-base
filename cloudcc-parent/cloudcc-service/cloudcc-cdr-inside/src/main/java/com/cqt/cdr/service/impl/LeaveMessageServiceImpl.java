package com.cqt.cdr.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.cdr.interceptor.TheadLocalUtil;
import com.cqt.cdr.service.LeaveMessageService;
import com.cqt.cdr.mapper.LeaveMessageMapper;
import com.cqt.cdr.util.CommonUtils;
import com.cqt.model.cdr.entity.CallCenterMainCdr;
import com.cqt.model.cdr.entity.LeaveMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 * @description 针对表【cloudcc_leave_message(留言)】的数据库操作Service实现
 * @createDate 2023-10-09 16:57:01
 */
@Service
@Slf4j
public class LeaveMessageServiceImpl extends ServiceImpl<LeaveMessageMapper, LeaveMessage>
        implements LeaveMessageService {
    private static final Logger MYSQL_FAIL_LOG = LoggerFactory.getLogger("cdrInsertFailLogger");

    @Override
    public void insertLeaveMessage(String logTag, CallCenterMainCdr mainCdr, String companyCode, String month) {
        mainCdr.setCallerNumber(CommonUtils.replaceNumPerfix(mainCdr.getCallerNumber()));
        mainCdr.setDisplayNumber(CommonUtils.replaceNumPerfix(mainCdr.getDisplayNumber()));
        mainCdr.setCalleeNumber(CommonUtils.replaceNumPerfix(mainCdr.getCalleeNumber()));
        mainCdr.setPlatformNumber(CommonUtils.replaceNumPerfix(mainCdr.getPlatformNumber()));
        mainCdr.setChargeNumber(CommonUtils.replaceNumPerfix(mainCdr.getChargeNumber()));
        LeaveMessage leaveMessage = new LeaveMessage();
        BeanUtils.copyProperties(mainCdr, leaveMessage);
        try {
            this.baseMapper.insert(leaveMessage);
        } catch (DuplicateKeyException duplicateKeyException) {
        } catch (Exception e) {
            log.error(logTag + "留言：{}，录入异常", mainCdr, e);
            if (e instanceof TransientDataAccessResourceException) {
                throw e;
            }
            MYSQL_FAIL_LOG.info(TheadLocalUtil.instance().getSql());
        } finally {
            TheadLocalUtil.instance().reset();
        }
    }
}




