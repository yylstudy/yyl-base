package com.cqt.hmbc.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.common.constants.HmbcConstants;
import com.cqt.hmbc.mapper.PrivateDialTestTaskRecordMapper;
import com.cqt.hmbc.service.PrivateDialTestTaskRecordService;
import com.cqt.model.hmbc.entity.PrivateDialTestTaskRecord;
import com.cqt.model.hmbc.entity.PrivateDialTestTimingConf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 定时拨测执行任务管理
 *
 * @author jeecg-boot
 * @date 2022-07-07
 * @since V2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateDialTestTaskRecordServiceImpl extends ServiceImpl<PrivateDialTestTaskRecordMapper, PrivateDialTestTaskRecord> implements PrivateDialTestTaskRecordService {

    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    /**
     * 定时拨测任务开始
     *
     * @param timingConf 定时拨测任务配置
     * @param totalCount 拨测号码数量
     * @return 当前任务的Id
     */
    @Override
    public String taskRecordStart(PrivateDialTestTimingConf timingConf, Integer totalCount) {
        PrivateDialTestTaskRecord saveEntity = new PrivateDialTestTaskRecord();

        String taskRecordId = IdUtil.getSnowflakeNextIdStr();

        saveEntity.setId(taskRecordId)
                .setJobId(timingConf.getJobId())
                .setVccId(timingConf.getVccId())
                .setType(timingConf.getType())
                .setTotalCount(totalCount)
                .setServerIp(nacosDiscoveryProperties.getIp())
                .setCreateBy(nacosDiscoveryProperties.getIp())
                .setState(HmbcConstants.DIAL_TEST_TASK_EXECUTION_STATE_EXECUTING);

        this.save(saveEntity);

        return taskRecordId;
    }

    /**
     * 定时拨测任务完成
     *
     * @param taskRecordId 拨测任务的主键id
     * @param sucCount     拨测成功号码数量
     * @param failCount    拨测失败号码数量
     */
    @Override
    public void taskRecordFinish(String taskRecordId, Integer sucCount, Integer failCount) {
        if (StrUtil.isEmpty(taskRecordId)) {
            log.warn("taskRecordId为空, 本次不更新任务");
            return;
        }
        PrivateDialTestTaskRecord updateEntity = new PrivateDialTestTaskRecord();

        updateEntity.setId(taskRecordId)
                .setSucCount(sucCount)
                .setFailCount(failCount)
                .setUpdateBy(nacosDiscoveryProperties.getIp())
                .setState(HmbcConstants.DIAL_TEST_TASK_EXECUTION_STATE_FINISH);

        this.updateById(updateEntity);
    }

    /**
     * 空 拨测任务
     *
     * @param timingConf 定时拨测任务配置
     */
    @Override
    public void emptyTaskRecord(PrivateDialTestTimingConf timingConf) {
        PrivateDialTestTaskRecord saveEntity = new PrivateDialTestTaskRecord();

        saveEntity.setJobId(timingConf.getJobId())
                .setVccId(timingConf.getVccId())
                .setType(timingConf.getType())
                .setTotalCount(0)
                .setSucCount(0)
                .setFailCount(0)
                .setCreateBy(nacosDiscoveryProperties.getIp())
                .setUpdateBy(nacosDiscoveryProperties.getIp())
                .setState(HmbcConstants.DIAL_TEST_TASK_EXECUTION_STATE_FINISH);

        this.save(saveEntity);
    }
}
