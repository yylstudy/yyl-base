package com.cqt.hmbc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.hmbc.mapper.PrivateDialTestPushDiscardRecordMapper;
import com.cqt.hmbc.service.PrivateDialTestPushDiscardRecordService;
import com.cqt.model.hmbc.entity.PrivateDialTestPushDiscardRecord;
import org.springframework.stereotype.Service;

/**
 * 定时拨测任务结果推送失败记录
 *
 * @author jeecg-boot
 * @date 2022-07-07
 * @since V2.1.0
 */
@Service
public class PrivateDialTestPushDiscardRecordServiceImpl extends ServiceImpl<PrivateDialTestPushDiscardRecordMapper, PrivateDialTestPushDiscardRecord> implements PrivateDialTestPushDiscardRecordService {

}
