package com.cqt.hmbc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqt.model.hmbc.entity.PrivateDialTestTaskRecord;
import com.cqt.model.hmbc.entity.PrivateDialTestTimingConf;


/**
 * 定时拨测执行任务
 *
 * @author jeecg-boot
 * @date 2022-07-07
 * @since V2.1.0
 */
public interface PrivateDialTestTaskRecordService extends IService<PrivateDialTestTaskRecord> {

    /**
     * 定时拨测任务开始
     *
     * @param timingConf 定时拨测任务配置
     * @param totalCount 拨测号码数量
     * @return 当前任务的Id
     */
    String taskRecordStart(PrivateDialTestTimingConf timingConf, Integer totalCount);

    /**
     * 定时拨测任务完成
     *
     * @param taskRecordId 拨测任务的主键id
     * @param sucCount     拨测成功号码数量
     * @param failCount    拨测失败号码数量
     */
    void taskRecordFinish(String taskRecordId, Integer sucCount, Integer failCount);

    /**
     * 空 拨测任务
     *
     * @param timingConf 定时拨测任务配置
     */
    void emptyTaskRecord(PrivateDialTestTimingConf timingConf);
}
