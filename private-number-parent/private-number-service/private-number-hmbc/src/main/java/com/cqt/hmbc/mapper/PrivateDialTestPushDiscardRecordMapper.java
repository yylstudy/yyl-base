package com.cqt.hmbc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.hmbc.entity.PrivateDialTestPushDiscardRecord;
import org.apache.ibatis.annotations.Mapper;


/**
 * 定时拨测任务结果推送失败记录
 *
 * @author jeecg-boot
 * @date 2022-07-07
 * @since V2.1.0
 */
@Mapper
public interface PrivateDialTestPushDiscardRecordMapper extends BaseMapper<PrivateDialTestPushDiscardRecord> {

}
